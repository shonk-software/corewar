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
import software.shonk.interpreter.internal.instruction.Jmn
import software.shonk.interpreter.internal.program.Program

internal class TestJmn {

    private val dat = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    private val settings =
        getDefaultInternalSettings(dat, gameDataCollector = MockGameDataCollector())
    private var shork = InternalShork(settings)
    private var program = Program("jmn", shork)

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
        program = Program("jmn", shork)
        program.createProcessAt(0)
    }

    @Test
    fun `test jump unsuccessful if value is zero with modifier a`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        val dat2 = Dat(0, 42, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun `test jump unsuccessful if value is zero with modifier ba`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.BA)
        val dat2 = Dat(0, 42, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun `test jump unsuccessful if value is zero with modifier b`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)
        val dat2 = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun `test jump unsuccessful if value is zero with modifier ab`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.AB)
        val dat2 = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun `test jump unsuccessful if only one value is non-zero with modifier f`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        val dat2 = Dat(2, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun `test jump unsuccessful if only one value is non-zero with modifier x`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.X)
        val dat2 = Dat(2, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun `test jump unsuccessful if only one value is non-zero with modifier i`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        val dat2 = Dat(2, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun `test jump successful if value is non-zero with modifier a`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        val dat2 = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun `test jump successful if value is non-zero with modifier ba`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.BA)
        val dat2 = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun `test jump successful if value is non-zero with modifier b`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)
        val dat2 = Dat(0, 42, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun `test jump successful if value is non-zero with modifier ab`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.AB)
        val dat2 = Dat(0, 42, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun `test jump successful if both values are non-zero with modifier f`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        val dat2 = Dat(42, 3, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun `test jump successful if both values are non-zero with modifier i`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        val dat2 = Dat(42, 3, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun `test jump successful if both values are non-zero with modifier x`() {
        val jmn = Jmn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.X)
        val dat2 = Dat(42, 3, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmn)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun testNewInstanceSameValues() {
        val jmn = Jmn(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance =
            jmn.newInstance(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)

        assert(newInstance is Jmn)
        assertEquals(jmn.aField, newInstance.aField)
        assertEquals(jmn.bField, newInstance.bField)
        assertEquals(jmn.addressModeA, newInstance.addressModeA)
        assertEquals(jmn.addressModeB, newInstance.addressModeB)
        assertEquals(jmn.modifier, newInstance.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val jmn = Jmn(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance = jmn.newInstance(1, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)

        assert(newInstance is Jmn)
        assertEquals(1, newInstance.aField)
        assertEquals(1, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeB)
        assertEquals(Modifier.B, newInstance.modifier)
    }

    @Test
    fun testDeepCopy() {
        val jmn = Jmn(42, 0, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val copy = jmn.deepCopy()

        assertEquals(jmn.aField, copy.aField)
        assertEquals(jmn.bField, copy.bField)
        assertEquals(jmn.addressModeA, copy.addressModeA)
        assertEquals(jmn.addressModeB, copy.addressModeB)
        assertEquals(jmn.modifier, copy.modifier)
        assert(jmn !== copy)
    }
}
