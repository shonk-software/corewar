package software.shonk.lobby.application.service

import io.mockk.spyk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.incoming.getLobbyStatus.GetLobbyStatusCommand
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.GetLobbyStatusQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.Lobby

class GetLobbyStatusServiceTest {

    private lateinit var getLobbyStatusQuery: GetLobbyStatusQuery
    private lateinit var loadLobbyPort: LoadLobbyPort
    private lateinit var saveLobbyPort: SaveLobbyPort

    // The in-memory lobby management also serves as a kind of mock here.
    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        loadLobbyPort = lobbyManager
        saveLobbyPort = lobbyManager
        getLobbyStatusQuery = GetLobbyStatusService(loadLobbyPort)
    }

    @Test
    fun `get status for the lobby fails if lobby does not exist`() {
        val result = getLobbyStatusQuery.getLobbyStatus(GetLobbyStatusCommand(0L, false))

        assertEquals(true, result.isFailure)
        assertEquals("No lobby with that id", result.exceptionOrNull()?.message)
    }

    @Test
    fun `get lobby status with visualization data`() {
        // Given...
        val lobbyId = 0L
        val lobby =
            Lobby(
                lobbyId,
                hashMapOf("playerA" to "MOV 0, 1", "playerB" to "MOV 0, 1"),
                MockShork(),
                joinedPlayers = mutableListOf("playerA", "playerB"),
            )
        lobby.run()
        saveLobbyPort.saveLobby(lobby)

        // When...
        val result =
            getLobbyStatusQuery.getLobbyStatus(GetLobbyStatusCommand(lobbyId, true)).getOrThrow()

        // Then...
        assertTrue(result.visualizationData.isNotEmpty())
    }

    @Test
    fun `get lobby status without visualization data`() {
        // Given...
        val lobbyId = 0L
        val lobby =
            Lobby(
                lobbyId,
                hashMapOf("playerA" to "MOV 0, 1", "playerB" to "MOV 0, 1"),
                MockShork(),
                joinedPlayers = mutableListOf("playerA", "playerB"),
            )
        lobby.run()
        saveLobbyPort.saveLobby(lobby)

        // When...
        val result =
            getLobbyStatusQuery.getLobbyStatus(GetLobbyStatusCommand(lobbyId, false)).getOrThrow()

        // Then...
        assertTrue(result.visualizationData.isEmpty())
    }
}
