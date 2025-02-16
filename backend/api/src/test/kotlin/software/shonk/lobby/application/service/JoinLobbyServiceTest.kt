package software.shonk.lobby.application.service

import io.mockk.spyk
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.incoming.JoinLobbyCommand
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.JoinLobbyUseCase
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.Lobby

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
        joinLobbyUseCase.joinLobby(JoinLobbyCommand(aLobbyId, "playerB"))

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
        joinLobbyUseCase.joinLobby(JoinLobbyCommand(aLobbyId, "playerA"))

        assertEquals(1, loadLobbyPort.getLobby(aLobbyId).getOrNull()?.joinedPlayers?.size)
    }

    @Test
    fun `join nonexistent lobby`() {
        val result = joinLobbyUseCase.joinLobby(JoinLobbyCommand(0L, "playerA"))

        assertEquals(result.isFailure, true)
    }
}
