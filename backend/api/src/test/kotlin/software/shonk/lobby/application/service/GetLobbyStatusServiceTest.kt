package software.shonk.lobby.application.service

import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.*
import software.shonk.lobby.adapters.incoming.getLobbyStatus.GetLobbyStatusCommand
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.GetLobbyStatusQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException

class GetLobbyStatusServiceTest {

    private lateinit var getLobbyStatusQuery: GetLobbyStatusQuery
    private lateinit var loadLobbyPort: LoadLobbyPort
    private lateinit var saveLobbyPort: SaveLobbyPort

    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        loadLobbyPort = lobbyManager
        saveLobbyPort = lobbyManager
        getLobbyStatusQuery = GetLobbyStatusService(loadLobbyPort)
    }

    @Test
    fun `get status for the lobby fails if lobby does not exist and returns LobbyNotFoundException`() {
        // Given...

        // When...
        val result =
            getLobbyStatusQuery.getLobbyStatus(
                GetLobbyStatusCommand(A_LOBBY_ID_THAT_HAS_NOT_BEEN_CREATED, false)
            )

        // Then...
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is LobbyNotFoundException)
    }

    @Test
    fun `get lobby status for valid lobby with visualization data`() {
        // Given...
        saveLobbyPort.saveLobby(
            aLobbyThatHasAlreadyRun(
                id = A_VALID_LOBBY_ID,
                hashMapOf(
                    A_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                    ANOTHER_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                ),
                joinedPlayers = mutableListOf(A_VALID_PLAYERNAME, ANOTHER_VALID_PLAYERNAME),
            )
        )

        // When...
        val result =
            getLobbyStatusQuery
                .getLobbyStatus(GetLobbyStatusCommand(A_VALID_LOBBY_ID, true))
                .getOrThrow()

        // Then...
        assertTrue(result.visualizationData.isNotEmpty())
    }

    @Test
    fun `get lobby status without visualization data`() {
        // Given...
        saveLobbyPort.saveLobby(
            aLobbyThatHasAlreadyRun(
                id = A_VALID_LOBBY_ID,
                hashMapOf(
                    A_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                    ANOTHER_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                ),
                joinedPlayers = mutableListOf(A_VALID_PLAYERNAME, ANOTHER_VALID_PLAYERNAME),
            )
        )

        // When...
        val result =
            getLobbyStatusQuery
                .getLobbyStatus(GetLobbyStatusCommand(A_VALID_LOBBY_ID, false))
                .getOrThrow()

        // Then...
        assertTrue(result.visualizationData.isEmpty())
    }

    @Test
    fun `playerSubmitted is false when no programs are submitted`() {
        // Given...
        saveLobbyPort.saveLobby(
            aLobby(
                id = A_VALID_LOBBY_ID,
                joinedPlayers = mutableListOf(A_VALID_PLAYERNAME, ANOTHER_VALID_PLAYERNAME),
            )
        )

        // When...
        val result =
            getLobbyStatusQuery
                .getLobbyStatus(GetLobbyStatusCommand(A_VALID_LOBBY_ID, false))
                .getOrThrow()

        // Then...
        assertFalse(result.playerASubmitted)
        assertFalse(result.playerBSubmitted)
    }

    @Test
    fun `playerASubmitted is true when playerA has submitted code`() {
        // Given...
        saveLobbyPort.saveLobby(
            aLobbyThatHasAlreadyRun(
                id = A_VALID_LOBBY_ID,
                hashMapOf(PLAYER_A to A_REDCODE_PROGRAM),
                joinedPlayers = mutableListOf(A_VALID_PLAYERNAME, ANOTHER_VALID_PLAYERNAME),
            )
        )

        // When...
        val result =
            getLobbyStatusQuery
                .getLobbyStatus(GetLobbyStatusCommand(A_VALID_LOBBY_ID, false))
                .getOrThrow()

        // Then...
        assertTrue(result.playerASubmitted)
        assertFalse(result.playerBSubmitted)
    }

    @Test
    fun `playerBSubmitted is true when playerA has submitted code`() {
        // Given...
        saveLobbyPort.saveLobby(
            aLobbyThatHasAlreadyRun(
                id = A_VALID_LOBBY_ID,
                hashMapOf(PLAYER_B to A_REDCODE_PROGRAM),
                joinedPlayers = mutableListOf(A_VALID_PLAYERNAME, ANOTHER_VALID_PLAYERNAME),
            )
        )

        // When...
        val result =
            getLobbyStatusQuery
                .getLobbyStatus(GetLobbyStatusCommand(A_VALID_LOBBY_ID, false))
                .getOrThrow()

        // Then...
        assertFalse(result.playerASubmitted)
        assertTrue(result.playerBSubmitted)
    }
}
