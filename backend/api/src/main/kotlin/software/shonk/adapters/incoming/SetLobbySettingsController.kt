package software.shonk.adapters.incoming

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlin.getValue
import kotlin.text.toLongOrNull
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.application.port.incoming.SetLobbySettingsUseCase
import software.shonk.domain.InterpreterSettings

fun Route.configureSetLobbySettingsControllerV1() {
    val logger = LoggerFactory.getLogger("SetLobbySettingsControllerV1")
    val setLobbySettingsUseCase by inject<SetLobbySettingsUseCase>()

    post("/lobby/{lobbyId}/settings") {
        val lobbyId =
            call.parameters["lobbyId"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

        val incomingSettings = call.receive<InterpreterSettings>()
        val newSettings =
            runCatching { SetLobbySettingsCommand(lobbyId, incomingSettings) }
                .mapCatching { setLobbySettingsUseCase.setLobbySettings(it) }

        newSettings.onFailure {
            logger.error(
                "Failed to set lobby settings, error on service layer after passing command!",
                it,
            )
            // todo change this to internal server error
            call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
        }
        newSettings.onSuccess { result ->
            result.onFailure {
                call.respond(HttpStatusCode.NotFound, it.message ?: UNKNOWN_ERROR_MESSAGE)
            }

            result.onSuccess { call.respond(HttpStatusCode.OK, "Settings updated") }
        }
    }
}
