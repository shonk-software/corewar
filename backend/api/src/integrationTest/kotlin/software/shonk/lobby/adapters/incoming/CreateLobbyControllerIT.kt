package software.shonk.lobby.adapters.incoming

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
import software.shonk.interpreter.IShork
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.CreateLobbyUseCase
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.application.service.CreateLobbyService
import software.shonk.moduleApiV1

class CreateLobbyControllerIT : KoinTest {

    private val testModule = module {
        single<IShork> { MockShork() }
        single<CreateLobbyUseCase> { CreateLobbyService(get(), get()) }
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
    fun `creating a new lobby with valid playerName succeeds with 201 and returns the id of the lobby`() =
        testApplication {
            // Setup
            application {
                basicModule()
                moduleApiV1()
            }

            // Given...
            val saveLobbyPort = get<SaveLobbyPort>()

            // When...
            clearAllMocks()
            // todo testfactory
            val result =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.Created, result.status)

            val lobbyId = Json.decodeFromString<LobbyIdDTO>(result.bodyAsText())
            verify(exactly = 1) {
                saveLobbyPort.saveLobby(match { it -> it.id == lobbyId.lobbyId })
            }
        }

    @Test
    fun `creating a new lobby with invalid playerName returns 400 and doesnt create a new lobby`() =
        testApplication {
            // Setup
            application {
                basicModule()
                moduleApiV1()
            }

            // Given...
            val saveLobbyPort = get<SaveLobbyPort>()

            // When...
            clearAllMocks()
            // todo testfactory
            val result =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"invalidName :3\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.BadRequest, result.status)
            assertEquals("Your player name is invalid", result.bodyAsText())

            verify(exactly = 0) { saveLobbyPort.saveLobby(any()) }
        }

    @Test
    fun `creating a lobby without playerName returns 400 and doesnt create a new lobby`() =
        testApplication {
            // Setup
            application {
                basicModule()
                moduleApiV1()
            }

            // Given...
            val saveLobbyPort = get<SaveLobbyPort>()

            // When...
            clearAllMocks()
            // todo testfactory
            val result =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{}")
                }

            // Then...
            assertEquals(HttpStatusCode.BadRequest, result.status)
            assertEquals("Player name is missing", result.bodyAsText())

            verify(exactly = 0) { saveLobbyPort.saveLobby(any()) }
        }

    @Serializable data class LobbyIdDTO(val lobbyId: Long)
}
