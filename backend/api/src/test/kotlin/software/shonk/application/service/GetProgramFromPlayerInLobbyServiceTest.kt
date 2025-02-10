package software.shonk.application.service

import io.mockk.spyk
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.adapters.outgoing.MemoryLobbyManager
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort
import software.shonk.domain.Lobby
import software.shonk.interpreter.MockShork

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
    fun `get code from lobby`() {
        val aLobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(
                aLobbyId,
                hashMapOf("playerA" to "someProgram", "playerB" to "someOtherProgram"),
                MockShork(),
                joinedPlayers = mutableListOf("playerA", "playerB"),
            )
        )

        assertEquals(
            "someProgram",
            getProgramFromPlayerInLobbyService
                .getProgramFromPlayerInLobby(aLobbyId, "playerA")
                .getOrNull(),
        )
        assertEquals(
            "someOtherProgram",
            getProgramFromPlayerInLobbyService
                .getProgramFromPlayerInLobby(aLobbyId, "playerB")
                .getOrNull(),
        )
    }

    @Test
    fun `get code from multiple independent lobbies`() {
        val aLobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(
                aLobbyId,
                hashMapOf("playerA" to "someProgram", "playerB" to "someOtherProgram"),
                MockShork(),
                joinedPlayers = mutableListOf("playerA", "playerB"),
            )
        )

        val anotherLobbyId = 1L
        saveLobbyPort.saveLobby(
            Lobby(
                anotherLobbyId,
                hashMapOf("playerA" to "differentProgram", "playerB" to "evenMoreDifferentProgram"),
                MockShork(),
                joinedPlayers = mutableListOf("playerA", "playerB"),
            )
        )

        assertEquals(
            "someProgram",
            getProgramFromPlayerInLobbyService
                .getProgramFromPlayerInLobby(aLobbyId, "playerA")
                .getOrNull(),
        )
        assertEquals(
            "someOtherProgram",
            getProgramFromPlayerInLobbyService
                .getProgramFromPlayerInLobby(aLobbyId, "playerB")
                .getOrNull(),
        )
        assertEquals(
            "differentProgram",
            getProgramFromPlayerInLobbyService
                .getProgramFromPlayerInLobby(anotherLobbyId, "playerA")
                .getOrNull(),
        )
        assertEquals(
            "evenMoreDifferentProgram",
            getProgramFromPlayerInLobbyService
                .getProgramFromPlayerInLobby(anotherLobbyId, "playerB")
                .getOrNull(),
        )
    }

    @Test
    fun `get code from lobby with player who has not submitted anything`() {
        val aLobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(
                aLobbyId,
                hashMapOf("playerA" to "someProgram"),
                MockShork(),
                joinedPlayers = mutableListOf("playerA"),
            )
        )

        assertEquals(
            "No player with that name in the lobby",
            getProgramFromPlayerInLobbyService
                .getProgramFromPlayerInLobby(aLobbyId, "playerB")
                .exceptionOrNull()
                ?.message,
        )
    }

    @Test
    fun `get code from lobby with invalid lobby`() {
        assertEquals(
            "No lobby with that id",
            getProgramFromPlayerInLobbyService
                .getProgramFromPlayerInLobby(0L, "playerA")
                .exceptionOrNull()
                ?.message,
        )
    }
}
