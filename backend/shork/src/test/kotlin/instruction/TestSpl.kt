package instruction

import getDefaultInternalSettings
import mocks.MockGameDataCollector
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.internal.InternalShork
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier
import software.shonk.interpreter.internal.instruction.Dat
import software.shonk.interpreter.internal.instruction.Spl
import software.shonk.interpreter.internal.process.Process
import software.shonk.interpreter.internal.program.Program

internal class TestSpl {
    val dat = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    val settings = getDefaultInternalSettings(dat, gameDataCollector = MockGameDataCollector())
    var shork = InternalShork(settings)

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
    }

    @Test
    fun testExecute() {
        val program = Program("Splitty", shork)
        val process = Process(program, 0)

        val spl = Spl(42, 0, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        shork.memoryCore.storeAbsolute(0, spl)
        spl.execute(process, shork.memoryCore.resolveFields(0))

        assertEquals(program.processes.get().programCounter, 42)
        assert(shork.memoryCore.loadAbsolute(42) is Dat)
    }

    @Test
    fun `test in conjunction with program`() {
        val core = shork.memoryCore
        val program = Program("Splitty", shork)

        val spl = Spl(42, 0, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, spl)
        program.createProcessAt(0)

        program.tick()

        assertEquals(2, program.processes.size())
        // Execution should first continue from the next instruction after SPL
        assertEquals(1, program.processes.get().programCounter)
        // The new process should have been created at the address specified by the SPL instruction
        assertEquals(42, program.processes.get().programCounter)
    }

    @Test
    fun testNewInstanceSameValues() {
        val spl = Spl(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance =
            spl.newInstance(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)

        assert(newInstance is Spl)
        assertEquals(spl.aField, newInstance.aField)
        assertEquals(spl.bField, newInstance.bField)
        assertEquals(spl.addressModeA, newInstance.addressModeA)
        assertEquals(spl.addressModeB, newInstance.addressModeB)
        assertEquals(spl.modifier, newInstance.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val spl = Spl(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance = spl.newInstance(1, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)

        assert(newInstance is Spl)
        assertEquals(1, newInstance.aField)
        assertEquals(1, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeB)
        assertEquals(Modifier.B, newInstance.modifier)
    }

    @Test
    fun testDeepCopy() {
        val spl = Spl(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val copy = spl.deepCopy()

        assertEquals(spl.aField, copy.aField)
        assertEquals(spl.bField, copy.bField)
        assertEquals(spl.addressModeA, copy.addressModeA)
        assertEquals(spl.addressModeB, copy.addressModeB)
        assertEquals(spl.modifier, copy.modifier)
        assert(spl !== copy)
    }
}
