package software.shonk.lobby.adapters.incoming.setLobbySettings

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlin.getValue
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.lobby.adapters.incoming.addProgramToLobby.UNKNOWN_ERROR_MESSAGE
import software.shonk.lobby.application.port.incoming.SetLobbySettingsUseCase
import software.shonk.lobby.domain.InterpreterSettings
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException

fun Route.configureSetLobbySettingsControllerV1() {

    val logger = LoggerFactory.getLogger("SetLobbySettingsControllerV1")
    val setLobbySettingsUseCase by inject<SetLobbySettingsUseCase>()

    post("/lobby/{lobbyId}/settings") {
        val incomingSettingsResult = runCatching { call.receive<InterpreterSettings>() }
        incomingSettingsResult.onFailure {
            logger.error("Unable to extract parameters from request...", it)
            call.respond(HttpStatusCode.BadRequest, "Unable to parse settings from request")
            return@post
        }
        val incomingSettings = incomingSettingsResult.getOrThrow()

        val lobbyId = call.parameters["lobbyId"]
        val constructSetLobbySettingsCommandResult = runCatching {
            SetLobbySettingsCommand(lobbyId, incomingSettings)
        }

        constructSetLobbySettingsCommandResult.onFailure {
            logger.error(
                "Parameters for setLobbySettingsCommand construction failed basic validation...",
                it,
            )
            call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
            return@post
        }

        val didSetNewSettingsResult =
            setLobbySettingsUseCase.setLobbySettings(
                constructSetLobbySettingsCommandResult.getOrThrow()
            )
        didSetNewSettingsResult.onSuccess { call.respond(HttpStatusCode.OK, "Settings updated") }
        didSetNewSettingsResult.onFailure {
            when (it) {
                is LobbyNotFoundException -> {
                    logger.error("Failed to get settings from lobby, lobby does not exist")
                    call.respond(HttpStatusCode.NotFound, it.message ?: UNKNOWN_ERROR_MESSAGE)
                }
                else -> {
                    logger.error(
                        "Failed to set lobby settings, unknown error on service layer after passing command!"
                    )
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        it.message ?: UNKNOWN_ERROR_MESSAGE,
                    )
                }
            }
        }
    }
}
