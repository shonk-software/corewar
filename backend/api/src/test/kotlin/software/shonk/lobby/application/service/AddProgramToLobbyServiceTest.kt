package software.shonk.lobby.application.service

import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.AddProgramToLobbyUseCase
import software.shonk.lobby.application.port.outgoing.DeleteLobbyPort
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.Lobby
import software.shonk.lobby.domain.Player

class AddProgramToLobbyServiceTest {
    private lateinit var addProgramToLobbyUseCase: AddProgramToLobbyUseCase
    private lateinit var loadLobbyPort: LoadLobbyPort
    private lateinit var saveLobbyPort: SaveLobbyPort
    private lateinit var deleteLobbyPort: DeleteLobbyPort

    // The in-memory lobby management also serves as a kind of mock here.
    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        loadLobbyPort = lobbyManager
        saveLobbyPort = lobbyManager
        deleteLobbyPort = lobbyManager
        addProgramToLobbyUseCase = AddProgramToLobbyService(loadLobbyPort, saveLobbyPort)
    }

    @Test
    fun `submitting a program stores that program in the lobby for playerA`() {
        // Given ...
        val lobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(lobbyId, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerA"))
        )
        clearAllMocks()

        // When ...
        addProgramToLobbyUseCase.addProgramToLobby(lobbyId, Player("playerA"), "someProgram")

        // Then ...
        verify(exactly = 1) {
            saveLobbyPort.saveLobby(
                match { it ->
                    it.programs.containsKey("playerA") &&
                        it.programs.get("playerA").equals("someProgram") &&
                        it.getStatus().playerASubmitted &&
                        !it.getStatus().playerBSubmitted
                }
            )
        }
    }

    @Test
    fun `submitting a program stores that program in the lobby for playerB`() {
        // Given ...
        val lobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(lobbyId, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerB"))
        )
        clearAllMocks()

        // When ...
        addProgramToLobbyUseCase.addProgramToLobby(lobbyId, Player("playerB"), "someProgram")

        // Then ...
        verify(exactly = 1) {
            saveLobbyPort.saveLobby(
                match { it ->
                    it.programs.containsKey("playerB") &&
                        it.programs.get("playerB").equals("someProgram") &&
                        !it.getStatus().playerASubmitted &&
                        it.getStatus().playerBSubmitted
                }
            )
        }
    }

    @Test
    fun `add program to the lobby fails if lobby does not exist`() {
        val result =
            addProgramToLobbyUseCase.addProgramToLobby(0L, Player("playerA"), "someProgram")

        assertEquals(true, result.isFailure)
        assertEquals("No lobby with that id", result.exceptionOrNull()?.message)
    }
}
