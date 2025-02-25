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
import software.shonk.interpreter.internal.instruction.Djn
import software.shonk.interpreter.internal.program.Program

internal class TestDjn {

    private val dat = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    private val settings =
        getDefaultInternalSettings(dat, gameDataCollector = MockGameDataCollector())
    private var shork = InternalShork(settings)
    private var program = Program("djn", shork)
    private var core = shork.memoryCore

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
        program = Program("djn", shork)
        program.createProcessAt(0)
        core = shork.memoryCore
    }

    @Test
    fun `test jump unsuccessful if value is zero after decrement with modifier a`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        var dat2 = Dat(1, 42, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)

        dat2 = core.loadAbsolute(1) as Dat
        assertEquals(0, dat2.aField)
        assertEquals(42, dat2.bField)
    }

    @Test
    fun `test jump unsuccessful if value is zero after decrement with modifier ba`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.BA)
        var dat2 = Dat(1, 42, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)

        dat2 = core.loadAbsolute(1) as Dat
        assertEquals(0, dat2.aField)
        assertEquals(42, dat2.bField)
    }

    @Test
    fun `test jump unsuccessful if value is zero after decrement with modifier b`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)
        var dat2 = Dat(42, 1, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)

        dat2 = core.loadAbsolute(1) as Dat
        assertEquals(42, dat2.aField)
        assertEquals(0, dat2.bField)
    }

    @Test
    fun `test jump unsuccessful if value is zero after decrement with modifier ab`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.AB)
        var dat2 = Dat(42, 1, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)

        dat2 = core.loadAbsolute(1) as Dat
        assertEquals(42, dat2.aField)
        assertEquals(0, dat2.bField)
    }

    @Test
    fun `test jump unsuccessful if only one value is non-zero after decrement with modifier f`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        var dat2 = Dat(2, 1, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)

        dat2 = core.loadAbsolute(1) as Dat
        assertEquals(1, dat2.aField)
        assertEquals(0, dat2.bField)
    }

    @Test
    fun `test jump unsuccessful if only one value is non-zero with modifier x`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.X)
        var dat2 = Dat(2, 1, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)

        dat2 = core.loadAbsolute(1) as Dat
        assertEquals(1, dat2.aField)
        assertEquals(0, dat2.bField)
    }

    @Test
    fun `test jump unsuccessful if only one value is non-zero with modifier i`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        var dat2 = Dat(2, 1, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(1, program.processes.get().programCounter)

        dat2 = core.loadAbsolute(1) as Dat
        assertEquals(1, dat2.aField)
        assertEquals(0, dat2.bField)
    }

    @Test
    fun `test jump successful if value is non-zero after decrement with modifier a`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        var dat2 = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)

        dat2 = core.loadAbsolute(1) as Dat
        assertEquals(41, dat2.aField)
        assertEquals(0, dat2.bField)
    }

    @Test
    fun `test jump successful if value is non-zero after decrement with modifier ba`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.BA)
        var dat2 = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)

        dat2 = core.loadAbsolute(1) as Dat
        assertEquals(41, dat2.aField)
        assertEquals(0, dat2.bField)
    }

    @Test
    fun `test jump successful if value is non-zero after decrement with modifier b`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)
        var dat2 = Dat(0, 42, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)

        dat2 = core.loadAbsolute(1) as Dat
        assertEquals(0, dat2.aField)
        assertEquals(41, dat2.bField)
    }

    @Test
    fun `test jump successful if value is non-zero after decrement with modifier ab`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.AB)
        var dat2 = Dat(0, 42, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)

        dat2 = program.shork.memoryCore.loadAbsolute(1) as Dat
        assertEquals(0, dat2.aField)
        assertEquals(41, dat2.bField)
    }

    @Test
    fun `test jump successful if both values are non-zero after decrement with modifier f`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        var dat2 = Dat(42, 3, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)

        dat2 = core.loadAbsolute(1) as Dat
        assertEquals(41, dat2.aField)
        assertEquals(2, dat2.bField)
    }

    @Test
    fun `test jump successful if both values are non-zero after decrement with modifier i`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        var dat2 = Dat(42, 3, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)

        dat2 = core.loadAbsolute(1) as Dat
        assertEquals(41, dat2.aField)
        assertEquals(2, dat2.bField)
    }

    @Test
    fun `test jump successful if both values are non-zero after decrement with modifier x`() {
        val djn = Djn(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.X)
        var dat2 = Dat(42, 3, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        core.storeAbsolute(0, djn)
        core.storeAbsolute(1, dat2)
        program.tick()

        assertEquals(69, program.processes.get().programCounter)

        dat2 = core.loadAbsolute(1) as Dat
        assertEquals(41, dat2.aField)
        assertEquals(2, dat2.bField)
    }

    @Test
    fun testNewInstanceSameValues() {
        val djn = Djn(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance =
            djn.newInstance(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)

        assert(newInstance is Djn)
        assertEquals(djn.aField, newInstance.aField)
        assertEquals(djn.bField, newInstance.bField)
        assertEquals(djn.addressModeA, newInstance.addressModeA)
        assertEquals(djn.addressModeB, newInstance.addressModeB)
        assertEquals(djn.modifier, newInstance.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val djn = Djn(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance = djn.newInstance(1, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)

        assert(newInstance is Djn)
        assertEquals(1, newInstance.aField)
        assertEquals(1, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeB)
        assertEquals(Modifier.B, newInstance.modifier)
    }

    @Test
    fun testDeepCopy() {
        val djn = Djn(42, 0, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val copy = djn.deepCopy()

        assertEquals(djn.aField, copy.aField)
        assertEquals(djn.bField, copy.bField)
        assertEquals(djn.addressModeA, copy.addressModeA)
        assertEquals(djn.addressModeB, copy.addressModeB)
        assertEquals(djn.modifier, copy.modifier)
        assert(djn !== copy)
    }
}
