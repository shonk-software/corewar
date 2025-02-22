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
import software.shonk.lobby.application.port.incoming.AddProgramToLobbyUseCase
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.application.service.AddProgramToLobbyService
import software.shonk.lobby.domain.GameState
import software.shonk.lobby.domain.Lobby
import software.shonk.moduleApiV1

class AddProgramToLobbyControllerIT : KoinTest {

    private val testModule = module {
        single<AddProgramToLobbyUseCase> { AddProgramToLobbyService(get(), get()) }
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
    fun `submitting code to only player in lobby returns 200`() = testApplication {
        // Setup...
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val saveLobby = get<SaveLobbyPort>()
        val aLobbyId = 0L
        val aProgram = "someString"
        // todo Testfactory?
        val aLobby =
            Lobby(
                id = aLobbyId,
                programs = hashMapOf(),
                shork = MockShork(),
                gameState = GameState.NOT_STARTED,
                joinedPlayers = mutableListOf("playerA"),
            )
        saveLobby.saveLobby(aLobby)
        clearAllMocks()

        // When...
        val response =
            client.post("/api/v1/lobby/$aLobbyId/code/playerA") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\": \"$aProgram\"}")
            }

        // Then...
        assertEquals(HttpStatusCode.OK, response.status)
        verify(exactly = 1) {
            saveLobby.saveLobby(
                match { it -> it.id == aLobbyId && it.programs["playerA"] == aProgram }
            )
        }
    }

    @Test
    fun `submitting code to an invalid lobby returns 400`() = testApplication {
        // Setup...
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val saveLobby = get<SaveLobbyPort>()
        val anInvalidLobbyId = -1L

        // When...
        val response =
            client.post("/api/v1/lobby/$anInvalidLobbyId/code/playerA") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\": \"MOV 0, 1\"}")
            }

        // Then...
        assertEquals(HttpStatusCode.BadRequest, response.status)
        verify(exactly = 0) { saveLobby.saveLobby(any()) }
    }

    @Test
    fun `submitting code with invalid username returns 400 and does not touch any lobbies`() =
        testApplication {
            // Setup...
            application {
                basicModule()
                moduleApiV1()
            }

            // Given...
            val saveLobby = get<SaveLobbyPort>()
            val aLobbyId = 0L
            val anInvalidPlayerName = "an invalid playername :3"

            // When...
            val response =
                client.post("/api/v1/lobby/$aLobbyId/code/$anInvalidPlayerName") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"code\": \"MOV 0, 1\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { saveLobby.saveLobby(any()) }
        }

    @Test
    fun `trying to submit code to a lobby which you have not joined yet returns 403`() =
        testApplication {
            // Setup...
            application {
                basicModule()
                moduleApiV1()
            }

            // Given...
            val saveLobby = get<SaveLobbyPort>()
            val aLobbyId = 0L
            // todo Testfactory?
            val aLobby =
                Lobby(
                    id = aLobbyId,
                    programs = hashMapOf(),
                    shork = MockShork(),
                    gameState = GameState.NOT_STARTED,
                    joinedPlayers = mutableListOf("playerA"),
                )
            saveLobby.saveLobby(aLobby)
            clearAllMocks()

            // When...
            val response =
                client.post("/api/v1/lobby/$aLobbyId/code/playerB") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"code\": \"MOV 0, 1\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.Forbidden, response.status)
            verify(exactly = 0) { saveLobby.saveLobby(any()) }
        }

    @Test
    fun `trying to submit with missing code in json returns 400`() = testApplication {
        // Setup...
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val saveLobby = get<SaveLobbyPort>()
        val aLobbyId = 0L
        // todo Testfactory?
        val aLobby =
            Lobby(
                id = aLobbyId,
                programs = hashMapOf(),
                shork = MockShork(),
                gameState = GameState.NOT_STARTED,
                joinedPlayers = mutableListOf("playerA"),
            )
        saveLobby.saveLobby(aLobby)
        clearAllMocks()

        // When...
        val response =
            client.post("/api/v1/lobby/$aLobbyId/code/playerA") {
                contentType(ContentType.Application.Json)
                setBody("{}")
            }

        // Then...
        assertEquals(HttpStatusCode.BadRequest, response.status)
        verify(exactly = 0) { saveLobby.saveLobby(any()) }
    }

    @Test
    fun `trying to submit to a lobby that does not exist returns 404`() = testApplication {
        // Setup...
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val saveLobby = get<SaveLobbyPort>()

        // When...
        val response =
            client.post("/api/v1/lobby/0/code/playerA") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\": \"MOV 0, 1\"}")
            }

        // Then...
        assertEquals(HttpStatusCode.NotFound, response.status)
        verify(exactly = 0) { saveLobby.saveLobby(any()) }
    }

    @Test
    fun `trying to submit code to a lobby that has already completed returns 403`() =
        testApplication {
            // Setup...
            application {
                basicModule()
                moduleApiV1()
            }

            // Given...
            val saveLobby = get<SaveLobbyPort>()
            val aLobbyId = 0L
            // todo Testfactory?
            val aLobby =
                Lobby(
                    id = aLobbyId,
                    programs = hashMapOf("playerA" to "MOV, 0, 1", "playerB" to "MOV, 0, 1"),
                    shork = MockShork(),
                    gameState = GameState.FINISHED,
                    joinedPlayers = mutableListOf("playerA", "playerB"),
                )
            saveLobby.saveLobby(aLobby)
            clearAllMocks()

            // When...
            val response =
                client.post("/api/v1/lobby/$aLobbyId/code/playerA") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"code\": \"MOV 0, 1\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.Forbidden, response.status)
            verify(exactly = 0) { saveLobby.saveLobby(any()) }
        }
}
