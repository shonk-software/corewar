package instruction

import getDefaultInternalSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.internal.InternalShork
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier
import software.shonk.interpreter.internal.instruction.Dat
import software.shonk.interpreter.internal.instruction.Seq
import software.shonk.interpreter.internal.instruction.Slt
import software.shonk.interpreter.internal.process.Process
import software.shonk.interpreter.internal.program.Program

internal class TestSlt {
    private val dat = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    private val settings = getDefaultInternalSettings(dat)
    private var shork = InternalShork(settings)
    private var program = Program("slt", shork)
    private var process = Process(program, 0)

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
        program = Program("slt", shork)
        process = Process(program, 0)
    }

    private fun executeSltAndAssertCounter(slt: Slt, expectedCounterIncrement: Int) {
        slt.execute(process, shork.memoryCore.resolveFields(0))
        assertEquals(expectedCounterIncrement, process.programCounter)
    }

    private fun setupSlt(modifier: Modifier): Slt {
        val slt = Slt(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, modifier)
        shork.memoryCore.storeAbsolute(0, slt)
        return slt
    }

    private fun setupMemory(aField: Int, bField: Int, address: Int) {
        val dat = Dat(aField, bField, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        shork.memoryCore.storeAbsolute(address, dat)
    }

    @Test
    fun `test modifier A fields smaller`() {
        val slt = setupSlt(Modifier.A)
        setupMemory(42, 42, 1)
        setupMemory(69, 69, 2)
        executeSltAndAssertCounter(slt, 1)
    }

    @Test
    fun `test modifier A fields not smaller`() {
        val slt = setupSlt(Modifier.A)
        setupMemory(42, 69, 1)
        setupMemory(42, 69, 2)
        executeSltAndAssertCounter(slt, 0)
    }

    @Test
    fun `test modifier B fields smaller`() {
        val slt = setupSlt(Modifier.B)
        setupMemory(42, 42, 1)
        setupMemory(42, 69, 2)
        executeSltAndAssertCounter(slt, 1)
    }

    @Test
    fun `test modifier B fields not smaller`() {
        val slt = setupSlt(Modifier.B)
        setupMemory(42, 69, 1)
        setupMemory(42, 69, 2)
        executeSltAndAssertCounter(slt, 0)
    }

    @Test
    fun `test modifier AB fields smaller`() {
        val slt = setupSlt(Modifier.AB)
        setupMemory(42, 69, 1)
        setupMemory(42, 69, 2)
        executeSltAndAssertCounter(slt, 1)
    }

    @Test
    fun `test modifier AB fields not smaller`() {
        val slt = setupSlt(Modifier.AB)
        setupMemory(42, 69, 1)
        setupMemory(69, 42, 2)
        executeSltAndAssertCounter(slt, 0)
    }

    @Test
    fun `test modifier BA fields smaller`() {
        val slt = setupSlt(Modifier.BA)
        setupMemory(42, 42, 1)
        setupMemory(69, 69, 2)
        executeSltAndAssertCounter(slt, 1)
    }

    @Test
    fun `test modifier BA fields not smaller`() {
        val slt = setupSlt(Modifier.BA)
        setupMemory(42, 69, 1)
        setupMemory(42, 69, 2)
        executeSltAndAssertCounter(slt, 0)
    }

    @Test
    fun `test modifier F fields smaller`() {
        val slt = setupSlt(Modifier.F)
        setupMemory(42, 42, 1)
        setupMemory(69, 69, 2)
        executeSltAndAssertCounter(slt, 1)
    }

    @Test
    fun `test modifier I fields smaller`() {
        val slt = setupSlt(Modifier.I)
        setupMemory(42, 42, 1)
        setupMemory(69, 69, 2)
        executeSltAndAssertCounter(slt, 1)
    }

    @Test
    fun `test modifier F fields not smaller`() {
        val slt = setupSlt(Modifier.F)
        setupMemory(42, 69, 1)
        setupMemory(69, 42, 2)
        executeSltAndAssertCounter(slt, 0)
    }

    @Test
    fun `test modifier I fields not smaller`() {
        val slt = setupSlt(Modifier.I)
        setupMemory(42, 69, 1)
        setupMemory(42, 69, 2)
        executeSltAndAssertCounter(slt, 0)
    }

    @Test
    fun `test modifier X fields smaller`() {
        val slt = setupSlt(Modifier.X)
        setupMemory(42, 42, 1)
        setupMemory(69, 69, 2)
        executeSltAndAssertCounter(slt, 1)
    }

    @Test
    fun `test modifier X fields not smaller`() {
        val slt = setupSlt(Modifier.X)
        setupMemory(42, 69, 1)
        setupMemory(42, 69, 2)
        executeSltAndAssertCounter(slt, 0)
    }

    @Test
    fun testNewInstanceSameValues() {
        val slt = Slt(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance =
            slt.newInstance(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)

        assert(newInstance is Slt)
        assertEquals(slt.aField, newInstance.aField)
        assertEquals(slt.bField, newInstance.bField)
        assertEquals(slt.addressModeA, newInstance.addressModeA)
        assertEquals(slt.addressModeB, newInstance.addressModeB)
        assertEquals(slt.modifier, newInstance.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val slt = Slt(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        val newInstance = slt.newInstance(1, 1, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.B)

        assert(newInstance is Slt)
        assertEquals(1, newInstance.aField)
        assertEquals(1, newInstance.bField)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeA)
        assertEquals(AddressMode.DIRECT, newInstance.addressModeB)
        assertEquals(Modifier.B, newInstance.modifier)
    }

    @Test
    fun testDeepCopy() {
        val seq = Seq(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val copy = seq.deepCopy()

        assertEquals(seq.aField, copy.aField)
        assertEquals(seq.bField, copy.bField)
        assertEquals(seq.addressModeA, copy.addressModeA)
        assertEquals(seq.addressModeB, copy.addressModeB)
        assertEquals(seq.modifier, copy.modifier)
        assert(seq !== copy)
    }
}
