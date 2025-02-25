package process

import assertExecutionCountAtAddress
import getDefaultInternalSettings
import mocks.MockGameDataCollector
import mocks.MockInstruction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.internal.InternalShork
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier
import software.shonk.interpreter.internal.instruction.Dat
import software.shonk.interpreter.internal.instruction.Jmp
import software.shonk.interpreter.internal.program.Program

internal class TestProcess {
    private var settings =
        getDefaultInternalSettings(MockInstruction(), gameDataCollector = MockGameDataCollector())
    private var shork = InternalShork(settings)
    private var program = Program("id", shork)

    @BeforeEach
    fun beforeEach() {
        shork = InternalShork(settings)
        program = Program("id", shork)
    }

    @Test
    fun testTickSingle() {
        program.createProcessAt(42)

        val process = program.processes.get()
        process.tick()

        assertExecutionCountAtAddress(shork.memoryCore, 42, 1)
    }

    // Tests multiple ticks on the same process
    @Test
    fun testTickMultiple() {
        program.createProcessAt(42)

        val process = program.processes.get()
        process.tick()
        process.tick()

        assertExecutionCountAtAddress(shork.memoryCore, 42, 1)
        assertExecutionCountAtAddress(shork.memoryCore, 43, 1)
    }

    @Test
    fun `test multiple ticks with no programcounter incrementation`() {
        program.createProcessAt(42)

        val process = program.processes.get()
        process.tick()
        assertEquals(43, process.programCounter)

        process.dontIncrementProgramCounter = true
        process.tick()
        assertEquals(43, process.programCounter)
        assertFalse(process.dontIncrementProgramCounter)
    }

    @Test
    fun testDontIncrementProgramCounter() {
        program.createProcessAt(42)

        val process = program.processes.get()
        process.dontIncrementProgramCounter = true
        process.tick()

        assertEquals(42, process.programCounter)
    }

    @Test
    fun `test if process integrates with the Game Data Collector`() {
        val dat = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val settings = getDefaultInternalSettings(dat)
        shork = InternalShork(settings)
        val gameDataCollector = shork.gameDataCollector

        val jmp = Jmp(42, 0, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        shork.memoryCore.storeAbsolute(0, jmp)

        val program = Program("Test", shork)
        program.createProcessAt(0)
        val process = program.processes.first()

        gameDataCollector.startRoundForProgram(program)
        gameDataCollector.collectProcessDataBeforeTick(process)

        process.tick()

        gameDataCollector.collectProcessDataAfterTick(process)
        gameDataCollector.endRoundForProgram(program)

        val stats = gameDataCollector.getGameStatistics().first()
        assertEquals(0, stats.programCounterBefore)
        assertEquals(42, stats.programCounterAfter)
        assertEquals(false, stats.processDied)
    }
}
