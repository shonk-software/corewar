package software.shonk.interpreter.application.service

import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.application.port.incoming.GetCompilationErrorsQuery
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort

class GetCompilationErrorsServiceTest {

    private lateinit var getAllCompilationErrorsQuery: GetCompilationErrorsQuery
    private lateinit var saveLobbyPort: SaveLobbyPort

    // The in-memory lobby management also serves as a kind of mock here.
    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        saveLobbyPort = lobbyManager
        getAllCompilationErrorsQuery = GetCompilationErrorsService()
    }

    @Test
    fun `no code should return no errors`() {
        assertTrue { getAllCompilationErrorsQuery.getCompilationErrors("").isEmpty() }
    }

    // todo check some common error message
}
