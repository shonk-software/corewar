package software.shonk.lobby.adapters.incoming.createLobby

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
import software.shonk.lobby.adapters.incoming.addProgramToLobby.UNKNOWN_ERROR_MESSAGE
import software.shonk.lobby.application.port.incoming.CreateLobbyUseCase

fun Route.configureCreateLobbyControllerV1() {
    val logger = LoggerFactory.getLogger("CreateLobbyControllerV1")
    val createLobbyUseCase by inject<CreateLobbyUseCase>()

    @Serializable data class CreateLobbyBody(val playerName: String)

    @Serializable data class CreateLobbyResponse(val lobbyId: String)

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
        val createLobbyBodyResult = runCatching { call.receive<CreateLobbyBody>() }
        createLobbyBodyResult.onFailure {
            logger.error("Unable to extract parameters from request...", it)
            call.respond(HttpStatusCode.BadRequest, "Player name is missing")
            return@post
        }

        val createLobbyBody = createLobbyBodyResult.getOrThrow()
        val buildCreateLobbyCommandResult = runCatching {
            CreateLobbyCommand(createLobbyBody.playerName)
        }
        buildCreateLobbyCommandResult.onFailure {
            when (it) {
                is IllegalArgumentException -> {
                    logger.error(
                        "Parameters for createLobbyCommand construction failed basic validation...",
                        it,
                    )
                    call.respond(HttpStatusCode.BadRequest, it.message ?: UNKNOWN_ERROR_MESSAGE)
                    return@post
                }
            }
        }

        val createLobbyResult =
            createLobbyUseCase.createLobby(buildCreateLobbyCommandResult.getOrThrow())

        createLobbyResult.onSuccess {
            call.respond(HttpStatusCode.Created, CreateLobbyResponse(it.toString()))
        }
        createLobbyResult.onFailure {
            when (it) {
                // Write Exception when we don't have any known Exceptions that throw here to
                // already have the structure
                // for when actual ones get added. Change the matching to be 'else' branch for 500
                // then
                is Exception -> {
                    logger.error(
                        "Failed to create lobby, unknown error on service layer after passing command!",
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
