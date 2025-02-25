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
import software.shonk.interpreter.internal.instruction.Div
import software.shonk.interpreter.internal.process.Process
import software.shonk.interpreter.internal.program.Program

internal class TestDiv {

    private val dat = Dat(5, 13, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    private val settings =
        getDefaultInternalSettings(dat, gameDataCollector = MockGameDataCollector())
    private var shork = InternalShork(settings)
    private var program = Program("div", shork)
    private var process = Process(program, 0)

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
        program = Program("div", shork)
        process = Process(program, 0)
    }

    @Test
    fun testExecuteA() {
        val div = Div(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        shork.memoryCore.storeAbsolute(0, div)

        div.execute(process, shork.memoryCore.resolveFields(0))

        var resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 1)
        assert(resultInstruction.bField == 13)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteB() {
        val div = Div(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)
        shork.memoryCore.storeAbsolute(0, div)

        div.execute(process, shork.memoryCore.resolveFields(0))

        var resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 5)
        assert(resultInstruction.bField == 1)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteAB() {
        val div = Div(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.AB)
        shork.memoryCore.storeAbsolute(0, div)

        div.execute(process, shork.memoryCore.resolveFields(0))

        var resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 5)
        assert(resultInstruction.bField == 2)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteBA() {
        val div = Div(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.BA)
        shork.memoryCore.storeAbsolute(0, div)

        div.execute(process, shork.memoryCore.resolveFields(0))

        var resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 0)
        assert(resultInstruction.bField == 13)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteF() {
        val div = Div(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.F)
        shork.memoryCore.storeAbsolute(0, div)

        div.execute(process, shork.memoryCore.resolveFields(0))

        var resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 1)
        assert(resultInstruction.bField == 1)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteI() {
        val div = Div(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        shork.memoryCore.storeAbsolute(0, div)

        div.execute(process, shork.memoryCore.resolveFields(0))

        var resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 1)
        assert(resultInstruction.bField == 1)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testExecuteX() {
        val div = Div(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.X)
        shork.memoryCore.storeAbsolute(0, div)

        div.execute(process, shork.memoryCore.resolveFields(0))

        var resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assert(resultInstruction.aField == 0)
        assert(resultInstruction.bField == 2)
        assert(resultInstruction.addressModeA == AddressMode.IMMEDIATE)
        assert(resultInstruction.addressModeB == AddressMode.IMMEDIATE)
    }

    @Test
    fun testDivideByZero() {
        val div = Div(0, 1, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.F)
        shork.memoryCore.storeAbsolute(0, div)

        program.createProcessAt(0)
        program.tick()

        assert(process.program.processes.isEmpty())
    }

    @Test
    fun testNewInstanceSameValues() {
        val div = Div(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val copy = div.newInstance(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)

        assert(copy is Div)
        assertEquals(div.aField, copy.aField)
        assertEquals(div.bField, copy.bField)
        assertEquals(div.addressModeA, copy.addressModeA)
        assertEquals(div.addressModeB, copy.addressModeB)
        assertEquals(div.modifier, copy.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val div = Div(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val copy = div.newInstance(1337, 37, AddressMode.IMMEDIATE, AddressMode.DIRECT, Modifier.B)

        assert(copy is Div)
        assertEquals(1337, copy.aField)
        assertEquals(37, copy.bField)
        assertEquals(AddressMode.IMMEDIATE, copy.addressModeA)
        assertEquals(AddressMode.DIRECT, copy.addressModeB)
        assertEquals(Modifier.B, copy.modifier)
    }

    @Test
    fun div() {
        val div = Div(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val copy = div.deepCopy()

        assert(copy is Div)
        assertEquals(div.aField, copy.aField)
        assertEquals(div.bField, copy.bField)
        assertEquals(div.addressModeA, copy.addressModeA)
        assertEquals(div.addressModeB, copy.addressModeB)
        assertEquals(div.modifier, copy.modifier)
        assert(div !== copy)
    }
}
