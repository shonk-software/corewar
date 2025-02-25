package instruction

import getDefaultInternalSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import software.shonk.interpreter.internal.InternalShork
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier
import software.shonk.interpreter.internal.instruction.Dat
import software.shonk.interpreter.internal.process.Process
import software.shonk.interpreter.internal.program.Program

internal class TestDat {
    @Test
    fun testExecute() {
        val dat = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val settings = getDefaultInternalSettings(dat)
        val shork = InternalShork(settings)
        val program = Program("A-", shork)
        val process = Process(program, 0)

        dat.execute(process, shork.memoryCore.resolveFields(0))
        assert(program.processes.isEmpty())
    }

    @Test
    fun testNewInstanceSameValues() {
        val dat = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance =
            dat.newInstance(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)

        assert(newInstance is Dat)
        assertEquals(dat.aField, newInstance.aField)
        assertEquals(dat.bField, newInstance.bField)
        assertEquals(dat.addressModeA, newInstance.addressModeA)
        assertEquals(dat.addressModeB, newInstance.addressModeB)
        assertEquals(dat.modifier, newInstance.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val dat = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance = dat.newInstance(1, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)

        assert(newInstance is Dat)
        assertEquals(1, newInstance.aField)
        assertEquals(1, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeB)
        assertEquals(Modifier.B, newInstance.modifier)
    }

    @Test
    fun testDeepCopy() {
        val dat = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val copy = dat.deepCopy()

        assertEquals(dat.aField, copy.aField)
        assertEquals(dat.bField, copy.bField)
        assertEquals(dat.addressModeA, copy.addressModeA)
        assertEquals(dat.addressModeB, copy.addressModeB)
        assertEquals(dat.modifier, copy.modifier)
        assert(dat !== copy)
    }
}
