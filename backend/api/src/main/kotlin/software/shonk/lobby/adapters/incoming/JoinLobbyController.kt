package software.shonk.lobby.adapters.incoming

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.interpreter.adapters.incoming.UNKNOWN_ERROR_MESSAGE
import software.shonk.lobby.application.port.incoming.GetLobbyStatusQuery
import software.shonk.lobby.application.port.incoming.JoinLobbyUseCase

fun Route.configureJoinLobbyControllerV1() {

    val logger = LoggerFactory.getLogger("JoinLobbyControllerV1")
    val getLobbyStatusQuery by inject<GetLobbyStatusQuery>()
    val joinLobbyUseCase by inject<JoinLobbyUseCase>()

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
        val lobbyId =
            call.parameters["lobbyId"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

        // todo this should be in the service
        val checkLobbyExists = getLobbyStatusQuery.getLobbyStatus(lobbyId)

        checkLobbyExists.onFailure {
            logger.error("The lobby you are trying to join doesn't exist", it)
            return@post call.respond(HttpStatusCode.NotFound)
        }

        @Serializable data class JoinLobbyBody(val playerName: String)

        val joinLobbyBody = call.receive<JoinLobbyBody>()

        val joinLobbyResult =
            runCatching { JoinLobbyCommand(lobbyId, joinLobbyBody.playerName) }
                .mapCatching { joinLobbyUseCase.joinLobby(it) }

        joinLobbyResult.onFailure {
            logger.error("Failed to join lobby, error on service layer after passing command!", it)
            // todo change this to internal server error or at least re-evaluate
            call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
        }

        joinLobbyResult.onSuccess { result ->
            result.onFailure {
                logger.error(
                    "Someone already joined as that player. The slot is locked and the join operation is aborted",
                    it,
                )
                call.respond(HttpStatusCode.Conflict, it.message ?: UNKNOWN_ERROR_MESSAGE)
            }
            result.onSuccess { call.respond(HttpStatusCode.OK, it) }
        }
    }
}
