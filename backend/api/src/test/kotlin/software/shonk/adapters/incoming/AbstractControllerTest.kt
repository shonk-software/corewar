package software.shonk.adapters.incoming

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
import software.shonk.adapters.outgoing.MemoryLobbyManager
import software.shonk.application.port.incoming.*
import software.shonk.application.port.outgoing.DeleteLobbyPort
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort
import software.shonk.application.service.*
import software.shonk.interpreter.IShork
import software.shonk.interpreter.MockShork

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
                single<ShorkUseCase> { ShorkService(get(), get(), get()) }
                single<GetLobbySettingsQuery> { GetLobbySettingsService(get()) }
                single<GetCompilationErrorsQuery> { GetCompilationErrorsService() }
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
