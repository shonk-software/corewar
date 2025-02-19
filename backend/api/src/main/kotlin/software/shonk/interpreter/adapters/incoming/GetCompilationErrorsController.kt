package software.shonk.interpreter.adapters.incoming

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import software.shonk.interpreter.application.port.incoming.GetCompilationErrorsQuery

fun Route.configureGetCompilationErrorsControllerV1() {

    val logger = LoggerFactory.getLogger("GetCompilationErrorsControllerV1")
    val getCompilationErrorsQuery by inject<GetCompilationErrorsQuery>()

    @Serializable data class CompileErrorsRequest(val code: String)

    /**
     * Retrieves the errors encountered during the compilation of the redcode. If no errors were
     * encountered during compilation, the provided error array is empty. The body must contain the
     * code that is to be checked. body: { "code": String, }
     *
     * Response 200: The post operation was successful. <br> response: <br> { "errors":
     * [ { "line": number, "message": string, "columnStart": number, "columnEnd": number, }, ] }
     *
     * Response 400: If "code" key is missing entirely
     */
    post("/redcode/compile/errors") {
        val compileErrorsBodyResult = runCatching { call.receive<CompileErrorsRequest>() }
        compileErrorsBodyResult.onFailure {
            logger.error("Unable to extract parameters from request...", it)
            call.respond(HttpStatusCode.BadRequest, "Code is missing")
            return@post
        }

        val compilerErrors = compileErrorsBodyResult.getOrThrow()
        val errors = getCompilationErrorsQuery.getCompilationErrors(compilerErrors.code)
        call.respond(mapOf("errors" to errors))
    }
}
