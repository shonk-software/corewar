package instruction

import getDefaultInternalSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.internal.InternalShork
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier
import software.shonk.interpreter.internal.instruction.Dat
import software.shonk.interpreter.internal.instruction.Mov
import software.shonk.interpreter.internal.process.Process
import software.shonk.interpreter.internal.program.Program

// todo refactor basic asserts to junit asserts
internal class TestMov {

    private val dat = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    private val settings = getDefaultInternalSettings(dat)
    private var shork = InternalShork(settings)
    private var program = Program("mov", shork)
    private var process = Process(program, 0)

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
        program = Program("mov", shork)
        process = Process(program, 0)
    }

    @Test
    fun testExecuteA() {
        val mov1 = Mov(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        val mov2 = Mov(42, 69, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        shork.memoryCore.storeAbsolute(0, mov1)
        shork.memoryCore.storeAbsolute(1, mov2)

        mov1.execute(process, shork.memoryCore.resolveFields(0))

        val movedInstruction = shork.memoryCore.loadAbsolute(2)

        assert(movedInstruction is Dat)
        assert(movedInstruction.aField == 42)
        assert(movedInstruction.bField == 0)
        assert(movedInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(movedInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteB() {
        val mov1 = Mov(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)
        val mov2 = Mov(42, 69, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)
        shork.memoryCore.storeAbsolute(0, mov1)
        shork.memoryCore.storeAbsolute(1, mov2)

        mov1.execute(process, shork.memoryCore.resolveFields(0))

        var movedInstruction = shork.memoryCore.loadAbsolute(2)

        assert(movedInstruction is Dat)
        assert(movedInstruction.aField == 0)
        assert(movedInstruction.bField == 69)
        assert(movedInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(movedInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteAB() {
        val mov1 = Mov(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.AB)
        val mov2 = Mov(42, 69, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.AB)
        shork.memoryCore.storeAbsolute(0, mov1)
        shork.memoryCore.storeAbsolute(1, mov2)

        mov1.execute(process, shork.memoryCore.resolveFields(0))

        var movedInstruction = shork.memoryCore.loadAbsolute(2)

        assert(movedInstruction is Dat)
        assert(movedInstruction.aField == 0)
        assert(movedInstruction.bField == 42)
        assert(movedInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(movedInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteBA() {
        val mov1 = Mov(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.BA)
        val mov2 = Mov(42, 69, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.BA)
        shork.memoryCore.storeAbsolute(0, mov1)
        shork.memoryCore.storeAbsolute(1, mov2)

        mov1.execute(process, shork.memoryCore.resolveFields(0))

        var movedInstruction = shork.memoryCore.loadAbsolute(2)

        assert(movedInstruction is Dat)
        assert(movedInstruction.aField == 69)
        assert(movedInstruction.bField == 0)
        assert(movedInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(movedInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteF() {
        val mov1 = Mov(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        val mov2 = Mov(42, 69, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        shork.memoryCore.storeAbsolute(0, mov1)
        shork.memoryCore.storeAbsolute(1, mov2)

        mov1.execute(process, shork.memoryCore.resolveFields(0))

        var movedInstruction = shork.memoryCore.loadAbsolute(2)

        assert(movedInstruction is Dat)
        assert(movedInstruction.aField == 42)
        assert(movedInstruction.bField == 69)
    }

    @Test
    fun testExecuteI() {
        val mov = Mov(0, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        shork.memoryCore.storeAbsolute(0, mov)

        mov.execute(process, shork.memoryCore.resolveFields(0))

        var movedInstruction = shork.memoryCore.loadAbsolute(1)

        assert(movedInstruction is Mov)
        assert(movedInstruction.aField == 0)
        assert(movedInstruction.bField == 1)
        assert(movedInstruction.addressModeA == AddressMode.DIRECT)
        assert(movedInstruction.addressModeB == AddressMode.DIRECT)
    }

    @Test
    fun testExecuteX() {
        val mov = Mov(0, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.X)
        shork.memoryCore.storeAbsolute(0, mov)

        mov.execute(process, shork.memoryCore.resolveFields(0))

        var movedInstruction = shork.memoryCore.loadAbsolute(1)

        assert(movedInstruction is Dat)
        assert(movedInstruction.aField == 1)
        assert(movedInstruction.bField == 0)
        assert(movedInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(movedInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testNewInstanceSameValues() {
        val mov = Mov(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance =
            mov.newInstance(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)

        assert(newInstance is Mov)
        assertEquals(42, newInstance.aField)
        assertEquals(69, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.IMMEDIATE, newInstance.addressModeB)
        assertEquals(Modifier.A, newInstance.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val mov = Mov(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance = mov.newInstance(69, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)

        assert(newInstance is Mov)
        assertEquals(69, newInstance.aField)
        assertEquals(1, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeB)
        assertEquals(Modifier.B, newInstance.modifier)
    }

    @Test
    fun testDeepCopy() {
        val mov = Mov(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val copy = mov.deepCopy()

        assertEquals(mov.aField, copy.aField)
        assertEquals(mov.bField, copy.bField)
        assertEquals(mov.addressModeA, copy.addressModeA)
        assertEquals(mov.addressModeB, copy.addressModeB)
        assertEquals(mov.modifier, copy.modifier)
        assert(mov !== copy)
    }
}
