package software.shonk

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.koin
import software.shonk.adapters.incoming.configureCreateLobbyControllerV1
import software.shonk.adapters.incoming.configureGetProgramFromPlayerInLobbyControllerV1
import software.shonk.adapters.incoming.configureShorkInterpreterControllerV1
import software.shonk.adapters.outgoing.MemoryLobbyManager
import software.shonk.application.port.incoming.CreateLobbyUseCase
import software.shonk.application.port.incoming.GetProgramFromPlayerInLobbyQuery
import software.shonk.application.port.incoming.ShorkUseCase
import software.shonk.application.port.outgoing.DeleteLobbyPort
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort
import software.shonk.application.service.CreateLobbyService
import software.shonk.application.service.GetProgramFromPlayerInLobbyService
import software.shonk.application.service.ShorkService
import software.shonk.interpreter.IShork
import software.shonk.interpreter.Shork

fun main() {
    embeddedServer(Netty, port = 8080) {
            basicModule()
            moduleApiV1()
            koinModule()
            staticResources()
        }
        .start(wait = true)
}

fun Application.basicModule() {
    // Install basic middleware like CORS and content negotiation here
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                useArrayPolymorphism = false
            }
        )
    }
}

fun Application.moduleApiV1() {
    routing {
        route("/api/v1") {
            staticResources("/docs", "openapi/v1", index = "scalar.html")
            configureShorkInterpreterControllerV1()
            configureCreateLobbyControllerV1()
            configureGetProgramFromPlayerInLobbyControllerV1()
        }
    }
}

fun Application.staticResources() {
    routing { staticResources("/resources", "static") }
}

fun Application.koinModule() {
    koin {
        modules(
            module {
                single<IShork> { Shork() }
                single<ShorkUseCase> { ShorkService(get(), get(), get()) }
                single<CreateLobbyUseCase> { CreateLobbyService(get(), get()) }
                single<GetProgramFromPlayerInLobbyQuery> {
                    GetProgramFromPlayerInLobbyService(get())
                }
                singleOf(::MemoryLobbyManager) {
                    bind<LoadLobbyPort>()
                    bind<SaveLobbyPort>()
                    bind<DeleteLobbyPort>()
                }
            }
        )
    }
}
