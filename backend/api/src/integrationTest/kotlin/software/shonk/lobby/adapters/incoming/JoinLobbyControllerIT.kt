package software.shonk.lobby.adapters.incoming

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import software.shonk.basicModule
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.JoinLobbyUseCase
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.application.service.JoinLobbyService
import software.shonk.lobby.domain.GameState
import software.shonk.lobby.domain.Lobby
import software.shonk.moduleApiV1

class JoinLobbyControllerIT : KoinTest {

    private val testModule = module {
        single<JoinLobbyUseCase> { JoinLobbyService(get(), get()) }
        val spy = spyk(MemoryLobbyManager())
        single { spy as LoadLobbyPort }
        single { spy as SaveLobbyPort }
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
    fun `joining an existing lobby with a valid playerName returns 200 and updates the lobby`() =
        testApplication {
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
                    gameState = GameState.NOT_STARTED,
                    joinedPlayers = mutableListOf("playerA"),
                )
            saveLobby.saveLobby(aLobby)

            // When...
            clearAllMocks()
            val result =
                client.post("/api/v1/lobby/0/join") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerB\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.OK, result.status)
            verify(exactly = 1) {
                saveLobby.saveLobby(match { it -> it.joinedPlayers.contains("playerB") })
            }
        }

    @Test
    fun `trying to join an existing lobby with already joined playerName returns 409 and does not touch the lobby`() =
        testApplication {
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
                    gameState = GameState.NOT_STARTED,
                    joinedPlayers = mutableListOf("playerA"),
                )
            saveLobby.saveLobby(aLobby)

            // When...
            clearAllMocks()
            val result =
                client.post("/api/v1/lobby/0/join") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.Conflict, result.status)
            verify(exactly = 0) { saveLobby.saveLobby(any()) }
        }

    @Test
    fun `trying to join an existing lobby with invalid playerName returns 400 and does not touch the lobby`() =
        testApplication {
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
                    gameState = GameState.NOT_STARTED,
                    joinedPlayers = mutableListOf("playerA"),
                )
            saveLobby.saveLobby(aLobby)

            // When...
            clearAllMocks()
            val result =
                client.post("/api/v1/lobby/0/join") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"invalid name :3\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.BadRequest, result.status)
            verify(exactly = 0) { saveLobby.saveLobby(any()) }
        }

    @Test
    fun `trying to join an existing lobby with missing playerName returns 400 and does not touch the lobby`() =
        testApplication {
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
                    gameState = GameState.NOT_STARTED,
                    joinedPlayers = mutableListOf("playerA"),
                )
            saveLobby.saveLobby(aLobby)

            // When...
            clearAllMocks()
            val result =
                client.post("/api/v1/lobby/0/join") {
                    contentType(ContentType.Application.Json)
                    setBody("{}")
                }

            // Then...
            assertEquals(HttpStatusCode.BadRequest, result.status)
            verify(exactly = 0) { saveLobby.saveLobby(any()) }
        }

    @Test
    fun `trying to join a nonexistent lobby with valid playerName returns 404 and does not create a lobby`() =
        testApplication {
            // Setup
            application {
                basicModule()
                moduleApiV1()
            }
            // Given...
            val saveLobby = get<SaveLobbyPort>()

            // When...
            clearAllMocks()
            val result =
                client.post("/api/v1/lobby/0/join") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.NotFound, result.status)
            verify(exactly = 0) { saveLobby.saveLobby(any()) }
        }
}
