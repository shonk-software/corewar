package software.shonk.interpreter.adapters.incoming

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import software.shonk.interpreter.application.port.incoming.GetCompilationErrorsQuery

fun Route.configureGetCompilationErrorsControllerV1() {

    val getCompilationErrorsQuery by inject<GetCompilationErrorsQuery>()

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
}
