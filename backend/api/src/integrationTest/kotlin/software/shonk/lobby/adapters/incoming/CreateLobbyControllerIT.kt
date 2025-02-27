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
import software.shonk.AN_INVALID_PLAYERNAME
import software.shonk.A_VALID_PLAYERNAME
import software.shonk.CREATE_LOBBY_ENDPOINT
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
            clearAllMocks()

            // When...
            val response =
                client.post(CREATE_LOBBY_ENDPOINT) {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"$A_VALID_PLAYERNAME\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.Created, response.status)

            val lobbyIdFromResponse = Json.decodeFromString<LobbyIdDTO>(response.bodyAsText())
            verify(exactly = 1) {
                saveLobbyPort.saveLobby(match { it -> it.id == lobbyIdFromResponse.lobbyId })
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
            clearAllMocks()

            // When...
            val response =
                client.post(CREATE_LOBBY_ENDPOINT) {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"$AN_INVALID_PLAYERNAME\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Name must only consist of valid characters", response.bodyAsText())

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
            clearAllMocks()

            // When...
            val response =
                client.post(CREATE_LOBBY_ENDPOINT) {
                    contentType(ContentType.Application.Json)
                    setBody("{}")
                }

            // Then...
            assertEquals(HttpStatusCode.BadRequest, response.status)
            // todo decide if this exact text should be tested here
            assertEquals("Player name is missing", response.bodyAsText())

            verify(exactly = 0) { saveLobbyPort.saveLobby(any()) }
        }

    @Serializable data class LobbyIdDTO(val lobbyId: Long)
}
