package software.shonk.adapters.incoming

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.application.port.incoming.GetCompilationErrorsQuery
import software.shonk.application.port.incoming.ShorkUseCase
import software.shonk.domain.InterpreterSettings
import software.shonk.domain.toSettings

const val UNKNOWN_ERROR_MESSAGE = "Unknown Error"

fun Route.configureShorkInterpreterControllerV1() {
    val logger = LoggerFactory.getLogger("ShorkInterpreterControllerV1")
    val shorkUseCase by inject<ShorkUseCase>()
    val getCompilationErrorsQuery by inject<GetCompilationErrorsQuery>()

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
                ?: return@get call.respond(HttpStatusCode.BadRequest)

        val showVisualizationData =
            call.request.queryParameters["showVisualizationData"]?.toBoolean() ?: true

        val lobbyStatus = shorkUseCase.getLobbyStatus(lobbyId, showVisualizationData)
        lobbyStatus.onFailure {
            logger.error("No lobby with that id exists.")
            return@get call.respond(HttpStatusCode.NotFound, it.message ?: UNKNOWN_ERROR_MESSAGE)
        }
        lobbyStatus.onSuccess { call.respond(HttpStatusCode.OK, it) }
        return@get
    }

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

        val checkLobbyExists = shorkUseCase.getLobbyStatus(lobbyId)

        checkLobbyExists.onFailure {
            logger.error("The lobby you are trying to join doesn't exist", it)
            return@post call.respond(HttpStatusCode.NotFound)
        }

        @Serializable data class JoinLobbyBody(val playerName: String)

        val joinLobbyBody = call.receive<JoinLobbyBody>()

        val result = shorkUseCase.joinLobby(lobbyId, joinLobbyBody.playerName)
        result.onFailure {
            logger.error(
                "Someone already joined as that player. The slot is locked and the join operation is aborted",
                it,
            )
            return@post call.respond(HttpStatusCode.Conflict, it.message ?: UNKNOWN_ERROR_MESSAGE)
        }
        result.onSuccess { call.respond(HttpStatusCode.OK, it) }
    }

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

        if (shorkUseCase.getLobbyStatus(lobbyId).isFailure) {
            return@post call.respond(HttpStatusCode.NotFound)
        }

        val result = shorkUseCase.addProgramToLobby(lobbyId, playerName, submitCodeRequest.code)

        result.onFailure {
            call.respond(HttpStatusCode.Forbidden, it.message ?: UNKNOWN_ERROR_MESSAGE)
        }
        result.onSuccess { call.respond(HttpStatusCode.OK) }
        return@post
    }

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
            shorkUseCase.getAllLobbies().getOrElse {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    it.message ?: UNKNOWN_ERROR_MESSAGE,
                )
                return@get
            }
        call.respond(HttpStatusCode.OK, mapOf("lobbies" to lobbiesStatus))
        return@get
    }

    /**
     * Retrieves the errors encountered during the compilation of the redcode. If no errors were
     * encountered during compilation, the provided error array is empty. The body must contain the
     * code that is to be checked. body: { "code": String, }
     *
     * Response 200: The post operation was successful. <br> response: <br> { "errors":
     * [ { "line": number, "message": string, "columnStart": number, "columnEnd": number, }, ] }
     */
    post("/redcode/compile/errors") {
        @Serializable data class CompileErrorsRequest(val code: String)

        val compileErrorsRequest = call.receive<CompileErrorsRequest>()
        val errors = getCompilationErrorsQuery.getCompilationErrors(compileErrorsRequest.code)
        call.respond(mapOf("errors" to errors))
    }

    get("/lobby/{lobbyId}/settings") {
        val lobbyId =
            call.parameters["lobbyId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

        val settingsResult = shorkUseCase.getLobbySettings(lobbyId)

        settingsResult.onFailure {
            call.respond(HttpStatusCode.NotFound, it.message ?: UNKNOWN_ERROR_MESSAGE)
        }

        settingsResult.onSuccess { settings ->
            call.respond(
                HttpStatusCode.OK,
                mapOf("settings" to mapOf("interpreterSettings" to settings)),
            )
        }
        return@get
    }

    post("/lobby/{lobbyId}/settings") {
        val lobbyId =
            call.parameters["lobbyId"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

        val incomingSettings = call.receive<InterpreterSettings>()
        val newSettings = shorkUseCase.setLobbySettings(lobbyId, incomingSettings.toSettings())

        newSettings.onFailure {
            call.respond(HttpStatusCode.NotFound, it.message ?: UNKNOWN_ERROR_MESSAGE)
        }

        newSettings.onSuccess { call.respond(HttpStatusCode.OK, "Settings updated") }
    }
}

@Serializable data class Program(val code: String)

@Serializable data class SubmitCodeRequest(val code: String)
