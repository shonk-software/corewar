package software.shonk.lobby.adapters.incoming

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlin.getValue
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.interpreter.adapters.incoming.UNKNOWN_ERROR_MESSAGE
import software.shonk.lobby.application.port.incoming.CreateLobbyUseCase

fun Route.configureCreateLobbyControllerV1() {
    val logger = LoggerFactory.getLogger("CreateLobbyControllerV1")
    val createLobbyUseCase by inject<CreateLobbyUseCase>()

    /**
     * Creates a new lobby and returns the id of the newly created one, which identifies the lobby
     * uniquely. If the playerName is invalid, the create operation is aborted. The body must
     * contain the desired playerName of the player creating the lobby. body: { "playerName":
     * String, }
     *
     * Response 201: The response contains the id of the created lobby. response: { "lobbyId":
     * String, }
     *
     * Response 400: Failed to create a lobby, because the playerName is invalid.
     */
    post("/lobby") {
        @Serializable data class CreateLobbyBody(val playerName: String)

        val createLobbyBody = call.receive<CreateLobbyBody>()

        val commandResult =
            runCatching { CreateLobbyCommand(createLobbyBody.playerName) }
                .mapCatching {
                    createLobbyUseCase.createLobby(it)
                } // Ensures exceptions here are caught

        commandResult.onFailure {
            logger.error(
                "Failed to create lobby, error on service layer after passing command!",
                it,
            )
            // todo change this to internal server error
            call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
        }
        commandResult.onSuccess { result ->
            result.onFailure {
                logger.error("Failed to create lobby, player name failed basic validation", it)
                call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
            }
            result.onSuccess {
                call.respond(HttpStatusCode.Created, CreateLobbyResponse(it.toString()))
            }
        }
    }
}

@Serializable data class CreateLobbyResponse(val lobbyId: String)
