package software.shonk.lobby.adapters.incoming.getProgramFromPlayerInLobby

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlin.getValue
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.lobby.adapters.incoming.addProgramToLobby.UNKNOWN_ERROR_MESSAGE
import software.shonk.lobby.application.port.incoming.GetProgramFromPlayerInLobbyQuery
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException
import software.shonk.lobby.domain.exceptions.NoCodeForPlayerException
import software.shonk.lobby.domain.exceptions.PlayerNotInLobbyException

fun Route.configureGetProgramFromPlayerInLobbyControllerV1() {
    val logger = LoggerFactory.getLogger("GetProgramFromPlayerInLobbyControllerV1")
    val getProgramFromPlayerInLobbyQuery by inject<GetProgramFromPlayerInLobbyQuery>()

    /**
     * Returns the player program code from a lobby which was specified in the path parameters. Path
     * parameter - {lobbyId}: The lobby id from which the player code has to be gotten. Path
     * parameter - {player}: The player path variable has to be one of [A,B].
     *
     * Response 200: The body contains the player code from the player, which was specified in the
     * path. response: { "code": String, }
     *
     * Response 400: An incorrect player has been specified in the path/request. response: {
     * "message": String, }
     *
     * Response 404: If lobby doesn't exist.
     */
    get("/lobby/{lobbyId}/code/{player}") {
        val player = call.parameters["player"]
        val lobbyId = call.parameters["lobbyId"]

        val constructGetProgramFromPlayerInLobbyCommandResult = runCatching {
            require(player != null) { "Name must not be null" }
            GetProgramFromPlayerInLobbyCommand(lobbyId, player)
        }

        constructGetProgramFromPlayerInLobbyCommandResult.onFailure {
            logger.error(
                "Parameters for getProgramFromPlayerInLobbyCommand construction failed basic validation...",
                it,
            )
            call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
            return@get
        }

        val program =
            getProgramFromPlayerInLobbyQuery.getProgramFromPlayerInLobby(
                constructGetProgramFromPlayerInLobbyCommandResult.getOrThrow()
            )
        program.onSuccess { call.respond(Program(it)) }
        program.onFailure {
            when (it) {
                is LobbyNotFoundException -> {
                    logger.error("Failed to get settings from lobby, lobby does not exist")
                    call.respond(HttpStatusCode.NotFound, it.message ?: UNKNOWN_ERROR_MESSAGE)
                }
                // todo if player does not exist change to 404
                is PlayerNotInLobbyException -> {
                    logger.error(
                        "Failed to get code from player, player did not join lobby yet",
                        it,
                    )
                    call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
                }
                // todo if player did not submit code change to 404
                is NoCodeForPlayerException -> {
                    logger.error(
                        "Failed to get code from player, player did not submit any code in lobby yet",
                        it,
                    )
                    call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
                }
                else -> {
                    logger.error(
                        "Failed to get code, unknown error on service layer after passing command!",
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

@Serializable data class Program(val code: String)
