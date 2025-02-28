package software.shonk.lobby.application.service

import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.*
import software.shonk.lobby.adapters.incoming.joinLobby.JoinLobbyCommand
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.JoinLobbyUseCase
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException
import software.shonk.lobby.domain.exceptions.PlayerAlreadyJoinedLobbyException

class JoinLobbyServiceTest {

    private lateinit var joinLobbyUseCase: JoinLobbyUseCase
    private lateinit var loadLobbyPort: LoadLobbyPort
    private lateinit var saveLobbyPort: SaveLobbyPort

    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        loadLobbyPort = lobbyManager
        saveLobbyPort = lobbyManager
        joinLobbyUseCase = JoinLobbyService(loadLobbyPort, saveLobbyPort)
    }

    @Test
    fun `join lobby with valid playerName updates lobby to now include the joined player`() {
        // Given...
        saveLobbyPort.saveLobby(
            aLobby(A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
        )
        clearAllMocks()

        // When...
        val result =
            joinLobbyUseCase.joinLobby(JoinLobbyCommand(A_VALID_LOBBY_ID, ANOTHER_VALID_PLAYERNAME))

        // Then...
        assertTrue(result.isSuccess)
        verify(exactly = 1) {
            saveLobbyPort.saveLobby(
                match { lobby ->
                    lobby.id == A_VALID_LOBBY_ID &&
                        lobby.joinedPlayers.contains(ANOTHER_VALID_PLAYERNAME)
                }
            )
        }
    }

    @Test
    fun `join lobby with duplicate playerName throws PlayerAlreadyJoinedLobbyException`() {
        // Given...
        saveLobbyPort.saveLobby(
            aLobby(A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
        )
        clearAllMocks()

        // When...
        val result =
            joinLobbyUseCase.joinLobby(JoinLobbyCommand(A_VALID_LOBBY_ID, A_VALID_PLAYERNAME))

        // Then...
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PlayerAlreadyJoinedLobbyException)
        verify(exactly = 0) {
            saveLobbyPort.saveLobby(match { lobby -> lobby.id == A_VALID_LOBBY_ID })
        }
    }

    @Test
    fun `trying to join a nonexistent lobby fails with LobbyNotFoundException`() {
        // Given...

        // When...
        val result =
            joinLobbyUseCase.joinLobby(
                JoinLobbyCommand(A_LOBBY_ID_THAT_HAS_NOT_BEEN_CREATED, A_VALID_PLAYERNAME)
            )

        // Then...
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is LobbyNotFoundException)
        verify(exactly = 0) {
            saveLobbyPort.saveLobby(match { lobby -> lobby.id == A_VALID_LOBBY_ID })
        }
    }
}
