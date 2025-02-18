package software.shonk.lobby.adapters.incoming.addProgramToLobby

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.lobby.adapters.incoming.getLobbyStatus.GetLobbyStatusCommand
import software.shonk.lobby.application.port.incoming.AddProgramToLobbyUseCase
import software.shonk.lobby.application.port.incoming.GetLobbyStatusQuery
import software.shonk.lobby.domain.PlayerNameString

const val UNKNOWN_ERROR_MESSAGE = "Unknown Error"

fun Route.configureAddProgramToLobbyControllerV1() {
    val logger = LoggerFactory.getLogger("AddProgramToLobbyControllerV1")
    val getLobbyStatusQuery by inject<GetLobbyStatusQuery>()
    val addProgramToLobbyUseCase by inject<AddProgramToLobbyUseCase>()

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
        val lobbyId =
            call.parameters["lobbyId"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

        val playerName = call.parameters["player"]
        val submitCodeRequest = call.receive<SubmitCodeRequest>()

        // todo this can throw an exception for negative lobbyIds!!! try to catch and map to result
        if (
            getLobbyStatusQuery.getLobbyStatus(GetLobbyStatusCommand(lobbyId, false)).isFailure ||
                playerName == null
        ) {
            return@post call.respond(HttpStatusCode.NotFound)
        }

        val result =
            runCatching {
                    AddProgramToLobbyCommand(
                        lobbyId,
                        PlayerNameString(playerName),
                        submitCodeRequest.code,
                    )
                }
                .mapCatching { addProgramToLobbyUseCase.addProgramToLobby(it) }

        result.onFailure {
            logger.error(
                "Failed to add program to lobby, error on service layer after passing command!",
                it,
            )
            // todo change this to internal server error or at least re-evaluate
            call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
        }

        result.onSuccess { wrappedResult ->
            wrappedResult.onFailure {
                call.respond(HttpStatusCode.Forbidden, it.message ?: UNKNOWN_ERROR_MESSAGE)
            }
            wrappedResult.onSuccess { call.respond(HttpStatusCode.OK) }
        }
    }
}

@Serializable data class SubmitCodeRequest(val code: String)
