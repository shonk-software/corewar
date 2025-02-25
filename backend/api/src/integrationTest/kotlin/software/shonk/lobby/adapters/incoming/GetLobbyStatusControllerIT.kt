package software.shonk.lobby.adapters.incoming

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import software.shonk.*
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.GetLobbyStatusQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.application.service.GetLobbyStatusService
import software.shonk.lobby.domain.GameState
import software.shonk.lobby.domain.Status
import software.shonk.lobby.domain.Winner

class GetLobbyStatusControllerIT : KoinTest {

    private val testModule = module {
        single<GetLobbyStatusQuery> { GetLobbyStatusService(get()) }
        singleOf(::MemoryLobbyManager) {
            bind<LoadLobbyPort>()
            bind<SaveLobbyPort>()
        }
    }

    @BeforeEach
    fun setup() {
        startKoin { modules(testModule) }
    }

    @AfterEach
    fun teardown() {
        stopKoin()
    }

    @Test
    fun `404 when lobby id doesn't exist`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...

        // When...
        val result = client.get("/api/v1/lobby/$A_LOBBY_ID_THAT_HAS_NOT_BEEN_CREATED/status")

        // Then...
        assertEquals(HttpStatusCode.NotFound, result.status)
        assert(
            result
                .bodyAsText()
                .contains("Lobby with id $A_LOBBY_ID_THAT_HAS_NOT_BEEN_CREATED not found!")
        )
    }

    @Test
    fun `400 when lobby id fails validation`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...

        // When...
        val result = client.get("/api/v1/lobby/$AN_INVALID_LOBBY_ID/status")

        // Then...
        assertEquals(HttpStatusCode.BadRequest, result.status)
        assert(result.bodyAsText().contains("Failed to parse Lobby id: $AN_INVALID_LOBBY_ID"))
    }

    @Test
    fun `test get lobby status with valid custom ID`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val saveLobby = get<SaveLobbyPort>()
        saveLobby.saveLobby(
            aLobby(
                id = A_VALID_LOBBY_ID,
                programs =
                    hashMapOf(
                        A_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                        ANOTHER_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                    ),
                gameState = GameState.FINISHED,
                winner = Winner.B,
                joinedPlayers = mutableListOf(A_VALID_PLAYERNAME, ANOTHER_VALID_PLAYERNAME),
            )
        )

        // When...
        val response = client.get("/api/v1/lobby/$A_VALID_LOBBY_ID/status")

        // Then...
        assertEquals(HttpStatusCode.OK, response.status)

        val lobbyStatusResponse = Json.decodeFromString<Status>(response.bodyAsText())
        assertNotNull(lobbyStatusResponse)
        assertTrue(lobbyStatusResponse.playerASubmitted)
        assertTrue(lobbyStatusResponse.playerBSubmitted)
        assertEquals(GameState.FINISHED, lobbyStatusResponse.gameState)
        assertEquals(Winner.B, lobbyStatusResponse.result.winner)
    }

    @Test
    fun `game visualization data is absent before any game round has run`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val saveLobby = get<SaveLobbyPort>()
        saveLobby.saveLobby(
            aLobby(id = A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
        )

        // When...
        val lobbyStatusResponse = client.get("/api/v1/lobby/$A_VALID_LOBBY_ID/status")

        // Then
        assertEquals(HttpStatusCode.OK, lobbyStatusResponse.status)

        val lobbyStatus = Json.decodeFromString<Status>(lobbyStatusResponse.bodyAsText())
        assertEquals(GameState.NOT_STARTED, lobbyStatus.gameState)
        assertTrue(lobbyStatus.visualizationData.isEmpty())
    }

    @Test
    fun `game visualization data is present after a game round has run`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val saveLobby = get<SaveLobbyPort>()
        saveLobby.saveLobby(
            aLobbyThatHasAlreadyRun(
                id = A_VALID_LOBBY_ID,
                programs =
                    hashMapOf(
                        A_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                        ANOTHER_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                    ),
                joinedPlayers = mutableListOf(A_VALID_PLAYERNAME, ANOTHER_VALID_PLAYERNAME),
            )
        )

        // When...
        val lobbyStatusResponse = client.get("/api/v1/lobby/$A_VALID_LOBBY_ID/status")

        // Then...
        assertEquals(HttpStatusCode.OK, lobbyStatusResponse.status)

        val lobbyStatus = Json.decodeFromString<Status>(lobbyStatusResponse.bodyAsText())
        assertEquals(GameState.FINISHED, lobbyStatus.gameState)
        assertTrue(lobbyStatus.visualizationData.isNotEmpty())

        // Also available when we specifically toggle it to true...
        // When...
        val lobbyStatusResponseExplicitInclude =
            client.get("/api/v1/lobby/$A_VALID_LOBBY_ID/status?showVisualizationData=true")

        // Then...
        assertEquals(HttpStatusCode.OK, lobbyStatusResponseExplicitInclude.status)

        val lobbyStatusExplicitInclude =
            Json.decodeFromString<Status>(lobbyStatusResponseExplicitInclude.bodyAsText())
        assertEquals(GameState.FINISHED, lobbyStatusExplicitInclude.gameState)
        assertTrue(lobbyStatusExplicitInclude.visualizationData.isNotEmpty())
    }

    @Test
    fun `game visualization data can be excluded with query parameter`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val saveLobby = get<SaveLobbyPort>()
        saveLobby.saveLobby(
            aLobbyThatHasAlreadyRun(
                id = A_VALID_LOBBY_ID,
                programs =
                    hashMapOf(
                        A_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                        ANOTHER_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                    ),
                joinedPlayers = mutableListOf(A_VALID_PLAYERNAME, ANOTHER_VALID_PLAYERNAME),
            )
        )

        // When...
        val lobbyStatusResponse =
            client.get("/api/v1/lobby/$A_VALID_LOBBY_ID/status?showVisualizationData=false")

        // Then...
        assertEquals(HttpStatusCode.OK, lobbyStatusResponse.status)

        val lobbyStatus = Json.decodeFromString<Status>(lobbyStatusResponse.bodyAsText())
        assertEquals(GameState.FINISHED, lobbyStatus.gameState)
        assertTrue(lobbyStatus.visualizationData.isEmpty())
    }
}
