package software.shonk.lobby.application.service

import io.mockk.spyk
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.incoming.GetProgramFromPlayerInLobbyCommand
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.Lobby

class GetProgramFromPlayerInLobbyServiceTest {

    lateinit var loadLobbyPort: LoadLobbyPort
    // todo the stuff we do here with saveLobbyPort should be testhelpers that directly access the
    // MemoryLobbyManager stuff or something similar
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
                .getProgramFromPlayerInLobby(
                    GetProgramFromPlayerInLobbyCommand(aLobbyId, "playerA")
                )
                .getOrNull(),
        )
        assertEquals(
            "someOtherProgram",
            getProgramFromPlayerInLobbyService
                .getProgramFromPlayerInLobby(
                    GetProgramFromPlayerInLobbyCommand(aLobbyId, "playerB")
                )
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
                .getProgramFromPlayerInLobby(
                    GetProgramFromPlayerInLobbyCommand(aLobbyId, "playerA")
                )
                .getOrNull(),
        )
        assertEquals(
            "someOtherProgram",
            getProgramFromPlayerInLobbyService
                .getProgramFromPlayerInLobby(
                    GetProgramFromPlayerInLobbyCommand(aLobbyId, "playerB")
                )
                .getOrNull(),
        )
        assertEquals(
            "differentProgram",
            getProgramFromPlayerInLobbyService
                .getProgramFromPlayerInLobby(
                    GetProgramFromPlayerInLobbyCommand(anotherLobbyId, "playerA")
                )
                .getOrNull(),
        )
        assertEquals(
            "evenMoreDifferentProgram",
            getProgramFromPlayerInLobbyService
                .getProgramFromPlayerInLobby(
                    GetProgramFromPlayerInLobbyCommand(anotherLobbyId, "playerB")
                )
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
                .getProgramFromPlayerInLobby(
                    GetProgramFromPlayerInLobbyCommand(aLobbyId, "playerB")
                )
                .exceptionOrNull()
                ?.message,
        )
    }

    @Test
    fun `get code from lobby with invalid lobby`() {
        val result =
            getProgramFromPlayerInLobbyService.getProgramFromPlayerInLobby(
                GetProgramFromPlayerInLobbyCommand(0L, "playerA")
            )
        assertFalse(result.isSuccess)
        assertEquals("No lobby with that id", result.exceptionOrNull()?.message)
    }
}
