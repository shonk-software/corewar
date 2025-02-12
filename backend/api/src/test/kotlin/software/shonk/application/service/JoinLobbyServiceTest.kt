package software.shonk.application.service

import io.mockk.spyk
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.adapters.outgoing.MemoryLobbyManager
import software.shonk.application.port.incoming.JoinLobbyUseCase
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort
import software.shonk.domain.Lobby
import software.shonk.interpreter.MockShork

class JoinLobbyServiceTest {

    private lateinit var joinLobbyUseCase: JoinLobbyUseCase
    private lateinit var loadLobbyPort: LoadLobbyPort
    private lateinit var saveLobbyPort: SaveLobbyPort

    // The in-memory lobby management also serves as a kind of mock here.
    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        loadLobbyPort = lobbyManager
        saveLobbyPort = lobbyManager
        joinLobbyUseCase = JoinLobbyService(loadLobbyPort, saveLobbyPort)
    }

    @Test
    fun `join lobby with valid playerName`() {
        val aLobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(aLobbyId, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerA"))
        )
        joinLobbyUseCase.joinLobby(aLobbyId, "playerB")

        assertEquals(
            true,
            loadLobbyPort.getLobby(aLobbyId).getOrNull()?.joinedPlayers?.contains("playerB"),
        )
    }

    @Test
    fun `join lobby with duplicate (invalid) playerName`() {
        val aLobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(aLobbyId, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerA"))
        )
        joinLobbyUseCase.joinLobby(aLobbyId, "playerA")

        assertEquals(1, loadLobbyPort.getLobby(aLobbyId).getOrNull()?.joinedPlayers?.size)
    }

    @Test
    fun `join nonexistent lobby`() {
        val result = joinLobbyUseCase.joinLobby(0L, "playerA")

        assertEquals(result.isFailure, true)
    }
}
