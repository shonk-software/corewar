package memory

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

internal class TestAddressMode {
    private val dat = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    private val settings = getDefaultInternalSettings(dat)
    private var shork = InternalShork(settings)
    private var memoryCore = shork.memoryCore
    private var program = Program("add", shork)
    private var process = Process(program, 0)

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
        program = Program("add", shork)
        process = Process(program, 0)
        memoryCore = shork.memoryCore
    }

    @Test
    fun `test direct`() {
        val add = Add(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        val dat = Dat(42, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        shork.memoryCore.storeAbsolute(0, add)
        shork.memoryCore.storeAbsolute(1, dat)

        add.execute(process, memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assertEquals(42, resultInstruction.aField)
    }

    @Test
    fun `test immediate`() {
        val add = Add(1, 5, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        shork.memoryCore.storeAbsolute(0, add)

        add.execute(process, memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(0)

        assert(resultInstruction is Add)
        assertEquals(2, resultInstruction.aField)
    }

    @Test
    fun `test A-Indirect`() {
        val add = Add(1, 1, AddressMode.A_INDIRECT, AddressMode.A_INDIRECT, Modifier.A)
        val dat = Dat(1, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val dat2 = Dat(2, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        shork.memoryCore.storeAbsolute(0, add)
        shork.memoryCore.storeAbsolute(1, dat)
        shork.memoryCore.storeAbsolute(2, dat2)

        add.execute(process, memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assertEquals(4, resultInstruction.aField)
    }

    @Test
    fun `test B-Indirect`() {
        val add = Add(1, 1, AddressMode.B_INDIRECT, AddressMode.B_INDIRECT, Modifier.A)
        val dat = Dat(0, 1, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val dat2 = Dat(2, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        shork.memoryCore.storeAbsolute(0, add)
        shork.memoryCore.storeAbsolute(1, dat)
        shork.memoryCore.storeAbsolute(2, dat2)

        add.execute(process, memoryCore.resolveFields(0))

        val resultInstruction = shork.memoryCore.loadAbsolute(2)

        assert(resultInstruction is Dat)
        assertEquals(4, resultInstruction.aField)
    }

    @Test
    fun `test A-Pre-Decrement`() {
        val dat = Dat(3, 2, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val add = Add(-1, 3, AddressMode.A_PRE_DECREMENT, AddressMode.IMMEDIATE, Modifier.AB)
        val dat2 = Dat(7, 42, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        shork.memoryCore.storeAbsolute(0, dat)
        shork.memoryCore.storeAbsolute(1, add)
        shork.memoryCore.storeAbsolute(2, dat2)

        add.execute(process, memoryCore.resolveFields(1))

        val datResult = shork.memoryCore.loadAbsolute(0)

        assert(datResult is Dat)
        assertEquals(2, datResult.aField)
        assertEquals(2, datResult.bField)

        val addResult = shork.memoryCore.loadAbsolute(1)

        assert(addResult is Add)
        assertEquals(-1, addResult.aField)
        assertEquals(10, addResult.bField)

        val dat2Result = shork.memoryCore.loadAbsolute(2)

        assert(dat2Result is Dat)
        assertEquals(7, dat2Result.aField)
        assertEquals(42, dat2Result.bField)
    }

    @Test
    fun `test B-Pre-Decrement`() {
        val dat = Dat(1987, 3, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val add = Add(-1, 3, AddressMode.B_PRE_DECREMENT, AddressMode.IMMEDIATE, Modifier.AB)
        val dat2 = Dat(42, 7, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        shork.memoryCore.storeAbsolute(0, dat)
        shork.memoryCore.storeAbsolute(1, add)
        shork.memoryCore.storeAbsolute(2, dat2)

        add.execute(process, memoryCore.resolveFields(1))

        val datResult = shork.memoryCore.loadAbsolute(0)

        assert(datResult is Dat)
        assertEquals(1987, datResult.aField)
        assertEquals(2, datResult.bField)

        val addResult = shork.memoryCore.loadAbsolute(1)

        assert(addResult is Add)
        assertEquals(-1, addResult.aField)
        assertEquals(45, addResult.bField)

        val dat2Result = shork.memoryCore.loadAbsolute(2)

        assert(dat2Result is Dat)
        assertEquals(42, dat2Result.aField)
        assertEquals(7, dat2Result.bField)
    }

    @Test
    fun `test A-Post-Increment`() {
        val dat = Dat(2, 1984, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val add = Add(-1, 3, AddressMode.A_POST_INCREMENT, AddressMode.IMMEDIATE, Modifier.AB)
        val dat2 = Dat(42, 7, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        shork.memoryCore.storeAbsolute(0, dat)
        shork.memoryCore.storeAbsolute(1, add)
        shork.memoryCore.storeAbsolute(2, dat2)

        add.execute(process, memoryCore.resolveFields(1))

        val datResult = shork.memoryCore.loadAbsolute(0)

        assert(datResult is Dat)
        assertEquals(3, datResult.aField)
        assertEquals(1984, datResult.bField)

        val addResult = shork.memoryCore.loadAbsolute(1)

        assert(addResult is Add)
        assertEquals(-1, addResult.aField)
        assertEquals(45, addResult.bField)

        val dat2Result = shork.memoryCore.loadAbsolute(2)

        assert(dat2Result is Dat)
        assertEquals(42, dat2Result.aField)
        assertEquals(7, dat2Result.bField)
    }

    @Test
    fun `test B-Post-Increment`() {
        val dat = Dat(451, 2, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val add = Add(69, -1, AddressMode.IMMEDIATE, AddressMode.B_POST_INCREMENT, Modifier.AB)
        val dat2 = Dat(42, 7, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        shork.memoryCore.storeAbsolute(0, dat)
        shork.memoryCore.storeAbsolute(1, add)
        shork.memoryCore.storeAbsolute(2, dat2)

        add.execute(process, memoryCore.resolveFields(1))

        val datResult = shork.memoryCore.loadAbsolute(0)

        assert(datResult is Dat)
        assertEquals(451, datResult.aField)
        assertEquals(3, datResult.bField)

        val addResult = shork.memoryCore.loadAbsolute(1)

        assert(addResult is Add)
        assertEquals(69, addResult.aField)
        assertEquals(-1, addResult.bField)

        val dat2Result = shork.memoryCore.loadAbsolute(2)

        assert(dat2Result is Dat)
        assertEquals(42, dat2Result.aField)
        assertEquals(76, dat2Result.bField)
    }
}
