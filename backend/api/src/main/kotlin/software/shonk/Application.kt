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
import software.shonk.interpreter.IShork
import software.shonk.interpreter.Shork
import software.shonk.interpreter.adapters.incoming.configureGetCompilationErrorsControllerV1
import software.shonk.interpreter.application.port.incoming.GetCompilationErrorsQuery
import software.shonk.interpreter.application.service.GetCompilationErrorsService
import software.shonk.lobby.adapters.incoming.addProgramToLobby.configureAddProgramToLobbyControllerV1
import software.shonk.lobby.adapters.incoming.createLobby.configureCreateLobbyControllerV1
import software.shonk.lobby.adapters.incoming.getAllLobbies.configureGetAllLobbiesControllerV1
import software.shonk.lobby.adapters.incoming.getLobbySettings.configureGetLobbySettingsControllerV1
import software.shonk.lobby.adapters.incoming.getLobbyStatus.configureGetLobbyStatusControllerV1
import software.shonk.lobby.adapters.incoming.getProgramFromPlayerInLobby.configureGetProgramFromPlayerInLobbyControllerV1
import software.shonk.lobby.adapters.incoming.joinLobby.configureJoinLobbyControllerV1
import software.shonk.lobby.adapters.incoming.setLobbySettings.configureSetLobbySettingsControllerV1
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.AddProgramToLobbyUseCase
import software.shonk.lobby.application.port.incoming.CreateLobbyUseCase
import software.shonk.lobby.application.port.incoming.GetAllLobbiesQuery
import software.shonk.lobby.application.port.incoming.GetLobbySettingsQuery
import software.shonk.lobby.application.port.incoming.GetLobbyStatusQuery
import software.shonk.lobby.application.port.incoming.GetProgramFromPlayerInLobbyQuery
import software.shonk.lobby.application.port.incoming.JoinLobbyUseCase
import software.shonk.lobby.application.port.incoming.SetLobbySettingsUseCase
import software.shonk.lobby.application.port.outgoing.DeleteLobbyPort
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.application.service.AddProgramToLobbyService
import software.shonk.lobby.application.service.CreateLobbyService
import software.shonk.lobby.application.service.GetAllLobbiesService
import software.shonk.lobby.application.service.GetLobbySettingsService
import software.shonk.lobby.application.service.GetLobbyStatusService
import software.shonk.lobby.application.service.GetProgramFromPlayerInLobbyService
import software.shonk.lobby.application.service.JoinLobbyService
import software.shonk.lobby.application.service.SetLobbySettingsService

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
            configureAddProgramToLobbyControllerV1()
            configureCreateLobbyControllerV1()
            configureGetProgramFromPlayerInLobbyControllerV1()
            configureGetCompilationErrorsControllerV1()
            configureSetLobbySettingsControllerV1()
            configureGetLobbySettingsControllerV1()
            configureGetAllLobbiesControllerV1()
            configureJoinLobbyControllerV1()
            configureGetLobbyStatusControllerV1()
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
                single<CreateLobbyUseCase> { CreateLobbyService(get(), get()) }
                single<SetLobbySettingsUseCase> { SetLobbySettingsService(get(), get()) }
                single<GetProgramFromPlayerInLobbyQuery> {
                    GetProgramFromPlayerInLobbyService(get())
                }
                single<GetLobbySettingsQuery> { GetLobbySettingsService(get()) }
                single<GetCompilationErrorsQuery> { GetCompilationErrorsService() }
                single<GetAllLobbiesQuery> { GetAllLobbiesService(get()) }
                single<JoinLobbyUseCase> { JoinLobbyService(get(), get()) }
                single<GetLobbyStatusQuery> { GetLobbyStatusService(get()) }
                single<AddProgramToLobbyUseCase> { AddProgramToLobbyService(get(), get()) }
                singleOf(::MemoryLobbyManager) {
                    bind<LoadLobbyPort>()
                    bind<SaveLobbyPort>()
                    bind<DeleteLobbyPort>()
                }
            }
        )
    }
}
