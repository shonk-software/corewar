package software.shonk.lobby.adapters.incoming.joinLobby

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.lobby.adapters.incoming.addProgramToLobby.UNKNOWN_ERROR_MESSAGE
import software.shonk.lobby.application.port.incoming.JoinLobbyUseCase
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException
import software.shonk.lobby.domain.exceptions.PlayerAlreadyJoinedLobbyException

fun Route.configureJoinLobbyControllerV1() {

    val logger = LoggerFactory.getLogger("JoinLobbyControllerV1")
    val joinLobbyUseCase by inject<JoinLobbyUseCase>()

    @Serializable data class JoinLobbyBody(val playerName: String)

    /**
     * This endpoint is used to join an existing lobby with the desired playerName. The body must
     * contain the desired playerName. If the player is already in the lobby, the join operation is
     * aborted. Players who want to join must have a unique playerName. body: { "playername":
     * String, }
     *
     * Response 200: No special response body, join was accepted.
     *
     * Response 404: The lobby you are trying to join doesn't exist.
     *
     * Response 409: Someone already joined as that player. The slot is locked and the join
     * operation is aborted.
     */
    post("/lobby/{lobbyId}/join") {
        val lobbyId = call.parameters["lobbyId"]
        val joinLobbyBodyResult = runCatching { call.receive<JoinLobbyBody>() }
        joinLobbyBodyResult.onFailure {
            logger.error("Unable to extract parameters from request...", it)
            call.respond(HttpStatusCode.BadRequest, "Player name is missing")
            return@post
        }

        val joinLobbyBody = joinLobbyBodyResult.getOrThrow()
        val constructJoinLobbyCommandResult = runCatching {
            JoinLobbyCommand(lobbyId, joinLobbyBody.playerName)
        }

        constructJoinLobbyCommandResult.onFailure {
            when (it) {
                is IllegalArgumentException -> {
                    logger.error(
                        "Parameters for joinLobbyCommand construction failed basic validation...",
                        it,
                    )
                    call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
                    return@post
                }
            }
        }

        val joinLobbyResult =
            joinLobbyUseCase.joinLobby(constructJoinLobbyCommandResult.getOrThrow())

        joinLobbyResult.onSuccess { call.respond(HttpStatusCode.OK, it) }
        joinLobbyResult.onFailure {
            when (it) {
                is LobbyNotFoundException -> {
                    logger.error("Failed to join Lobby, requested lobby does not exist!", it)
                    call.respond(HttpStatusCode.NotFound, it.message ?: UNKNOWN_ERROR_MESSAGE)
                }
                is PlayerAlreadyJoinedLobbyException -> {
                    logger.error(
                        "Someone already joined as that player. The slot is locked and the join operation is aborted",
                        it,
                    )
                    call.respond(HttpStatusCode.Conflict, it.message ?: UNKNOWN_ERROR_MESSAGE)
                }
                else -> {
                    logger.error(
                        "Failed to join lobby, unknown error on service layer after passing command!",
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
