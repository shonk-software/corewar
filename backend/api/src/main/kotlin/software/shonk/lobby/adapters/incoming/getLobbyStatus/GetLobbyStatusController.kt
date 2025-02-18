package software.shonk.lobby.adapters.incoming.getLobbyStatus

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.lobby.adapters.incoming.addProgramToLobby.UNKNOWN_ERROR_MESSAGE
import software.shonk.lobby.application.port.incoming.GetLobbyStatusQuery
import software.shonk.lobby.domain.LobbyNotFoundException

fun Route.configureGetLobbyStatusControllerV1() {
    val logger = LoggerFactory.getLogger("GetLobbyStatusControllerV1")
    val getLobbyStatusQuery by inject<GetLobbyStatusQuery>()

    // todo proper integration testing
    /**
     * Path params:
     * - lobbyId: The id of the lobby, whose status you want to get.
     * - Type: number
     *
     * <br>
     *
     * Query params:
     * - showVisualizationData: Whether to include visualization data in the response.
     *     - Default: true
     *     - Type: boolean
     *
     * Returns either the status of the current game in a lobby or the status of the last finished
     * game in a lobby. Path parameter - {lobbyId}: The id of the lobby, which status you want to
     * get. Keep in mind, that in this api version there is no default lobby!
     *
     * playerXSubmitted will only be true if code for player X has been submitted.
     *
     * Result will only be valid when gameState is FINISHED, beforehand it is undefined.
     *
     * playerXSubmitted will switch back to false once the gameState switches to FINISHED and a
     * player in the lobby submits new code to the lobby.
     *
     * Response 400: The lobby id is invalid, so the id wasn't given as the path parameter OR the
     * lobby you are trying to get the status from, doesn't exist.
     *
     * Response 200: The GET request is valid and the lobby status including the visualization data
     * is returned.
     *
     * <br>
     *
     * response:
     *
     * <br>
     *
     * { "playerASubmitted": boolean, "playerBSubmitted": boolean, "gameState": One of
     * [NOT_STARTED, RUNNING, FINISHED], "result": { "winner": One of [A, B, DRAW], },
     * "visualizationData": [ { "playerId": One of [A, B], "programCounterBefore": number,
     * "programCounterAfter": number, "programCountersOfOtherProcesses": number, "memoryReads":
     * number, "memoryWrites": number, "processDied": boolean, } ] }
     */
    get("lobby/{lobbyId}/status") {
        val lobbyId =
            call.parameters["lobbyId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Unable to parse lobbyId!")

        val showVisualizationData =
            call.request.queryParameters["showVisualizationData"]?.toBoolean() != false

        val lobbyStatus =
            runCatching { GetLobbyStatusCommand(lobbyId, showVisualizationData) }
                .mapCatching { getLobbyStatusQuery.getLobbyStatus(it) }

        lobbyStatus.onFailure {
            when (it) {
                is IllegalArgumentException -> {
                    logger.error(
                        "Parameters for getLobbyStatus construction failed basic validation...",
                        it,
                    )
                    call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
                }
            }
        }

        lobbyStatus.onSuccess { result ->
            result.onSuccess { call.respond(HttpStatusCode.OK, it) }
            result.onFailure {
                when (it) {
                    is LobbyNotFoundException -> {
                        logger.error(
                            "Failed to add program to lobby, error on service layer after passing command!"
                        )
                        call.respond(HttpStatusCode.NotFound, it.message ?: UNKNOWN_ERROR_MESSAGE)
                    }
                }
            }
        }
    }
}
