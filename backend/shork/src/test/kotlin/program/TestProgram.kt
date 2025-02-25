package program

import assertExecutionCountAtAddress
import getDefaultInternalSettings
import mocks.MockGameDataCollector
import mocks.MockInstruction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.internal.InternalShork
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier
import software.shonk.interpreter.internal.instruction.Dat
import software.shonk.interpreter.internal.instruction.Jmp
import software.shonk.interpreter.internal.program.Program

internal class TestProgram {
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
    fun testNoProcesses() {
        // Shouldn't blow up and be a no-op
        program.tick()

        // The memory shouldn't be modified
        for (i in 0 until 8000) {
            val instruction = shork.memoryCore.loadAbsolute(i)
            instruction as MockInstruction
            assertEquals(0, instruction.executionCount)
        }
    }

    @Test
    fun testTickSingleProgram() {
        program.createProcessAt(42)

        program.tick()
        assertExecutionCountAtAddress(shork.memoryCore, 42, 1)

        // Tick again, it should move forward
        program.tick()
        assertExecutionCountAtAddress(shork.memoryCore, 43, 1)
    }

    @Test
    fun testTickMultiplePrograms() {
        program.createProcessAt(42)
        program.createProcessAt(420)

        // First process should be ticked
        program.tick()

        assertExecutionCountAtAddress(shork.memoryCore, 42, 1)
        assertExecutionCountAtAddress(shork.memoryCore, 420, 0)

        // Now the second process should get ticked
        program.tick()

        // Both are now at 1 execution
        assertExecutionCountAtAddress(shork.memoryCore, 42, 1)
        assertExecutionCountAtAddress(shork.memoryCore, 420, 1)

        // Tick again, 2 processes means the first one should be ticked again
        program.tick()

        // Program 1 was ticked again, it moved forward
        assertExecutionCountAtAddress(shork.memoryCore, 43, 1)
    }

    // This test tests with multiple processes and removal of a program
    @Test
    fun testRemoveProgram() {
        program.createProcessAt(42)
        program.createProcessAt(420)

        val process1 = program.processes.get()
        val process2 = program.processes.get()

        program.tick()

        assertExecutionCountAtAddress(shork.memoryCore, 42, 1)
        assertExecutionCountAtAddress(shork.memoryCore, 420, 0)

        program.removeProcess(process2)

        program.tick()

        // The second process was removed, so it's start instruction shouldn't be executed
        assertExecutionCountAtAddress(shork.memoryCore, 42, 1)
        assertExecutionCountAtAddress(shork.memoryCore, 420, 0)
        assertExecutionCountAtAddress(shork.memoryCore, 43, 1)
    }

    @Test
    fun testRemoveProgramComplex() {
        program.createProcessAt(42)
        program.createProcessAt(420)
        program.createProcessAt(4200)

        val process1 = program.processes.get()
        val process2 = program.processes.get()
        val process3 = program.processes.get()

        // First process should be ticket, second and third should not
        program.tick()
        assertExecutionCountAtAddress(shork.memoryCore, 42, 1)
        assertExecutionCountAtAddress(shork.memoryCore, 420, 0)
        assertExecutionCountAtAddress(shork.memoryCore, 4200, 0)

        // Gonna remove the middle / second process
        program.removeProcess(process2)

        // Third process should now get ticked, second should not
        program.tick()
        assertExecutionCountAtAddress(shork.memoryCore, 42, 1)
        assertExecutionCountAtAddress(shork.memoryCore, 420, 0)
        assertExecutionCountAtAddress(shork.memoryCore, 4200, 1)

        // Now the first process should get ticked again
        program.tick()
        assertExecutionCountAtAddress(shork.memoryCore, 42, 1)
        assertExecutionCountAtAddress(shork.memoryCore, 43, 1)
        assertExecutionCountAtAddress(shork.memoryCore, 420, 0)
        assertExecutionCountAtAddress(shork.memoryCore, 4200, 1)

        // Now the third process should get ticked again, the second one should still not be ticked
        program.tick()
        assertExecutionCountAtAddress(shork.memoryCore, 42, 1)
        assertExecutionCountAtAddress(shork.memoryCore, 43, 1)
        assertExecutionCountAtAddress(shork.memoryCore, 420, 0)
        assertExecutionCountAtAddress(shork.memoryCore, 4200, 1)
        assertExecutionCountAtAddress(shork.memoryCore, 4201, 1)
    }

    @Test
    fun testIsAlive() {
        // No processes, should be dead 💀💀💀
        assertEquals(false, program.isAlive())

        // Adding a process, should be alive
        program.createProcessAt(42)
        assertEquals(true, program.isAlive())

        // Dead again :(
        program.removeProcess(program.processes.get())
        assertEquals(false, program.isAlive())
    }

    @Test
    fun `test if program integrates with game data collector`() {
        val dat = Dat(1, 1, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val settings = getDefaultInternalSettings(dat)
        val jmp = Jmp(42, 0, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)

        shork = InternalShork(settings)
        shork.memoryCore.storeAbsolute(0, jmp)
        program = Program("Test", shork)
        val gameDataCollector = shork.gameDataCollector

        program.createProcessAt(0)
        program.createProcessAt(420)
        program.createProcessAt(4200)

        gameDataCollector.startRoundForProgram(program)
        program.tick()
        gameDataCollector.endRoundForProgram(program)

        val result = gameDataCollector.getGameStatistics().first()

        assertEquals(0, result.programCounterBefore)
        assertEquals(42, result.programCounterAfter)
        assertEquals(false, result.processDied)
        assertEquals(listOf(420, 4200), result.programCountersOfOtherProcesses)
    }

    fun testCreateProcessWithMaximumProcesses() {
        val settings = getDefaultInternalSettings(MockInstruction(), maximumProcessesPerPlayer = 2)
        val shork = InternalShork(settings)
        val program = Program("id", shork)

        program.createProcessAt(42)
        assertEquals(1, program.processes.size())

        program.createProcessAt(69)
        assertEquals(2, program.processes.size())

        program.createProcessAt(420)
        assertEquals(2, program.processes.size())
    }

    @Test
    fun testRemoveAndCreateProcessWithMaximumProcesses() {
        val settings = getDefaultInternalSettings(MockInstruction(), maximumProcessesPerPlayer = 2)
        val shork = InternalShork(settings)
        val program = Program("id", shork)

        program.createProcessAt(42)
        program.createProcessAt(69)
        program.createProcessAt(420)
        assertEquals(2, program.processes.size())

        program.removeProcess(program.processes.get())
        assertEquals(1, program.processes.size())

        program.createProcessAt(420)
        assertEquals(2, program.processes.size())

        program.createProcessAt(720)
        assertEquals(2, program.processes.size())
    }
}
