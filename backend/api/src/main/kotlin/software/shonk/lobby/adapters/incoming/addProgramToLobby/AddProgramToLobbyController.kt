package software.shonk.lobby.adapters.incoming.addProgramToLobby

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.lobby.application.port.incoming.AddProgramToLobbyUseCase
import software.shonk.lobby.domain.exceptions.LobbyAlreadyCompletedException
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException
import software.shonk.lobby.domain.exceptions.PlayerNotInLobbyException

const val UNKNOWN_ERROR_MESSAGE = "Unknown Error"

fun Route.configureAddProgramToLobbyControllerV1() {

    val logger = LoggerFactory.getLogger("AddProgramToLobbyControllerV1")
    val addProgramToLobbyUseCase by inject<AddProgramToLobbyUseCase>()

    @Serializable data class SubmitCodeRequest(val code: String)

    /**
     * Posts the player code to a specific lobby, which is specified in the path parameters. If the
     * gamestate of the game in the specified lobby is FINISHED, then the lobby gets reset, so a new
     * game can be played and the new code gets submitted correctly. Path parameter - {lobbyId}: The
     * lobby id which the player code is to be submitted. Path parameter - {player}: The player path
     * variable has to be one of [A,B].
     *
     * The body must contain the code that is to be submitted. body: { "code": String, }
     *
     * Response 201: The post operation was successful.
     *
     * Response 400: An incorrect player has been specified in the path/request. body: { "message":
     * String, }
     *
     * Response 404: The lobby doesn't exist.
     */
    post("/lobby/{lobbyId}/code/{player}") {
        val lobbyId = call.parameters["lobbyId"]
        val playerName = call.parameters["player"]
        val submitCodeRequestResult = runCatching { call.receive<SubmitCodeRequest>() }
        submitCodeRequestResult.onFailure {
            logger.error("Unable to extract parameters from request...", it)
            call.respond(HttpStatusCode.BadRequest, "Code is missing")
            return@post
        }

        val submitCodeRequest = submitCodeRequestResult.getOrThrow()
        val constructAddProgramToLobbyCommandResult = runCatching {
            AddProgramToLobbyCommand(lobbyId, playerName, submitCodeRequest.code)
        }

        constructAddProgramToLobbyCommandResult.onFailure {
            logger.error(
                "Parameters for addProgramToLobbyCommand construction failed basic validation...",
                it,
            )
            call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
            return@post
        }

        val result =
            addProgramToLobbyUseCase.addProgramToLobby(
                constructAddProgramToLobbyCommandResult.getOrThrow()
            )
        result.onSuccess { call.respond(HttpStatusCode.OK) }
        result.onFailure {
            when (it) {
                is LobbyNotFoundException -> {
                    logger.error("Failed to add program to lobby, lobby does not exist")
                    call.respond(HttpStatusCode.NotFound, it.message ?: UNKNOWN_ERROR_MESSAGE)
                }
                // todo add to docs
                is PlayerNotInLobbyException -> {
                    logger.error(
                        "Failed to add program to player, player did not join lobby yet",
                        it,
                    )
                    call.respond(HttpStatusCode.Forbidden, it.message ?: UNKNOWN_ERROR_MESSAGE)
                }
                is LobbyAlreadyCompletedException -> {
                    logger.error("Failed to add program to lobby, lobby already completed")
                    call.respond(HttpStatusCode.Forbidden, it.message ?: UNKNOWN_ERROR_MESSAGE)
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
