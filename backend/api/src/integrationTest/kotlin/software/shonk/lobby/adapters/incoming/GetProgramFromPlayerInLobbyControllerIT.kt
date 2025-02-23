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
import software.shonk.*
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.GetProgramFromPlayerInLobbyQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.application.service.GetProgramFromPlayerInLobbyService

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
        saveLobby.saveLobby(
            aLobby(
                id = A_VALID_LOBBY_ID,
                programs = hashMapOf(A_VALID_PLAYERNAME to A_REDCODE_PROGRAM),
                joinedPlayers = mutableListOf(A_VALID_PLAYERNAME),
            )
        )
        clearAllMocks()

        // When...
        val result = client.get("/api/v1/lobby/$A_VALID_LOBBY_ID/code/$A_VALID_PLAYERNAME")

        // Then...
        assertEquals(HttpStatusCode.OK, result.status)
        val codeResponse = Json.decodeFromString<CodeResponse>(result.bodyAsText())
        assertEquals(A_REDCODE_PROGRAM, codeResponse.code)
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
            saveLobby.saveLobby(
                aLobby(id = A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
            )
            clearAllMocks()

            // When...
            val result = client.get("/api/v1/lobby/$A_VALID_LOBBY_ID/code/$A_VALID_PLAYERNAME")

            // Then...
            assertEquals(HttpStatusCode.BadRequest, result.status)
            assert(
                result
                    .bodyAsText()
                    .contains(
                        "Player $A_VALID_PLAYERNAME has not submitted any code in lobby $A_VALID_LOBBY_ID yet"
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
            saveLobby.saveLobby(
                aLobby(id = A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
            )
            clearAllMocks()

            // When...
            val result =
                client.get("/api/v1/lobby/$A_VALID_LOBBY_ID/code/$ANOTHER_VALID_PLAYERNAME")

            // Then...
            assertEquals(HttpStatusCode.BadRequest, result.status)
            assert(
                result
                    .bodyAsText()
                    .contains(
                        "Player $ANOTHER_VALID_PLAYERNAME has not joined lobby $A_VALID_LOBBY_ID yet"
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
        saveLobby.saveLobby(
            aLobby(
                id = A_VALID_LOBBY_ID,
                programs = hashMapOf(A_VALID_PLAYERNAME to ""),
                joinedPlayers = mutableListOf(A_VALID_PLAYERNAME),
            )
        )
        clearAllMocks()

        // When...
        val result = client.get("/api/v1/lobby/$A_VALID_LOBBY_ID/code/$A_VALID_PLAYERNAME")

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

        // When...
        val result = client.get("/api/v1/lobby/$AN_INVALID_LOBBY_ID/code/$A_VALID_PLAYERNAME")

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

        // When...
        val result =
            client.get(
                "/api/v1/lobby/$A_LOBBY_ID_THAT_HAS_NOT_BEEN_CREATED/code/$A_VALID_PLAYERNAME"
            )

        // Then...
        assertEquals(HttpStatusCode.NotFound, result.status)
    }
}
