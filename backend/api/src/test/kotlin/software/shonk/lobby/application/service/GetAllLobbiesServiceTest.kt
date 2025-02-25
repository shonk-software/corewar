package software.shonk.lobby.application.service

import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.*
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.GetAllLobbiesQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.LobbyStatus

class GetAllLobbiesServiceTest {

    private lateinit var getAllLobbiesQuery: GetAllLobbiesQuery
    private lateinit var loadLobbyPort: LoadLobbyPort
    private lateinit var saveLobbyPort: SaveLobbyPort

    // The in-memory lobby management also serves as a kind of mock here.
    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        loadLobbyPort = lobbyManager
        saveLobbyPort = lobbyManager
        getAllLobbiesQuery = GetAllLobbiesService(loadLobbyPort)
    }

    @Test
    fun `get all lobbies returns all existing lobbies`() {
        // Given...
        saveLobbyPort.saveLobby(
            aLobby(id = A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
        )
        saveLobbyPort.saveLobby(
            aLobby(id = ANOTHER_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
        )
        saveLobbyPort.saveLobby(
            aLobby(id = A_THIRD_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
        )

        // When
        val result = getAllLobbiesQuery.getAllLobbies().getOrNull()

        // Then...
        assertEquals(3, result?.size)
        assertTrue(
            result?.contains(
                LobbyStatus(
                    id = A_VALID_LOBBY_ID,
                    playersJoined = listOf(A_VALID_PLAYERNAME),
                    gameState = "NOT_STARTED",
                )
            ) ?: false
        )

        assertTrue(
            result?.contains(
                LobbyStatus(
                    id = ANOTHER_VALID_LOBBY_ID,
                    playersJoined = listOf(A_VALID_PLAYERNAME),
                    gameState = "NOT_STARTED",
                )
            ) ?: false
        )

        assertTrue(
            result?.contains(
                LobbyStatus(
                    id = A_THIRD_VALID_LOBBY_ID,
                    playersJoined = listOf(A_VALID_PLAYERNAME),
                    gameState = "NOT_STARTED",
                )
            ) ?: false
        )
    }
}
