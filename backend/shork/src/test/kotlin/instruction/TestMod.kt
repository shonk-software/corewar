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
import software.shonk.interpreter.internal.instruction.Mod
import software.shonk.interpreter.internal.process.Process
import software.shonk.interpreter.internal.program.Program

internal class TestMod {

    private val dat = Dat(5, 13, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    private val settings =
        getDefaultInternalSettings(dat, gameDataCollector = MockGameDataCollector())
    private var shork = InternalShork(settings)
    private var program = Program("mod", shork)
    private var process = Process(program, 0)

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
        program = Program("mod", shork)
        process = Process(program, 0)
    }

    @Test
    fun testExecuteA() {
        val mod = Mod(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        shork.memoryCore.storeAbsolute(0, mod)

        mod.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 0)
        assert(resultInstruction.bField == 13)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteB() {
        val mod = Mod(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)
        shork.memoryCore.storeAbsolute(0, mod)

        mod.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 5)
        assert(resultInstruction.bField == 0)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteAB() {
        val mod = Mod(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.AB)
        shork.memoryCore.storeAbsolute(0, mod)

        mod.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 5)
        assert(resultInstruction.bField == 3)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteBA() {
        val mod = Mod(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.BA)
        shork.memoryCore.storeAbsolute(0, mod)

        mod.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 5)
        assert(resultInstruction.bField == 13)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteF() {
        val mod = Mod(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        shork.memoryCore.storeAbsolute(0, mod)

        mod.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 0)
        assert(resultInstruction.bField == 0)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteI() {
        val mod = Mod(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        shork.memoryCore.storeAbsolute(0, mod)

        mod.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 0)
        assert(resultInstruction.bField == 0)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteX() {
        val mod = Mod(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.X)
        shork.memoryCore.storeAbsolute(0, mod)

        mod.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 5)
        assert(resultInstruction.bField == 3)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testModByZero() {
        val mod = Mod(0, 1, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.F)
        shork.memoryCore.storeAbsolute(0, mod)

        program.createProcessAt(0)
        program.tick()

        assert(process.program.processes.isEmpty())
    }

    @Test
    fun testNewInstanceSameValues() {
        val mod = Mod(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance =
            mod.newInstance(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)

        assert(newInstance is Mod)
        assertEquals(mod.aField, newInstance.aField)
        assertEquals(mod.bField, newInstance.bField)
        assertEquals(mod.addressModeA, newInstance.addressModeA)
        assertEquals(mod.addressModeB, newInstance.addressModeB)
        assertEquals(mod.modifier, newInstance.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val mod = Mod(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance = mod.newInstance(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)

        assert(newInstance is Mod)
        assertEquals(1, newInstance.aField)
        assertEquals(2, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeB)
        assertEquals(Modifier.B, newInstance.modifier)
    }

    @Test
    fun div() {
        val mod = Mod(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val copy = mod.deepCopy()

        assert(copy is Mod)
        assertEquals(mod.aField, copy.aField)
        assertEquals(mod.bField, copy.bField)
        assertEquals(mod.addressModeA, copy.addressModeA)
        assertEquals(mod.addressModeB, copy.addressModeB)
        assertEquals(mod.modifier, copy.modifier)
        assert(mod !== copy)
    }
}
