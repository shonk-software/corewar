package software.shonk.lobby.application.service

import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.*
import software.shonk.lobby.adapters.incoming.addProgramToLobby.AddProgramToLobbyCommand
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.AddProgramToLobbyUseCase
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.exceptions.LobbyAlreadyCompletedException
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException
import software.shonk.lobby.domain.exceptions.PlayerNotInLobbyException

class AddProgramToLobbyServiceTest {
    private lateinit var addProgramToLobbyUseCase: AddProgramToLobbyUseCase
    private lateinit var loadLobbyPort: LoadLobbyPort
    private lateinit var saveLobbyPort: SaveLobbyPort

    // The in-memory lobby management also serves as a kind of mock here.
    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        loadLobbyPort = lobbyManager
        saveLobbyPort = lobbyManager
        addProgramToLobbyUseCase = AddProgramToLobbyService(loadLobbyPort, saveLobbyPort)
    }

    @Test
    fun `submitting a program stores that program in the lobby for playerA and returns success`() {
        // Given ...
        saveLobbyPort.saveLobby(
            aLobby(id = A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
        )
        clearAllMocks()

        // When ...
        val result =
            addProgramToLobbyUseCase.addProgramToLobby(
                AddProgramToLobbyCommand(A_VALID_LOBBY_ID, A_VALID_PLAYERNAME, A_REDCODE_PROGRAM)
            )

        // Then ...
        assertTrue(result.isSuccess)
        verify(exactly = 1) {
            saveLobbyPort.saveLobby(
                match { it ->
                    it.programs.containsKey(A_VALID_PLAYERNAME) &&
                        it.programs.get(A_VALID_PLAYERNAME).equals(A_REDCODE_PROGRAM) &&
                        it.getStatus().playerASubmitted &&
                        !it.getStatus().playerBSubmitted
                }
            )
        }
    }

    @Test
    fun `submitting a program stores that program in the lobby for playerB and returns success`() {
        // Given ...
        saveLobbyPort.saveLobby(
            aLobby(id = A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(ANOTHER_VALID_PLAYERNAME))
        )
        clearAllMocks()

        // When ...
        addProgramToLobbyUseCase.addProgramToLobby(
            AddProgramToLobbyCommand(A_VALID_LOBBY_ID, ANOTHER_VALID_PLAYERNAME, A_REDCODE_PROGRAM)
        )

        // Then ...
        verify(exactly = 1) {
            // todo improve this matcher (everywhere)
            saveLobbyPort.saveLobby(
                match { it ->
                    it.programs.containsKey(ANOTHER_VALID_PLAYERNAME) &&
                        it.programs.get(ANOTHER_VALID_PLAYERNAME).equals(A_REDCODE_PROGRAM) &&
                        !it.getStatus().playerASubmitted &&
                        it.getStatus().playerBSubmitted
                }
            )
        }
    }

    @Test
    fun `submitting a program for a lobby that does not exist returns Lobby NotFoundException and does nothing`() {
        // Given...

        // When...
        val result =
            addProgramToLobbyUseCase.addProgramToLobby(
                AddProgramToLobbyCommand(
                    A_LOBBY_ID_THAT_HAS_NOT_BEEN_CREATED,
                    A_VALID_PLAYERNAME,
                    A_REDCODE_PROGRAM,
                )
            )

        // Then...
        assertTrue { result.isFailure }
        assertTrue { result.exceptionOrNull() is LobbyNotFoundException }
    }

    @Test
    fun `submitting a program for a lobby that has already completed returns LobbyAlreadyCompletedException and does nothing`() {
        // Given...
        saveLobbyPort.saveLobby(
            aLobbyThatHasAlreadyRun(
                id = A_VALID_LOBBY_ID,
                joinedPlayers = mutableListOf(A_VALID_PLAYERNAME, ANOTHER_VALID_PLAYERNAME),
                programs =
                    hashMapOf(
                        A_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                        ANOTHER_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
                    ),
            )
        )
        clearAllMocks()

        // When...
        val result =
            addProgramToLobbyUseCase.addProgramToLobby(
                AddProgramToLobbyCommand(A_VALID_LOBBY_ID, A_VALID_PLAYERNAME, A_REDCODE_PROGRAM)
            )

        // Then...
        assertTrue { result.isFailure }
        assertTrue { result.exceptionOrNull() is LobbyAlreadyCompletedException }
    }

    @Test
    fun `submitting a program for a player that has not joined the lobby throws PlayerNotInLobbyException and does nothing`() {
        // Given ...
        saveLobbyPort.saveLobby(
            aLobby(id = A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
        )
        clearAllMocks()

        // When ...
        val result =
            addProgramToLobbyUseCase.addProgramToLobby(
                AddProgramToLobbyCommand(
                    A_VALID_LOBBY_ID,
                    ANOTHER_VALID_PLAYERNAME,
                    A_REDCODE_PROGRAM,
                )
            )

        // Then ...
        assertTrue(result.isFailure)
        assertTrue { result.exceptionOrNull() is PlayerNotInLobbyException }
    }
}
