package software.shonk.application.service

import io.mockk.spyk
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.adapters.outgoing.MemoryLobbyManager
import software.shonk.application.port.outgoing.DeleteLobbyPort
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort
import software.shonk.domain.*
import software.shonk.interpreter.MockShork

class ShorkServiceTest {

    private lateinit var shorkService: ShorkService
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
        shorkService = ShorkService(loadLobbyPort, saveLobbyPort)
    }

    @Test
    fun `playerA submits program`() {
        val lobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(lobbyId, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerA"))
        )

        shorkService.addProgramToLobby(lobbyId, Player("playerA"), "someProgram")
        val result = loadLobbyPort.getLobby(lobbyId).getOrThrow().getStatus()

        assertEquals(
            Status(
                playerASubmitted = true,
                playerBSubmitted = false,
                gameState = GameState.NOT_STARTED,
                result = GameResult(winner = Winner.DRAW),
                visualizationData = result.visualizationData,
            ),
            result,
        )
    }

    @Test
    fun `create lobby and playerB submits program`() {
        val lobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(
                lobbyId,
                hashMapOf(),
                MockShork(),
                joinedPlayers = mutableListOf("playerA", "playerB"),
            )
        )

        shorkService.addProgramToLobby(lobbyId, Player("playerB"), "someProgram")
        val result = loadLobbyPort.getLobby(lobbyId).getOrThrow().getStatus()

        assertEquals(
            Status(
                playerASubmitted = false,
                playerBSubmitted = true,
                gameState = GameState.NOT_STARTED,
                result = GameResult(winner = Winner.DRAW),
                visualizationData = result.visualizationData,
            ),
            result,
        )
    }

    @Test
    fun `upload code in lobby 0 does not affect lobby 1`() {
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
                joinedPlayers = mutableListOf("playerB"),
            )
        )

        shorkService.addProgramToLobby(aLobbyId, Player("playerA"), "someProgram")
        val result0 = loadLobbyPort.getLobby(aLobbyId).getOrThrow().getStatus()
        assertEquals(
            result0,
            Status(
                playerASubmitted = true,
                playerBSubmitted = false,
                gameState = GameState.NOT_STARTED,
                result = GameResult(winner = Winner.DRAW),
                visualizationData = result0.visualizationData,
            ),
        )

        val result1 = loadLobbyPort.getLobby(anotherLobbyId).getOrThrow().getStatus()
        assertEquals(
            result1,
            Status(
                playerASubmitted = false,
                playerBSubmitted = false,
                gameState = GameState.NOT_STARTED,
                result = GameResult(winner = Winner.DRAW),
                visualizationData = result1.visualizationData,
            ),
        )
    }

    @Test
    fun `add program to the lobby fails if lobby does not exist`() {
        val result = shorkService.addProgramToLobby(0L, Player("playerA"), "someProgram")

        assertEquals(true, result.isFailure)
        assertEquals("No lobby with that id", result.exceptionOrNull()?.message)
    }

    // This is a DEFINITELY an integration test!!!
    /*
    @Test
    fun `result contains visualization data`() {
        val lobbyId = shorkService.createLobby("playerA").getOrThrow()
        shorkService.addProgramToLobby(lobbyId, "playerA", "mov 0, 1")
        shorkService.joinLobby(lobbyId, "playerB")
        shorkService.addProgramToLobby(lobbyId, "playerB", "mov 0, 1")

        val result = shorkService.getLobbyStatus(lobbyId).getOrThrow()
        assertEquals(GameState.FINISHED, result.gameState)
        assertEquals(Winner.DRAW, result.result.winner)
        assertTrue(result.visualizationData.isNotEmpty())
    }

    @Test
    fun `game visualization data absent before first run`() {
        val lobbyId = shorkService.createLobby("playerA").getOrThrow()
        shorkService.setLobbySettings(lobbyId, Settings())

        val result = shorkService.getLobbyStatus(lobbyId).getOrThrow()
        assertEquals(GameState.NOT_STARTED, result.gameState)
        assertEquals(Winner.DRAW, result.result.winner)
        assertTrue(result.visualizationData.isEmpty())
    }

    @Test
    fun `game visualization data can be disabled (not included)`() {
        val lobbyId = shorkService.createLobby("playerA").getOrThrow()
        shorkService.addProgramToLobby(lobbyId, "playerA", "mov 0, 1")
        shorkService.joinLobby(0L, "playerB")
        shorkService.addProgramToLobby(lobbyId, "playerB", "jmp 42")

        val result =
            shorkService.getLobbyStatus(lobbyId, includeRoundInformation = false).getOrThrow()
        assertEquals(GameState.FINISHED, result.gameState)
        assertTrue(result.visualizationData.isEmpty())
    }
    */
}
