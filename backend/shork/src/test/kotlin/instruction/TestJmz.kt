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
import software.shonk.interpreter.internal.instruction.Jmz
import software.shonk.interpreter.internal.program.Program

internal class TestJmz {

    private val dat = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    private val settings =
        getDefaultInternalSettings(dat, gameDataCollector = MockGameDataCollector())
    private var shork = InternalShork(settings)
    private var program = Program("jmz", shork)

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
        program = Program("jmz", shork)
        program.createProcessAt(0)
    }

    @Test
    fun `test jump successful if value is zero with modifier a`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        val dat2 = Dat(0, 42, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun `test jump successful if value is zero with modifier ba`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.BA)
        val dat2 = Dat(0, 42, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun `test jump successful if value is zero with modifier b`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)
        val dat2 = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun `test jump successful if value is zero with modifier ab`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.AB)
        val dat2 = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun `test jump successful if value is zero with modifier f`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        val dat2 = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun `test jump successful if value is zero with modifier x`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.X)
        val dat2 = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun `test jump successful if value is zero with modifier i`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        val dat2 = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)
    }

    @Test
    fun `test jump unsuccessful if value is non-zero with modifier a`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        val dat2 = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun `test jump unsuccessful if value is non-zero with modifier ba`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.BA)
        val dat2 = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun `test jump unsuccessful if value is non-zero with modifier b`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)
        val dat2 = Dat(0, 42, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun `test jump unsuccessful if value is non-zero with modifier ab`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.AB)
        val dat2 = Dat(0, 42, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun `test jump unsuccessful if values are non-zero with modifier f`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        val dat2 = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun `test jump unsuccessful if values are non-zero with modifier i`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        val dat2 = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun `test jump unsuccessful if values are non-zero with modifier x`() {
        val jmz = Jmz(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.X)
        val dat2 = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        program.shork.memoryCore.storeAbsolute(0, jmz)
        program.shork.memoryCore.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)
    }

    @Test
    fun testNewInstanceSameValues() {
        val jmz = Jmz(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance =
            jmz.newInstance(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)

        assert(newInstance is Jmz)
        assertEquals(42, newInstance.aField)
        assertEquals(69, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.IMMEDIATE, newInstance.addressModeB)
        assertEquals(Modifier.A, newInstance.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val jmz = Jmz(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance = jmz.newInstance(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)

        assert(newInstance is Jmz)
        assertEquals(69, newInstance.aField)
        assertEquals(1, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeB)
        assertEquals(Modifier.B, newInstance.modifier)
    }

    @Test
    fun testDeepCopy() {
        val jmz = Jmz(42, 0, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val copy = jmz.deepCopy()

        assertEquals(jmz.aField, copy.aField)
        assertEquals(jmz.bField, copy.bField)
        assertEquals(jmz.addressModeA, copy.addressModeA)
        assertEquals(jmz.addressModeB, copy.addressModeB)
        assertEquals(jmz.modifier, copy.modifier)
        assert(jmz !== copy)
    }
}
