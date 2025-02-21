package software.shonk.lobby.adapters.incoming

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.clearAllMocks
import io.mockk.spyk
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
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.GetProgramFromPlayerInLobbyQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.application.service.GetProgramFromPlayerInLobbyService
import software.shonk.lobby.domain.Lobby
import software.shonk.moduleApiV1

class GetProgramFromPlayerInLobbyControllerIT : KoinTest {

    private val testModule = module {
        single<GetProgramFromPlayerInLobbyQuery> { GetProgramFromPlayerInLobbyService(get()) }
        val spy = spyk(MemoryLobbyManager())
        single { spy as LoadLobbyPort }
        single { spy as SaveLobbyPort }
    }

    @Serializable data class CodeResponse(val code: String)

    @BeforeEach
    fun setup() {
        startKoin { modules(testModule) }
    }

    @AfterEach
    fun teardown() {
        stopKoin()
    }

    @Test
    fun `requesting valid code submission returns 200 and the code`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val saveLobby = get<SaveLobbyPort>()
        val aLobbyId = 0L
        val aPlayerThatsInTheLobby = "playerA"
        val aValidProgram = "Mov 0, 1"
        // todo Testfactory?
        val aLobby =
            Lobby(
                id = aLobbyId,
                programs = hashMapOf(aPlayerThatsInTheLobby to aValidProgram),
                shork = MockShork(),
                joinedPlayers = mutableListOf(aPlayerThatsInTheLobby),
            )
        saveLobby.saveLobby(aLobby)
        clearAllMocks()

        // When...
        val result = client.get("/api/v1/lobby/$aLobbyId/code/$aPlayerThatsInTheLobby")

        // Then...
        assertEquals(HttpStatusCode.OK, result.status)
        val codeResponse = Json.decodeFromString<CodeResponse>(result.bodyAsText())
        assertEquals(aValidProgram, codeResponse.code)
    }

    @Test
    fun `trying to get the code from a player thats in the lobby but did not submit code yet returns 400`() =
        testApplication {
            // Setup
            application {
                basicModule()
                moduleApiV1()
            }

            // Given...
            val saveLobby = get<SaveLobbyPort>()
            val aLobbyId = 0L
            val aPlayerThatsInTheLobby = "playerA"
            // todo Testfactory?
            val aLobby =
                Lobby(
                    id = aLobbyId,
                    programs = hashMapOf(),
                    shork = MockShork(),
                    joinedPlayers = mutableListOf(aPlayerThatsInTheLobby),
                )
            saveLobby.saveLobby(aLobby)
            clearAllMocks()

            // When...
            val result = client.get("/api/v1/lobby/$aLobbyId/code/$aPlayerThatsInTheLobby")

            // Then...
            assertEquals(HttpStatusCode.BadRequest, result.status)
            assert(
                result
                    .bodyAsText()
                    .contains(
                        "Player $aPlayerThatsInTheLobby has not submitted any code in lobby $aLobbyId yet"
                    )
            )
        }

    @Test
    fun `trying to get the code from a player thats not in the lobby returns 400`() =
        testApplication {
            // Setup
            application {
                basicModule()
                moduleApiV1()
            }

            // Given...
            val saveLobby = get<SaveLobbyPort>()
            val aLobbyId = 0L
            val aPlayerThatsInTheLobby = "playerA"
            val aPlayerThatsNotInTheLobby = "playerB"
            // todo Testfactory?
            val aLobby =
                Lobby(
                    id = aLobbyId,
                    programs = hashMapOf(),
                    shork = MockShork(),
                    joinedPlayers = mutableListOf(aPlayerThatsInTheLobby),
                )
            saveLobby.saveLobby(aLobby)
            clearAllMocks()

            // When...
            val result = client.get("/api/v1/lobby/$aLobbyId/code/$aPlayerThatsNotInTheLobby")

            // Then...
            assertEquals(HttpStatusCode.BadRequest, result.status)
            assert(
                result
                    .bodyAsText()
                    .contains(
                        "Player $aPlayerThatsNotInTheLobby has not joined lobby $aLobbyId yet"
                    )
            )
        }

    @Test
    fun `requesting empty code submission returns 200`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val saveLobby = get<SaveLobbyPort>()
        val aLobbyId = 0L
        val aPlayerThatsInTheLobby = "playerA"
        // todo Testfactory?
        val aLobby =
            Lobby(
                id = aLobbyId,
                programs = hashMapOf(aPlayerThatsInTheLobby to ""),
                shork = MockShork(),
                joinedPlayers = mutableListOf(aPlayerThatsInTheLobby),
            )
        saveLobby.saveLobby(aLobby)
        clearAllMocks()

        // When...
        val result = client.get("/api/v1/lobby/$aLobbyId/code/$aPlayerThatsInTheLobby")

        // Then...
        assertEquals(HttpStatusCode.OK, result.status)
    }

    @Test
    fun `requesting code from an invalid lobby returns 400`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val anInvalidLobbyId = -1L

        // When...
        val result = client.get("/api/v1/lobby/$anInvalidLobbyId/code/playerA")

        // Then...
        assertEquals(HttpStatusCode.BadRequest, result.status)
    }

    @Test
    fun `requesting code from an a lobby that does not exist returns 404`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val aLobbyThatDoesNotExist = 0L

        // When...
        val result = client.get("/api/v1/lobby/$aLobbyThatDoesNotExist/code/playerA")

        // Then...
        assertEquals(HttpStatusCode.NotFound, result.status)
    }
}
