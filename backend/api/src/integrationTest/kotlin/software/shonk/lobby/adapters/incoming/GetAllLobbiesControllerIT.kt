package software.shonk.lobby.adapters.incoming

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
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
import software.shonk.basicModule
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.GetAllLobbiesQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.application.service.GetAllLobbiesService
import software.shonk.lobby.domain.*
import software.shonk.moduleApiV1

class GetAllLobbiesControllerIT : KoinTest {

    private val testModule = module {
        single<GetAllLobbiesQuery> { GetAllLobbiesService(get()) }
        singleOf(::MemoryLobbyManager) {
            bind<LoadLobbyPort>()
            bind<SaveLobbyPort>()
        }
    }

    @Serializable data class AllLobbiesResponse(val lobbies: List<LobbyStatus>)

    @BeforeEach
    fun setup() {
        startKoin { modules(testModule) }
    }

    @AfterEach
    fun teardown() {
        stopKoin()
    }

    @Test
    fun `returns empty list when there are no lobbies with 200`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...

        // When...
        val result = client.get("/api/v1/lobby")

        // Then...
        assertEquals(HttpStatusCode.OK, result.status)

        val allLobbies = Json.decodeFromString<AllLobbiesResponse>(result.bodyAsText())
        assertEquals(0, allLobbies.lobbies.size)
    }

    @Test
    fun `returns a list of all existing lobbies with 200`() = testApplication {
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
        val anotherLobby =
            Lobby(
                id = 1,
                programs = hashMapOf(),
                shork = MockShork(),
                gameState = GameState.NOT_STARTED,
                currentSettings = InterpreterSettings(),
                joinedPlayers = mutableListOf("playerA"),
            )
        saveLobby.saveLobby(aLobby)
        saveLobby.saveLobby(anotherLobby)

        // When...
        val result = client.get("/api/v1/lobby")

        // Then...
        assertEquals(HttpStatusCode.OK, result.status)

        val allLobbies = Json.decodeFromString<AllLobbiesResponse>(result.bodyAsText())
        assertEquals(2, allLobbies.lobbies.size)

        assertTrue(
            allLobbies.lobbies.contains(
                LobbyStatus(
                    id = aLobby.id,
                    playersJoined = aLobby.joinedPlayers,
                    gameState = aLobby.gameState.toString(),
                )
            )
        )

        assertTrue(
            allLobbies.lobbies.contains(
                LobbyStatus(
                    id = anotherLobby.id,
                    playersJoined = anotherLobby.joinedPlayers,
                    gameState = anotherLobby.gameState.toString(),
                )
            )
        )
    }
}
