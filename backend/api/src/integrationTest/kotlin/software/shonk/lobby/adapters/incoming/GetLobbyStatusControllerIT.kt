package software.shonk.lobby.adapters.incoming

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import software.shonk.basicModule
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.GetLobbyStatusQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.application.service.GetLobbyStatusService
import software.shonk.lobby.domain.GameState
import software.shonk.lobby.domain.InterpreterSettings
import software.shonk.lobby.domain.Lobby
import software.shonk.lobby.domain.Status
import software.shonk.lobby.domain.Winner
import software.shonk.moduleApiV1

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
    fun `get lobby status with lobby id that doesn't exist`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...

        // When...
        val result = client.get("/api/v1/lobby/1/status")

        // Then...
        assertEquals(HttpStatusCode.NotFound, result.status)
        assert(result.bodyAsText().contains("Lobby with id 1 not found!"))
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
        // todo Testfactory?
        val aLobby =
            Lobby(
                id = 0,
                programs =
                    hashMapOf<String, String>(
                        "playerA" to "someString",
                        "playerB" to "someOtherString",
                    ),
                shork = MockShork(),
                gameState = GameState.FINISHED,
                winner = Winner.B,
                currentSettings = InterpreterSettings(),
                joinedPlayers = mutableListOf("playerA", "playerB"),
            )
        saveLobby.saveLobby(aLobby)

        // When...
        val response = client.get("/api/v1/lobby/0/status")

        // Then...
        assertEquals(HttpStatusCode.OK, response.status)

        val lobbyStatus = Json.decodeFromString<Status>(response.bodyAsText())
        assertNotNull(lobbyStatus)
        assertTrue(lobbyStatus.playerASubmitted)
        assertTrue(lobbyStatus.playerBSubmitted)
        assertEquals(GameState.FINISHED, lobbyStatus.gameState)
        assertEquals(Winner.B, lobbyStatus.result.winner)
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
        // todo Testfactory?
        val aLobby =
            Lobby(
                id = 0,
                programs = hashMapOf<String, String>(),
                shork = MockShork(),
                currentSettings = InterpreterSettings(),
                joinedPlayers = mutableListOf("playerA"),
            )
        saveLobby.saveLobby(aLobby)

        // When...
        val lobbyStatusResponse = client.get("/api/v1/lobby/${aLobby.id}/status")

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
        // todo Testfactory?
        val aLobby =
            Lobby(
                id = 0,
                programs =
                    hashMapOf<String, String>("playerA" to "MOV 0, 1", "playerB" to "MOV 0, 1"),
                shork = MockShork(),
                currentSettings = InterpreterSettings(),
                joinedPlayers = mutableListOf("playerA", "playerB"),
            )
        // todo we should not have to call run here, but instead a testfactory method should provide
        // us with a lobby,
        // that is already run as we need it and contains the correct visualization data
        aLobby.run()
        saveLobby.saveLobby(aLobby)

        // When...
        val lobbyStatusResponse = client.get("/api/v1/lobby/${aLobby.id}/status")

        // Then...
        assertEquals(HttpStatusCode.OK, lobbyStatusResponse.status)

        val lobbyStatus = Json.decodeFromString<Status>(lobbyStatusResponse.bodyAsText())
        assertEquals(GameState.FINISHED, lobbyStatus.gameState)
        assertTrue(lobbyStatus.visualizationData.isNotEmpty())

        // Also available when we specifically toggle it to true...
        // When...
        val lobbyStatusResponseExplicitInclude =
            client.get("/api/v1/lobby/${aLobby.id}/status?showVisualizationData=true")

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
        // todo Testfactory?
        val aLobby =
            Lobby(
                id = 0,
                programs =
                    hashMapOf<String, String>("playerA" to "MOV 0, 1", "playerB" to "MOV 0, 1"),
                shork = MockShork(),
                currentSettings = InterpreterSettings(),
                joinedPlayers = mutableListOf("playerA", "playerB"),
            )
        // todo we should not have to call run here, but instead a testfactory method should provide
        // us with a lobby,
        // that is already run as we need it and contains the correct visualization data
        aLobby.run()
        saveLobby.saveLobby(aLobby)

        // When...
        val lobbyStatusResponse =
            client.get("/api/v1/lobby/${aLobby.id}/status?showVisualizationData=false")

        // Then...
        assertEquals(HttpStatusCode.OK, lobbyStatusResponse.status)

        val lobbyStatus = Json.decodeFromString<Status>(lobbyStatusResponse.bodyAsText())
        assertEquals(GameState.FINISHED, lobbyStatus.gameState)
        assertTrue(lobbyStatus.visualizationData.isEmpty())
    }
}
