package software.shonk.lobby.adapters.incoming.getAllLobbies

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import software.shonk.lobby.adapters.incoming.addProgramToLobby.UNKNOWN_ERROR_MESSAGE
import software.shonk.lobby.application.port.incoming.GetAllLobbiesQuery

fun Route.configureGetAllLobbiesControllerV1() {

    val getAllLobbiesQuery by inject<GetAllLobbiesQuery>()

    /**
     * Gets a list of all existing lobbies and their details. The playersJoined attribute contains a
     * list of all playerNames that joined the lobby. Joined means joined, not code submitted!
     *
     * Response 200: The post operation was successful. response: <br> { "lobbies":
     * [ { "id": number, "playersJoined": string, "gameState": One of [NOT_STARTED, RUNNING, FINISHED],
     * }, ... ] }
     */
    get("/lobby") {
        val lobbiesStatus =
            getAllLobbiesQuery.getAllLobbies().getOrElse {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    it.message ?: UNKNOWN_ERROR_MESSAGE,
                )
                return@get
            }
        call.respond(HttpStatusCode.OK, mapOf("lobbies" to lobbiesStatus))
        return@get
    }
}
