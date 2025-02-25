package instruction

import getDefaultInternalSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.internal.InternalShork
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier
import software.shonk.interpreter.internal.instruction.Dat
import software.shonk.interpreter.internal.instruction.Sub
import software.shonk.interpreter.internal.process.Process
import software.shonk.interpreter.internal.program.Program

// todo refactor vanilla kotlin asserts to matching JUNIT5 asserts
internal class TestSub {

    private val dat = Dat(5, 7, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    private val settings = getDefaultInternalSettings(dat)
    private var shork = InternalShork(settings)
    private var program = Program("sub", shork)
    private var process = Process(program, 0)

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
        program = Program("sub", shork)
        process = Process(program, 0)
    }

    @Test
    fun testExecuteA() {
        val sub = Sub(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        shork.memoryCore.storeAbsolute(0, sub)

        sub.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 0)
        assert(resultInstruction.bField == 7)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteB() {
        val sub = Sub(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)
        shork.memoryCore.storeAbsolute(0, sub)

        sub.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 5)
        assert(resultInstruction.bField == 0)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteAB() {
        val sub = Sub(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.AB)
        shork.memoryCore.storeAbsolute(0, sub)

        sub.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 5)
        assert(resultInstruction.bField == 2)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteBA() {
        val sub = Sub(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.BA)
        shork.memoryCore.storeAbsolute(0, sub)

        sub.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == -2)
        assert(resultInstruction.bField == 7)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteF() {
        val sub = Sub(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        shork.memoryCore.storeAbsolute(0, sub)

        sub.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 0)
        assert(resultInstruction.bField == 0)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteI() {
        val sub = Sub(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        shork.memoryCore.storeAbsolute(0, sub)

        sub.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 0)
        assert(resultInstruction.bField == 0)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteX() {
        val sub = Sub(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.X)
        shork.memoryCore.storeAbsolute(0, sub)

        sub.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == -2)
        assert(resultInstruction.bField == 2)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testNewInstanceSameValues() {
        val sub = Sub(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance =
            sub.newInstance(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)

        assert(newInstance is Sub)
        assertEquals(sub.aField, newInstance.aField)
        assertEquals(sub.bField, newInstance.bField)
        assertEquals(sub.addressModeA, newInstance.addressModeA)
        assertEquals(sub.addressModeB, newInstance.addressModeB)
        assertEquals(sub.modifier, newInstance.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val sub = Sub(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance = sub.newInstance(1, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)

        assert(newInstance is Sub)
        assertEquals(1, newInstance.aField)
        assertEquals(1, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeB)
        assertEquals(Modifier.B, newInstance.modifier)
    }

    @Test
    fun testDeepCopy() {
        val sub = Sub(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val copy = sub.deepCopy()

        assert(copy is Sub)
        assertEquals(sub.aField, copy.aField)
        assertEquals(sub.bField, copy.bField)
        assertEquals(sub.addressModeA, copy.addressModeA)
        assertEquals(sub.addressModeB, copy.addressModeB)
        assertEquals(sub.modifier, copy.modifier)
        assert(sub !== copy)
    }
}
