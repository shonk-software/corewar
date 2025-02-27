package software.shonk.lobby.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import software.shonk.interpreter.GameOutcome
import software.shonk.interpreter.GameResult
import software.shonk.interpreter.MockShork
import software.shonk.interpreter.OutcomeKind

class LobbyTest {

    @Test
    fun canSetSetting() {
        val SOME_SETTINGS = InterpreterSettings(69, 123, "NOP", 0)

        val lobby = Lobby(id = 0, programs = HashMap(), shork = MockShork())

        lobby.setSettings(SOME_SETTINGS)
        assertEquals(SOME_SETTINGS, lobby.getSettings())
    }

    @Test
    fun initsWithDefaultStatus() {
        val lobby = Lobby(id = 0, programs = HashMap(), shork = MockShork())
        val actualStatus = lobby.getStatus()
        val expectedStatus =
            Status(
                playerASubmitted = false,
                playerBSubmitted = false,
                gameState = GameState.NOT_STARTED,
                result = GameResult(Winner.DRAW),
                visualizationData = actualStatus.visualizationData,
            )
        assertEquals(expectedStatus, actualStatus)
    }

    @Test
    fun `player a submitted becomes true when the player uploads their program`() {
        val lobby = Lobby(id = 0, programs = HashMap(), shork = MockShork())

        lobby.addProgram("playerA", "test")

        val actualStatus = lobby.getStatus()
        val expectedStatus =
            Status(
                playerASubmitted = true,
                playerBSubmitted = false,
                gameState = GameState.NOT_STARTED,
                result = GameResult(Winner.DRAW),
                visualizationData = actualStatus.visualizationData,
            )
        assertEquals(expectedStatus, actualStatus)
    }

    @Test
    fun `player b submitted becomes true when the player uploads their program`() {
        val lobby = Lobby(id = 0, programs = HashMap(), shork = MockShork())

        lobby.addProgram("playerB", "test")

        val actualStatus = lobby.getStatus()
        val expectedStatus =
            Status(
                playerASubmitted = false,
                playerBSubmitted = true,
                gameState = GameState.NOT_STARTED,
                result = GameResult(Winner.DRAW),
                visualizationData = actualStatus.visualizationData,
            )
        assertEquals(expectedStatus, actualStatus)
    }

    @Test
    fun `runs game when both players have submitted their programs`() {
        val lobby = spyk(Lobby(id = 0, programs = HashMap(), shork = MockShork()))

        lobby.addProgram("playerA", "someProgram")
        lobby.addProgram("playerB", "someOtherProgram")

        verify(exactly = 1) { lobby.run() }
    }

    @Test
    fun `check if game is finished and winner is decided`() {
        val shork = mockk<MockShork>()
        every { shork.run(any(), any()) } returns
            Result.success(GameResult(GameOutcome("playerA", OutcomeKind.WIN), emptyList()))

        val lobby = Lobby(id = 0, programs = HashMap(), shork = shork)
        lobby.addProgram("playerA", "someProgram")
        lobby.addProgram("playerB", "someOtherProgram")

        val actualStatus = lobby.getStatus()
        val expectedStatus =
            Status(
                playerASubmitted = true,
                playerBSubmitted = true,
                gameState = GameState.FINISHED,
                result = GameResult(Winner.A),
                visualizationData = actualStatus.visualizationData,
            )
        assertEquals(expectedStatus, actualStatus)
    }

    @Test
    fun `tests if the shork is called upon during the lobby run`() {
        val shork = spyk<MockShork>()
        val lobby = Lobby(id = 0, programs = HashMap(), shork = shork)
        lobby.addProgram("playerA", "someProgram")
        lobby.addProgram("playerB", "someOtherProgram")

        verify(exactly = 1) { shork.run(any(), any()) }
    }

    @Test
    fun `test verify joined players`() {
        val lobby =
            Lobby(0L, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerA", "playerB"))
        assertTrue(lobby.containsPlayer("playerA"))
        assertTrue(lobby.containsPlayer("playerB"))
    }
}
