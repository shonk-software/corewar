package software.shonk.lobby.adapters.incoming

import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import software.shonk.interpreter.IShork
import software.shonk.interpreter.MockShork
import software.shonk.interpreter.application.port.incoming.GetCompilationErrorsQuery
import software.shonk.interpreter.application.service.GetCompilationErrorsService
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

abstract class AbstractControllerTest() : KoinTest {
    lateinit var testEngine: TestApplicationEngine

    @BeforeEach
    fun beforeEach() {
        initializeTestEngine()
    }

    private fun initializeTestEngine() {
        testEngine =
            TestApplicationEngine(
                createTestEnvironment {
                    config =
                        MapApplicationConfig(
                            "ktor.environment" to "test",
                            "ktor.deployments.port" to "8080",
                        )
                }
            )
        testEngine.start(wait = false)
        applyTestEngineApplication()
        configureCustomDI(
            module {
                single<IShork> { MockShork() }
                single<CreateLobbyUseCase> { CreateLobbyService(get(), get()) }
                single<SetLobbySettingsUseCase> { SetLobbySettingsService(get(), get()) }
                single<GetProgramFromPlayerInLobbyQuery> {
                    GetProgramFromPlayerInLobbyService(get())
                }
                single<AddProgramToLobbyUseCase> { AddProgramToLobbyService(get(), get()) }
                single<JoinLobbyUseCase> { JoinLobbyService(get(), get()) }
                single<GetLobbySettingsQuery> { GetLobbySettingsService(get()) }
                single<GetCompilationErrorsQuery> { GetCompilationErrorsService() }
                single<GetAllLobbiesQuery> { GetAllLobbiesService(get()) }
                single<GetLobbyStatusQuery> { GetLobbyStatusService(get()) }
                singleOf(::MemoryLobbyManager) {
                    bind<LoadLobbyPort>()
                    bind<SaveLobbyPort>()
                    bind<DeleteLobbyPort>()
                }
            }
        )
    }

    abstract fun applyTestEngineApplication()

    open fun configureCustomDI(module: Module) {
        testEngine.application.apply {
            stopKoin()
            startKoin { modules(module) }
        }
    }

    fun runTest(testBlock: suspend TestApplicationEngine.() -> Unit) {
        with(testEngine) { runBlocking { testBlock() } }
    }
}
