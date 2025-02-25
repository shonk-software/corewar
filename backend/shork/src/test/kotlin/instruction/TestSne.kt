package instruction

import getDefaultInternalSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.internal.InternalShork
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier
import software.shonk.interpreter.internal.instruction.Dat
import software.shonk.interpreter.internal.instruction.Sne
import software.shonk.interpreter.internal.process.Process
import software.shonk.interpreter.internal.program.Program

internal class TestSne {
    private val dat = Dat(0, 0, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
    private val settings = getDefaultInternalSettings(dat)
    private var shork = InternalShork(settings)
    private var program = Program("sne", shork)
    private var process = Process(program, 0)

    @BeforeEach
    fun setup() {
        shork = InternalShork(settings)
        program = Program("sne", shork)
        process = Process(program, 0)
    }

    private fun executeSneAndAssertCounter(sne: Sne, expectedCounterIncrement: Int) {
        sne.execute(process, shork.memoryCore.resolveFields(0))
        assertEquals(expectedCounterIncrement, process.programCounter)
    }

    private fun setupSne(modifier: Modifier): Sne {
        val sne = Sne(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, modifier)
        shork.memoryCore.storeAbsolute(0, sne)
        return sne
    }

    private fun setupMemory(aField: Int, bField: Int, address: Int) {
        val dat = Dat(aField, bField, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.A)
        shork.memoryCore.storeAbsolute(address, dat)
    }

    @Test
    fun `test modifier A fields equal`() {
        val sne = setupSne(Modifier.A)
        setupMemory(42, 0, 1)
        setupMemory(42, 69, 2)
        executeSneAndAssertCounter(sne, 0)
    }

    @Test
    fun `test modifier A fields not equal`() {
        val sne = setupSne(Modifier.A)
        setupMemory(40, 0, 1)
        setupMemory(42, 69, 2)
        executeSneAndAssertCounter(sne, 1)
    }

    @Test
    fun `test modifier B fields equal`() {
        val sne = setupSne(Modifier.B)
        setupMemory(0, 69, 1)
        setupMemory(42, 69, 2)
        executeSneAndAssertCounter(sne, 0)
    }

    @Test
    fun `test modifier B fields not equal`() {
        val sne = setupSne(Modifier.B)
        setupMemory(0, 40, 1)
        setupMemory(42, 69, 2)
        executeSneAndAssertCounter(sne, 1)
    }

    @Test
    fun `test modifier F fields equal`() {
        val sne = setupSne(Modifier.F)
        setupMemory(42, 69, 1)
        setupMemory(42, 69, 2)
        executeSneAndAssertCounter(sne, 0)
    }

    @Test
    fun `test modifier F with A Fields not equal`() {
        val sne = setupSne(Modifier.F)
        setupMemory(42, 69, 1)
        setupMemory(69, 69, 2)
        executeSneAndAssertCounter(sne, 1)
    }

    @Test
    fun `test modifier F with B Fields not equal`() {
        val sne = setupSne(Modifier.F)
        setupMemory(42, 69, 1)
        setupMemory(42, 70, 2)
        executeSneAndAssertCounter(sne, 1)
    }

    @Test
    fun `test modifier I equal`() {
        val sne = setupSne(Modifier.I)
        setupMemory(42, 69, 1)
        setupMemory(42, 69, 2)
        executeSneAndAssertCounter(sne, 0)
    }

    @Test
    fun `test modifier I not equal`() {
        val sne = setupSne(Modifier.I)
        setupMemory(42, 69, 1)
        setupMemory(69, 42, 2)
        executeSneAndAssertCounter(sne, 1)
    }

    @Test
    fun `test modifier AB equal`() {
        val sne = setupSne(Modifier.AB)
        setupMemory(42, 69, 1)
        setupMemory(69, 42, 2)
        executeSneAndAssertCounter(sne, 0)
    }

    @Test
    fun `test modifier AB not equal`() {
        val sne = setupSne(Modifier.AB)
        setupMemory(42, 69, 1)
        setupMemory(42, 69, 2)
        executeSneAndAssertCounter(sne, 1)
    }

    @Test
    fun `test modifier BA equal`() {
        val sne = setupSne(Modifier.BA)
        setupMemory(69, 42, 1)
        setupMemory(42, 69, 2)
        executeSneAndAssertCounter(sne, 0)
    }

    @Test
    fun `test modifier BA not equal`() {
        val sne = setupSne(Modifier.BA)
        setupMemory(69, 42, 1)
        setupMemory(69, 42, 2)
        executeSneAndAssertCounter(sne, 1)
    }

    @Test
    fun `test modifier X equal`() {
        val sne = setupSne(Modifier.X)
        setupMemory(42, 69, 1)
        setupMemory(69, 42, 2)
        executeSneAndAssertCounter(sne, 0)
    }

    @Test
    fun `test modifier X not equal`() {
        val sne = setupSne(Modifier.X)
        setupMemory(42, 69, 1)
        setupMemory(42, 69, 2)
        executeSneAndAssertCounter(sne, 1)
    }

    @Test
    fun `test modifier X not equal reversed`() {
        val sne = setupSne(Modifier.X)
        setupMemory(42, 69, 1)
        setupMemory(70, 42, 2)
        executeSneAndAssertCounter(sne, 1)
    }

    @Test
    fun testNewInstanceSameValues() {
        val sne = Sne(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        val copy = sne.newInstance(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)

        assert(copy is Sne)
        assertEquals(copy.aField, sne.aField)
        assertEquals(copy.bField, sne.bField)
        assertEquals(copy.addressModeA, sne.addressModeA)
        assertEquals(copy.addressModeB, sne.addressModeB)
        assertEquals(copy.modifier, sne.modifier)
    }

    @Test
    fun testNewInstanceDifferentValues() {
        val sne = Sne(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.A)
        val copy = sne.newInstance(2, 1, AddressMode.IMMEDIATE, AddressMode.IMMEDIATE, Modifier.B)

        assert(copy is Sne)
        assertEquals(2, copy.aField)
        assertEquals(1, copy.bField)
        assertEquals(AddressMode.IMMEDIATE, copy.addressModeA)
        assertEquals(AddressMode.IMMEDIATE, copy.addressModeB)
        assertEquals(Modifier.B, copy.modifier)
    }

    @Test
    fun testDeepCopy() {
        val sne = Sne(42, 69, AddressMode.DIRECT, AddressMode.IMMEDIATE, Modifier.A)
        val copy = sne.deepCopy()

        assertEquals(sne.aField, copy.aField)
        assertEquals(sne.bField, copy.bField)
        assertEquals(sne.addressModeA, copy.addressModeA)
        assertEquals(sne.addressModeB, copy.addressModeB)
        assertEquals(sne.modifier, copy.modifier)
        assert(sne !== copy)
    }
}
