package instruction

import getDefaultInternalSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.internal.InternalShork
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier
import software.shonk.interpreter.internal.instruction.Add
import software.shonk.interpreter.internal.instruction.Dat
import software.shonk.interpreter.internal.process.Process
import software.shonk.interpreter.internal.program.Program

internal class TestAdd {

    private val dat = Dat(5, 7, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    private val settings = getDefaultInternalSettings(dat)
    private var shork = InternalShork(settings)
    private var program = Program("add", shork)
    private var process = Process(program, 0)

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
        program = Program("add", shork)
        process = Process(program, 0)
    }

    @Test
    fun testExecuteA() {
        val add = Add(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        shork.memoryCore.storeAbsolute(0, add)

        add.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assertEquals(resultInstruction.aField, 10)
        assert(resultInstruction.bField == 7)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteB() {
        val add = Add(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)
        shork.memoryCore.storeAbsolute(0, add)

        add.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 5)
        assert(resultInstruction.bField == 14)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteAB() {
        val add = Add(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.AB)
        shork.memoryCore.storeAbsolute(0, add)

        add.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 5)
        assert(resultInstruction.bField == 12)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteBA() {
        val add = Add(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.BA)
        shork.memoryCore.storeAbsolute(0, add)

        add.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 12)
        assert(resultInstruction.bField == 7)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteF() {
        val add = Add(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        shork.memoryCore.storeAbsolute(0, add)

        add.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 10)
        assert(resultInstruction.bField == 14)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteI() {
        val add = Add(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        shork.memoryCore.storeAbsolute(0, add)

        add.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 10)
        assert(resultInstruction.bField == 14)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteX() {
        val add = Add(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.X)
        shork.memoryCore.storeAbsolute(0, add)

        add.execute(process, shork.memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 12)
        assert(resultInstruction.bField == 12)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testNewInstanceSameValues() {
        val add = Add(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance =
            add.newInstance(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)

        assert(newInstance is Add)
        assertEquals(42, newInstance.aField)
        assertEquals(69, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.IMMEDIATE, newInstance.addressModeB)
        assertEquals(Modifier.A, newInstance.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val add = Add(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance = add.newInstance(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)

        assert(newInstance is Add)
        assertEquals(1, newInstance.aField)
        assertEquals(2, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeB)
        assertEquals(Modifier.B, newInstance.modifier)
    }

    @Test
    fun testDeepCopy() {
        val add = Add(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val copy = add.deepCopy()

        assert(copy is Add)
        assertEquals(add.aField, copy.aField)
        assertEquals(add.bField, copy.bField)
        assertEquals(add.addressModeA, copy.addressModeA)
        assertEquals(add.addressModeB, copy.addressModeB)
        assertEquals(add.modifier, copy.modifier)
        assert(add !== copy)
    }
}
