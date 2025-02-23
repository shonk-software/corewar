package software.shonk.lobby.adapters.incoming

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
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
import software.shonk.*
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.GetLobbySettingsQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.application.service.GetLobbySettingsService
import software.shonk.lobby.domain.InterpreterSettings

class GetLobbySettingsControllerIT : KoinTest {

    private val testModule = module {
        single<GetLobbySettingsQuery> { GetLobbySettingsService(get()) }
        singleOf(::MemoryLobbyManager) {
            bind<LoadLobbyPort>()
            bind<SaveLobbyPort>()
        }
    }

    @Serializable data class SettingsResponse(val settings: InterpreterSettings)

    @BeforeEach
    fun setup() {
        startKoin { modules(testModule) }
    }

    @AfterEach
    fun teardown() {
        stopKoin()
    }

    @Test
    fun `get lobby settings for an existing lobby returns 200 with those settings`() =
        testApplication {
            // Setup
            application {
                basicModule()
                moduleApiV1()
            }

            // Given...
            val saveLobby = get<SaveLobbyPort>()
            saveLobby.saveLobby(
                aLobby(id = A_VALID_LOBBY_ID, joinedPlayers = mutableListOf("playerA"))
            )

            // When...
            val response = client.get("/api/v1/lobby/$A_VALID_LOBBY_ID/settings")

            // Then...
            assertEquals(HttpStatusCode.OK, response.status)

            val settingsAnswer = Json.decodeFromString<SettingsResponse>(response.bodyAsText())
            val defaultSettings = InterpreterSettings()
            assertEquals(defaultSettings, settingsAnswer.settings)
        }

    @Test
    fun `get lobby settings for a non-existing lobby returns 404`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...

        // When...
        val response = client.get("/api/v1/lobby/$A_LOBBY_ID_THAT_HAS_NOT_BEEN_CREATED/settings")

        // Then ...
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `get lobby settings for an invalid lobby id returns 400`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...

        // When...
        val response = client.get("/api/v1/lobby/$AN_INVALID_LOBBY_ID/settings")

        // Then ...
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
