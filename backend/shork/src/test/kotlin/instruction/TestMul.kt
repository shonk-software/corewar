package instruction

import getDefaultInternalSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.internal.InternalShork
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier
import software.shonk.interpreter.internal.instruction.Dat
import software.shonk.interpreter.internal.instruction.Mul
import software.shonk.interpreter.internal.process.Process
import software.shonk.interpreter.internal.program.Program

internal class TestMul {

    private val dat = Dat(5, 7, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    private val settings = getDefaultInternalSettings(dat)
    private var shork = InternalShork(settings)
    private var program = Program("mul", shork)
    private var process = Process(program, 0)

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
        program = Program("mul", shork)
        process = Process(program, 0)
    }

    @Test
    fun testExecuteA() {
        val mul = Mul(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        shork.memoryCore.storeAbsolute(0, mul)

        mul.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 25)
        assert(resultInstruction.bField == 7)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteB() {
        val mul = Mul(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)
        shork.memoryCore.storeAbsolute(0, mul)

        mul.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 5)
        assert(resultInstruction.bField == 49)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteAB() {
        val mul = Mul(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.AB)
        shork.memoryCore.storeAbsolute(0, mul)

        mul.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 5)
        assert(resultInstruction.bField == 35)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteBA() {
        val mul = Mul(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.BA)
        shork.memoryCore.storeAbsolute(0, mul)

        mul.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 35)
        assert(resultInstruction.bField == 7)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteF() {
        val mul = Mul(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        shork.memoryCore.storeAbsolute(0, mul)

        mul.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 25)
        assert(resultInstruction.bField == 49)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteI() {
        val mul = Mul(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        shork.memoryCore.storeAbsolute(0, mul)

        mul.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 25)
        assert(resultInstruction.bField == 49)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteX() {
        val mul = Mul(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.X)
        shork.memoryCore.storeAbsolute(0, mul)

        mul.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 35)
        assert(resultInstruction.bField == 35)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testNewInstanceSameValues() {
        val mul = Mul(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        val copy = mul.newInstance(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)

        assert(copy is Mul)
        assertEquals(mul.aField, copy.aField)
        assertEquals(mul.bField, copy.bField)
        assertEquals(mul.addressModeA, copy.addressModeA)
        assertEquals(mul.addressModeB, copy.addressModeB)
        assertEquals(mul.modifier, copy.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val mul = Mul(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        val copy = mul.newInstance(2, 1, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.B)

        assert(copy is Mul)
        assertEquals(2, copy.aField)
        assertEquals(1, copy.bField)
        assertEquals(AddressMode.IMMEDIATE, copy.addressModeA)
        assertEquals(AddressMode.IMMEDIATE, copy.addressModeB)
        assertEquals(Modifier.B, copy.modifier)
    }

    @Test
    fun testDeepCopy() {
        val mul = Mul(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val copy = mul.deepCopy()

        assert(copy is Mul)
        assertEquals(mul.aField, copy.aField)
        assertEquals(mul.bField, copy.bField)
        assertEquals(mul.addressModeA, copy.addressModeA)
        assertEquals(mul.addressModeB, copy.addressModeB)
        assertEquals(mul.modifier, copy.modifier)
        assert(mul !== copy)
    }
}
