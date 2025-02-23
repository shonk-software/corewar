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
import software.shonk.*
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.AddProgramToLobbyUseCase
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.application.service.AddProgramToLobbyService
import software.shonk.lobby.domain.GameState

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
        saveLobby.saveLobby(
            aLobby(id = A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
        )
        clearAllMocks()

        // When...
        val response =
            client.post("/api/v1/lobby/$A_VALID_LOBBY_ID/code/$A_VALID_PLAYERNAME") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\": \"$A_REDCODE_PROGRAM\"}")
            }

        // Then...
        assertEquals(HttpStatusCode.OK, response.status)
        verify(exactly = 1) {
            // todo adjust linter or just split so this reads nicer | maybe helper?
            saveLobby.saveLobby(
                match { it ->
                    it.id == A_VALID_LOBBY_ID &&
                        it.programs[A_VALID_PLAYERNAME] == A_REDCODE_PROGRAM
                }
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

        // When...
        val response =
            client.post("/api/v1/lobby/$AN_INVALID_LOBBY_ID/code/$A_VALID_PLAYERNAME") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\": \"$A_REDCODE_PROGRAM\"}")
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

            // When...
            val response =
                client.post("/api/v1/lobby/$A_VALID_LOBBY_ID/code/$AN_INVALID_PLAYERNAME") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"code\": \"$A_REDCODE_PROGRAM\"}")
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
            saveLobby.saveLobby(
                aLobby(id = A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
            )
            clearAllMocks()

            // When...
            val response =
                client.post("/api/v1/lobby/$A_VALID_LOBBY_ID/code/$ANOTHER_VALID_PLAYERNAME") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"code\": \"$A_REDCODE_PROGRAM\"}")
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
        saveLobby.saveLobby(
            aLobby(id = A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
        )
        clearAllMocks()

        // When...
        val response =
            client.post("/api/v1/lobby/$A_VALID_LOBBY_ID/code/$A_VALID_PLAYERNAME") {
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
            client.post(
                "/api/v1/lobby/$A_LOBBY_ID_THAT_HAS_NOT_BEEN_CREATED/code/$A_VALID_PLAYERNAME"
            ) {
                contentType(ContentType.Application.Json)
                setBody("{\"code\": \"$A_REDCODE_PROGRAM\"}")
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
            saveLobby.saveLobby(
                aLobby(
                    id = A_VALID_LOBBY_ID,
                    programs =
                        hashMapOf(
                            A_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                            ANOTHER_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                        ),
                    gameState = GameState.FINISHED,
                    joinedPlayers = mutableListOf(A_VALID_PLAYERNAME, ANOTHER_VALID_PLAYERNAME),
                )
            )
            clearAllMocks()

            // When...
            val response =
                client.post("/api/v1/lobby/$A_VALID_LOBBY_ID/code/$A_VALID_PLAYERNAME") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"code\": \"$A_REDCODE_PROGRAM\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.Forbidden, response.status)
            verify(exactly = 0) { saveLobby.saveLobby(any()) }
        }
}
