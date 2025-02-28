package software.shonk.lobby.application.service

import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.*
import software.shonk.lobby.adapters.incoming.getProgramFromPlayerInLobby.GetProgramFromPlayerInLobbyCommand
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException
import software.shonk.lobby.domain.exceptions.NoCodeForPlayerException
import software.shonk.lobby.domain.exceptions.PlayerNotInLobbyException

class GetProgramFromPlayerInLobbyServiceTest {

    lateinit var loadLobbyPort: LoadLobbyPort
    lateinit var saveLobbyPort: SaveLobbyPort
    lateinit var getProgramFromPlayerInLobbyService: GetProgramFromPlayerInLobbyService

    // The in-memory lobby management also serves as a kind of mock here.
    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        loadLobbyPort = lobbyManager
        saveLobbyPort = lobbyManager
        getProgramFromPlayerInLobbyService = GetProgramFromPlayerInLobbyService(loadLobbyPort)
    }

    @Test
    fun `get code from two players that submitted code in an existing lobby`() {
        // Given...
        saveLobbyPort.saveLobby(
            aLobby(
                A_VALID_LOBBY_ID,
                hashMapOf(
                    A_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                    ANOTHER_VALID_PLAYERNAME to ANOTHER_REDCODE_PROGRAM,
                ),
                joinedPlayers = mutableListOf(A_VALID_PLAYERNAME, ANOTHER_VALID_PLAYERNAME),
            )
        )

        // When...
        val playerAResponse =
            getProgramFromPlayerInLobbyService.getProgramFromPlayerInLobby(
                GetProgramFromPlayerInLobbyCommand(A_VALID_LOBBY_ID, A_VALID_PLAYERNAME)
            )
        val playerBResponse =
            getProgramFromPlayerInLobbyService.getProgramFromPlayerInLobby(
                GetProgramFromPlayerInLobbyCommand(A_VALID_LOBBY_ID, ANOTHER_VALID_PLAYERNAME)
            )

        // Then...
        assertEquals(A_REDCODE_PROGRAM, playerAResponse.getOrNull())
        assertEquals(ANOTHER_REDCODE_PROGRAM, playerBResponse.getOrNull())
    }

    @Test
    fun `get code from lobby with player who has not submitted anything throws NoCodeForPlayerException`() {
        // Given...
        saveLobbyPort.saveLobby(
            aLobby(A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
        )

        // When...
        val result =
            getProgramFromPlayerInLobbyService.getProgramFromPlayerInLobby(
                GetProgramFromPlayerInLobbyCommand(A_VALID_LOBBY_ID, A_VALID_PLAYERNAME)
            )

        // Then...
        assertTrue { result.isFailure }
        assertTrue { result.exceptionOrNull() is NoCodeForPlayerException }
    }

    @Test
    fun `get code from lobby with player who has joined throws PlayerNotInLobbyException`() {
        // Given...
        saveLobbyPort.saveLobby(
            aLobby(A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
        )

        // When...
        val result =
            getProgramFromPlayerInLobbyService.getProgramFromPlayerInLobby(
                GetProgramFromPlayerInLobbyCommand(A_VALID_LOBBY_ID, ANOTHER_VALID_PLAYERNAME)
            )

        // Then...
        assertTrue { result.isFailure }
        assertTrue { result.exceptionOrNull() is PlayerNotInLobbyException }
    }

    @Test
    fun `get code from lobby lobby that does not exist fails with LobbyNotFoundException`() {
        // Given...

        // When...
        val result =
            getProgramFromPlayerInLobbyService.getProgramFromPlayerInLobby(
                GetProgramFromPlayerInLobbyCommand(
                    A_LOBBY_ID_THAT_HAS_NOT_BEEN_CREATED,
                    A_VALID_PLAYERNAME,
                )
            )

        // Then...
        assertTrue { result.isFailure }
        assertTrue { result.exceptionOrNull() is LobbyNotFoundException }
    }
}
