package software.shonk.lobby.application.service

import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.ANOTHER_VALID_PLAYERNAME
import software.shonk.A_VALID_PLAYERNAME
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.incoming.createLobby.CreateLobbyCommand
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.Lobby

class CreateLobbyServiceTest {

    private lateinit var createLobbyService: CreateLobbyService
    private lateinit var saveLobbyPort: SaveLobbyPort

    // The in-memory lobby management also serves as a kind of mock here.
    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        saveLobbyPort = lobbyManager
        createLobbyService = CreateLobbyService(MockShork(), saveLobbyPort)
    }

    @Test
    fun `creating lobby containing playnerame when valid playername is given succeeds`() {
        // Given...

        // When...
        val result = createLobbyService.createLobby(CreateLobbyCommand(A_VALID_PLAYERNAME))

        // Then...
        assertTrue(result.isSuccess)
        verify(exactly = 1) {
            saveLobbyPort.saveLobby(match { it -> it.joinedPlayers.contains(A_VALID_PLAYERNAME) })
        }
    }

    @Test
    fun `two lobbies created after one another have different ids`() {
        // Given...
        val lobbySlot1 = slot<Lobby>()
        val lobbySlot2 = slot<Lobby>()

        // When...
        createLobbyService.createLobby(CreateLobbyCommand(A_VALID_PLAYERNAME))
        createLobbyService.createLobby(CreateLobbyCommand(ANOTHER_VALID_PLAYERNAME))

        // Then
        verify(exactly = 2) { saveLobbyPort.saveLobby(any()) }
        verifySequence {
            saveLobbyPort.saveLobby(capture(lobbySlot1))
            saveLobbyPort.saveLobby(capture(lobbySlot2))
        }
        assertNotEquals(lobbySlot1.captured.id, lobbySlot2.captured.id)
    }
}
