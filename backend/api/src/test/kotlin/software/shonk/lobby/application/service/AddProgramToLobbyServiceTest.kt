package software.shonk.lobby.application.service

import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.incoming.addProgramToLobby.AddProgramToLobbyCommand
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.AddProgramToLobbyUseCase
import software.shonk.lobby.application.port.outgoing.DeleteLobbyPort
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.Lobby
import software.shonk.lobby.domain.PlayerNameString
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException
import kotlin.test.assertEquals

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
        addProgramToLobbyUseCase.addProgramToLobby(
            AddProgramToLobbyCommand(lobbyId, PlayerNameString("playerA"), "someProgram")
        )

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
        addProgramToLobbyUseCase.addProgramToLobby(
            AddProgramToLobbyCommand(lobbyId, PlayerNameString("playerB"), "someProgram")
        )

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
        val aLobbyIdThatDoesNotExist = 0L
        val result =
            addProgramToLobbyUseCase.addProgramToLobby(
                AddProgramToLobbyCommand(
                    aLobbyIdThatDoesNotExist,
                    PlayerNameString("playerA"),
                    "someProgram",
                )
            )

        assertTrue { result.isFailure }
        assertEquals(
            LobbyNotFoundException(aLobbyIdThatDoesNotExist).message,
            result.exceptionOrNull()?.message,
        )
    }
}
