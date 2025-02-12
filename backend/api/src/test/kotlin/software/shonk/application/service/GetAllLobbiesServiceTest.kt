package software.shonk.application.service

import io.mockk.spyk
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.adapters.outgoing.MemoryLobbyManager
import software.shonk.application.port.incoming.GetAllLobbiesQuery
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort
import software.shonk.domain.Lobby
import software.shonk.domain.LobbyStatus
import software.shonk.interpreter.MockShork

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
    fun `test get all lobbies`() {
        val aLobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(aLobbyId, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerA"))
        )
        val anotherLobbyId = 1L
        saveLobbyPort.saveLobby(
            Lobby(
                anotherLobbyId,
                hashMapOf(),
                MockShork(),
                joinedPlayers = mutableListOf("playerA"),
            )
        )
        val aThirdLobbyId = 2L
        saveLobbyPort.saveLobby(
            Lobby(aThirdLobbyId, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerA"))
        )

        val result = getAllLobbiesQuery.getAllLobbies().getOrNull()

        assertEquals(3, result?.size)
        assertTrue(
            result?.contains(
                LobbyStatus(
                    id = aLobbyId,
                    playersJoined = listOf("playerA"),
                    gameState = "NOT_STARTED",
                )
            ) ?: false
        )

        assertTrue(
            result?.contains(
                LobbyStatus(
                    id = anotherLobbyId,
                    playersJoined = listOf("playerA"),
                    gameState = "NOT_STARTED",
                )
            ) ?: false
        )

        assertTrue(
            result?.contains(
                LobbyStatus(
                    id = aThirdLobbyId,
                    playersJoined = listOf("playerA"),
                    gameState = "NOT_STARTED",
                )
            ) ?: false
        )
    }
}
