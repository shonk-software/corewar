package software.shonk.lobby.adapters.incoming

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.interpreter.adapters.incoming.UNKNOWN_ERROR_MESSAGE
import software.shonk.lobby.application.port.incoming.GetLobbySettingsQuery

fun Route.configureGetLobbySettingsControllerV1() {

    val logger = LoggerFactory.getLogger("GetLobbySettingsControllerV1")
    val getLobbySettingsQuery by inject<GetLobbySettingsQuery>()

    get("/lobby/{lobbyId}/settings") {
        val lobbyId =
            call.parameters["lobbyId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

        val settingsResult =
            runCatching { GetLobbySettingsCommand(lobbyId) }
                .mapCatching { getLobbySettingsQuery.getLobbySettings(it) }

        settingsResult.onFailure {
            logger.error(
                "Failed to get settings from lobby, error on service layer after passing command!",
                it,
            )
            // todo change to 500 (or at least check if it makes sense, prob not...) just evaluate!
            call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
        }

        settingsResult.onSuccess { result ->
            result.onFailure {
                logger.error(
                    "Failed to get settings from lobby, parameters failed basic validation",
                    it,
                )
                call.respond(HttpStatusCode.NotFound, it.message ?: UNKNOWN_ERROR_MESSAGE)
            }

            result.onSuccess { settings ->
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("settings" to mapOf("interpreterSettings" to settings)),
                )
            }
        }
    }
}
