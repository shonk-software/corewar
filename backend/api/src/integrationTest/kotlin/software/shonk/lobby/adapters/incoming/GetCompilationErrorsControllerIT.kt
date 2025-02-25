package software.shonk.lobby.adapters.incoming

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import software.shonk.A_REDCODE_PROGRAM
import software.shonk.basicModule
import software.shonk.interpreter.application.port.incoming.GetCompilationErrorsQuery
import software.shonk.interpreter.application.service.GetCompilationErrorsService
import software.shonk.interpreter.domain.CompileError
import software.shonk.moduleApiV1

class GetCompilationErrorsControllerIT : KoinTest {

    private val testModule = module {
        single<GetCompilationErrorsQuery> { GetCompilationErrorsService() }
    }

    @Serializable data class Program(val code: String)

    @Serializable data class ErrorsResponse(val errors: List<CompileError>)

    @BeforeEach
    fun setup() {
        startKoin { modules(testModule) }
    }

    @AfterEach
    fun teardown() {
        stopKoin()
    }

    @Test
    fun `missing code field in request body returns 400`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...

        // When...
        val result =
            client.post("/api/v1/redcode/compile/errors") {
                contentType(ContentType.Application.Json)
                setBody("{ \"invalid\" : \"json >:3c\"")
            }

        // Then...
        assertEquals(HttpStatusCode.BadRequest, result.status)
    }

    @Test
    fun `test compile no errors`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val client = createClient { install(ContentNegotiation) { json() } }

        // When...
        val result =
            client.post("/api/v1/redcode/compile/errors") {
                contentType(ContentType.Application.Json)
                setBody(Program(A_REDCODE_PROGRAM))
            }

        // Then...
        assertEquals(HttpStatusCode.OK, result.status)
        val errors = Json.decodeFromString<ErrorsResponse>(result.bodyAsText())
        assertTrue(errors.errors.isEmpty())
    }

    @Test
    fun `test compile with errors`() = testApplication {
        // Setup
        application {
            basicModule()
            moduleApiV1()
        }

        // Given...
        val client = createClient { install(ContentNegotiation) { json() } }

        // When...
        val result =
            client.post("/api/v1/redcode/compile/errors") {
                contentType(ContentType.Application.Json)
                setBody(Program("ASduhsdlfuhsdf dlfasuihfals Totally valid code :333"))
            }

        // Then...
        assertEquals(HttpStatusCode.OK, result.status)
        val errors = Json.decodeFromString<ErrorsResponse>(result.bodyAsText())
        assertTrue(errors.errors.isNotEmpty())
    }
}
