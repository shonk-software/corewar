package software.shonk.lobby.adapters.incoming

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import kotlinx.serialization.encodeToString
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
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.SetLobbySettingsUseCase
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.application.service.SetLobbySettingsService
import software.shonk.lobby.domain.GameState
import software.shonk.lobby.domain.InterpreterSettings
import software.shonk.lobby.domain.Lobby
import software.shonk.moduleApiV1

class SetLobbySettingsControllerIT : KoinTest {

    private val testModule = module {
        single<SetLobbySettingsUseCase> { SetLobbySettingsService(get(), get()) }
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
    fun `update settings for an existing lobby returns 200`() = testApplication {
        // Setup
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

        val updatedSettings =
            InterpreterSettings(
                coreSize = 2048,
                instructionLimit = 500,
                initialInstruction = "ADD",
                maximumTicks = 100000,
                maximumProcessesPerPlayer = 16,
                readDistance = 100,
                writeDistance = 100,
                minimumSeparation = 50,
                separation = 50,
                randomSeparation = true,
            )
        clearAllMocks()

        // When...
        val response =
            client.post("/api/v1/lobby/$aLobbyId/settings") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(updatedSettings))
            }

        // Then...
        assertEquals(HttpStatusCode.OK, response.status)
        verify(exactly = 1) {
            saveLobby.saveLobby(match { it -> it.currentSettings == updatedSettings })
        }
    }

    @Test
    fun `trying to update settings for a non-existent lobby returns 404`() = testApplication {
        // Setup
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

        val updatedSettings =
            InterpreterSettings(
                coreSize = 2048,
                instructionLimit = 500,
                initialInstruction = "ADD",
                maximumTicks = 100000,
                maximumProcessesPerPlayer = 16,
                readDistance = 100,
                writeDistance = 100,
                minimumSeparation = 50,
                separation = 50,
                randomSeparation = true,
            )
        clearAllMocks()

        // When...
        val response =
            client.post("/api/v1/lobby/999/settings") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(updatedSettings))
            }

        // Then...
        assertEquals(HttpStatusCode.NotFound, response.status)
        verify(exactly = 0) { saveLobby.saveLobby(any()) }
    }

    @Test
    fun `trying to update settings with invalid setting values returns 400 and does not change settings`() =
        testApplication {
            // Setup
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
            val updateResponse =
                client.post("/api/v1/lobby/$aLobbyId/settings") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"coreSize\":\"invalid value :3\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.BadRequest, updateResponse.status)
            verify(exactly = 0) { saveLobby.saveLobby(any()) }
        }

    @Test
    fun `trying to update settings with invalid lobby id returns 400 and does not change settings`() =
        testApplication {
            // Setup
            application {
                basicModule()
                moduleApiV1()
            }

            // Given...
            val saveLobby = get<SaveLobbyPort>()
            val anInvalidLobbyId = -1L
            clearAllMocks()

            // When...
            val updateResponse =
                client.post("/api/v1/lobby/$anInvalidLobbyId/settings") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"coreSize\":\"128\"}")
                }

            // Then...
            assertEquals(HttpStatusCode.BadRequest, updateResponse.status)
            verify(exactly = 0) { saveLobby.saveLobby(any()) }
        }
}
