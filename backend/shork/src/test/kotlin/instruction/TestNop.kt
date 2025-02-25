package instruction

import getDefaultInternalSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.internal.InternalShork
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier
import software.shonk.interpreter.internal.instruction.Dat
import software.shonk.interpreter.internal.instruction.Nop
import software.shonk.interpreter.internal.process.Process
import software.shonk.interpreter.internal.program.Program

internal class TestNop {

    private val dat = Dat(5, 13, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    private val settings = getDefaultInternalSettings(dat)
    private var shork = InternalShork(settings)
    private var program = Program("nop", shork)
    private var process = Process(program, 0)

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
        program = Program("nop", shork)
        process = Process(program, 0)
    }

    @Test
    fun `test executing with every modifier`() {
        for (mod in Modifier.entries) {
            val nop = Nop(0, 0, AddressMode.DIRECT, AddressMode.DIRECT, mod)
            shork.memoryCore.storeAbsolute(0, nop)
            nop.execute(process, shork.memoryCore.resolveFields(0))
            assertEquals(0, process.programCounter)
        }
    }

    @Test
    fun testNewInstanceSameValues() {
        val nop = Nop(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance =
            nop.newInstance(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)

        assert(newInstance is Nop)
        assertEquals(nop.aField, newInstance.aField)
        assertEquals(nop.bField, newInstance.bField)
        assertEquals(nop.addressModeA, newInstance.addressModeA)
        assertEquals(nop.addressModeB, newInstance.addressModeB)
        assertEquals(nop.modifier, newInstance.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val nop = Nop(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance = nop.newInstance(1, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)

        assert(newInstance is Nop)
        assertEquals(1, newInstance.aField)
        assertEquals(1, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeB)
        assertEquals(Modifier.B, newInstance.modifier)
    }

    @Test
    fun testDeepCopy() {
        val nop = Nop(0, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        val copy = nop.deepCopy()

        // Check that the fields are identical
        assertEquals(nop.aField, copy.aField)
        assertEquals(nop.bField, copy.bField)
        assertEquals(nop.addressModeA, copy.addressModeA)
        assertEquals(nop.addressModeB, copy.addressModeB)
        assertEquals(nop.modifier, copy.modifier)

        // Verify that the two instances are not the same object
        assert(nop !== copy)
    }
}
