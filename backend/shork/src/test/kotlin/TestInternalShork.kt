import mocks.KillProgramInstruction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.internal.FinishedState
import software.shonk.interpreter.internal.GameStatus
import software.shonk.interpreter.internal.InternalShork
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier
import software.shonk.interpreter.internal.instruction.Mov
import software.shonk.interpreter.internal.program.Program

internal class TestInternalShork {
    var settings = getDefaultInternalSettings(KillProgramInstruction())

    @BeforeEach
    fun setUp() {
        settings = getDefaultInternalSettings(KillProgramInstruction())
    }

    @Test
    fun gameStatusIsDrawWithNoProgramsAndRunCalled() {
        val shork = InternalShork(settings)
        val status: GameStatus = shork.run()

        assertEquals(GameStatus.FINISHED(FinishedState.DRAW), status)
    }

    @Test
    fun `draw is correctly detected with nonzero amount of players`() {
        val shork = InternalShork(settings)

        val program = Program("Blahaj \uD83E\uDD7A", shork)
        val program2 = Program("Shork \uD83E\uDD88", shork)

        // Everything is just the kill instruction, so both programs should be killed
        // immediately and hence "loose" in the first round
        program.createProcessAt(42)
        program2.createProcessAt(1337)

        shork.addProgram(program, program2)

        val status = shork.run()

        assertEquals(GameStatus.FINISHED(FinishedState.DRAW), status)
    }

    @Test
    fun `player can win`() {
        val shork = InternalShork(settings)
        val core = shork.memoryCore

        val blahaj = Program("Blahaj \uD83E\uDD7A", shork)
        val shorky = Program("Shork \uD83E\uDD88", shork)

        // One imp please
        val impy = Mov(0, 1, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.I)
        core.storeAbsolute(42, impy)

        // Blahaj gets the imp
        blahaj.createProcessAt(42)
        // Shork gets a kill program instruction :(
        shorky.createProcessAt(1337)

        shork.addProgram(blahaj, shorky)

        val status = shork.run()

        // Shork's gonna loose qwq
        assertEquals(GameStatus.FINISHED(FinishedState.WINNER(blahaj)), status)
    }
}
