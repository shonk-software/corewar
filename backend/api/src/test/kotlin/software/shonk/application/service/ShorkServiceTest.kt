package software.shonk.application.service

import io.mockk.spyk
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

        shorkService.addProgramToLobby(lobbyId, "playerA", "someProgram")
        val result = shorkService.getLobbyStatus(lobbyId).getOrThrow()

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
            Lobby(lobbyId, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerA"))
        )

        shorkService.joinLobby(lobbyId, "playerB")
        shorkService.addProgramToLobby(lobbyId, "playerB", "someProgram")
        val result = shorkService.getLobbyStatus(lobbyId).getOrThrow()

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

        shorkService.addProgramToLobby(aLobbyId, "playerA", "someProgram")
        val result0 = shorkService.getLobbyStatus(aLobbyId).getOrThrow()
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

        val result1 = shorkService.getLobbyStatus(anotherLobbyId).getOrThrow()
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
    fun `add code with playerName null`() {
        val aLobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(aLobbyId, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerA"))
        )
        val result = shorkService.addProgramToLobby(aLobbyId, null, "someProgram")

        assertEquals(true, result.isFailure)
        assertEquals("Invalid player name", result.exceptionOrNull()?.message)
    }

    @Test
    fun `get status for the lobby fails if lobby does not exist`() {
        val result = shorkService.getLobbyStatus(0L)

        assertEquals(true, result.isFailure)
        assertEquals("No lobby with that id", result.exceptionOrNull()?.message)
    }

    @Test
    fun `add program to the lobby fails if lobby does not exist`() {
        val result = shorkService.addProgramToLobby(0L, "playerA", "someProgram")

        assertEquals(true, result.isFailure)
        assertEquals("No lobby with that id", result.exceptionOrNull()?.message)
    }

    @Test
    fun `join lobby with valid playerName`() {
        val aLobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(aLobbyId, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerA"))
        )
        shorkService.joinLobby(aLobbyId, "playerB")

        assertEquals(
            true,
            loadLobbyPort.getLobby(aLobbyId).getOrNull()?.joinedPlayers?.contains("playerB"),
        )
    }

    @Test
    fun `join lobby with duplicate (invalid) playerName`() {
        val aLobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(aLobbyId, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerA"))
        )
        shorkService.joinLobby(aLobbyId, "playerA")

        assertEquals(1, loadLobbyPort.getLobby(aLobbyId).getOrNull()?.joinedPlayers?.size)
    }

    @Test
    fun `join nonexistent lobby`() {
        val result = shorkService.joinLobby(0L, "playerA")

        assertEquals(result.isFailure, true)
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

    @Test
    fun `test verify joined players`() {
        val aLobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(aLobbyId, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerA"))
        )
        val resultA = shorkService.playerIsInLobby("playerA", aLobbyId)
        assertTrue(resultA.isSuccess)
        shorkService.joinLobby(aLobbyId, "playerB")
        val resultB = shorkService.playerIsInLobby("playerB", aLobbyId)
        assertTrue(resultB.isSuccess)
    }
}
