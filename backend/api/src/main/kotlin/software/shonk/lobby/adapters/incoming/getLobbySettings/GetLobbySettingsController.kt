package software.shonk.lobby.adapters.incoming.getLobbySettings

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.lobby.adapters.incoming.addProgramToLobby.UNKNOWN_ERROR_MESSAGE
import software.shonk.lobby.application.port.incoming.GetLobbySettingsQuery
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException

fun Route.configureGetLobbySettingsControllerV1() {

    val logger = LoggerFactory.getLogger("GetLobbySettingsControllerV1")
    val getLobbySettingsQuery by inject<GetLobbySettingsQuery>()

    get("/lobby/{lobbyId}/settings") {
        val lobbyId = call.parameters["lobbyId"]
        val constructGetLobbySettingsCommandResult = runCatching {
            GetLobbySettingsCommand(lobbyId)
        }
        constructGetLobbySettingsCommandResult.onFailure {
            logger.error(
                "Parameters for createLobbyCommand construction failed basic validation...",
                it,
            )
            call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
            return@get
        }

        val settingsResult =
            getLobbySettingsQuery.getLobbySettings(
                constructGetLobbySettingsCommandResult.getOrThrow()
            )
        settingsResult.onSuccess { settings ->
            call.respond(HttpStatusCode.OK, mapOf("settings" to settings))
        }
        settingsResult.onFailure {
            when (it) {
                is LobbyNotFoundException -> {
                    logger.error("Failed to get settings from lobby, lobby does not exist", it)
                    call.respond(HttpStatusCode.NotFound, it.message ?: UNKNOWN_ERROR_MESSAGE)
                }
                else -> {
                    logger.error(
                        "Failed to get lobby status, unknown error on service layer after passing command!",
                        it,
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
