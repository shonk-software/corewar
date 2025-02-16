package software.shonk.lobby.adapters.incoming

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlin.getValue
import kotlin.text.toLongOrNull
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.interpreter.adapters.incoming.Program
import software.shonk.interpreter.adapters.incoming.UNKNOWN_ERROR_MESSAGE
import software.shonk.lobby.application.port.incoming.GetProgramFromPlayerInLobbyQuery

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
        val lobbyId =
            call.parameters["lobbyId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

        val program =
            runCatching { GetProgramFromPlayerInLobbyCommand(lobbyId, player) }
                .mapCatching { getProgramFromPlayerInLobbyQuery.getProgramFromPlayerInLobby(it) }

        program.onFailure {
            logger.error(
                "Failed to get program from player, error on service layer after passing command!",
                it,
            )
            // todo change to 500 (or at least check if it makes sense, prob not...) just evaluate!
            call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
        }

        program.onSuccess { result ->
            result.onFailure {
                logger.error(
                    "Failed to get program from lobby, parameters failed basic validation",
                    it,
                )
                call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
            }

            result.onSuccess { call.respond(Program(it)) }
        }
    }
}
