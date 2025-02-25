package memory

import getDefaultInternalSettings
import mocks.MockGameDataCollector
import mocks.MockInstruction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.internal.InternalShork
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier
import software.shonk.interpreter.internal.memory.MemoryCore
import software.shonk.interpreter.internal.program.Program

internal class TestMemoryCore {
    val defaultInstruction =
        MockInstruction(42, 69, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
    val settings =
        getDefaultInternalSettings(defaultInstruction, gameDataCollector = MockGameDataCollector())

    var memoryCore = MemoryCore(8000, settings)

    @BeforeEach
    fun setup() {
        memoryCore = MemoryCore(8000, settings)
    }

    @Test
    fun testInitializesCorrectly() {
        for (i in 0 until 8000) {
            val instruction = memoryCore.loadAbsolute(i)
            assert(instruction !== defaultInstruction)
        }
    }

    @Test
    fun testReadWriteInBounds() {
        val instruction = MockInstruction(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        memoryCore.storeAbsolute(42, instruction)
        val out = memoryCore.loadAbsolute(42)

        assertNotEquals(defaultInstruction, out)
        assertEquals(instruction, out)
    }

    @Test
    fun testReadWriteOutOfBoundsPositive() {
        val memoryCore = MemoryCore(10, settings)

        val instruction = MockInstruction(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        memoryCore.storeAbsolute(20, instruction)
        val out = memoryCore.loadAbsolute(20)

        assertNotEquals(defaultInstruction, out)
        assertEquals(instruction, out)
    }

    @Test
    fun testReadWriteOutOfBoundsNegative() {
        val memoryCore = MemoryCore(10, settings)

        val instruction = MockInstruction(1, 2, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        memoryCore.storeAbsolute(-13, instruction)
        val out = memoryCore.loadAbsolute(7)

        assertNotEquals(defaultInstruction, out)
        assertEquals(instruction, out)
    }

    @Test
    fun `test resolve bounds positive`() {
        val defaultInstruction =
            MockInstruction(18, 18, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        val memoryCore =
            MemoryCore(
                100,
                getDefaultInternalSettings(
                    defaultInstruction,
                    readDistance = 10,
                    writeDistance = 10,
                ),
            )

        var resolvedAddresses = memoryCore.resolveFields(0)

        assertEquals(98, resolvedAddresses.aFieldRead)
        assertEquals(98, resolvedAddresses.bFieldRead)
        assertEquals(98, resolvedAddresses.aFieldWrite)
        assertEquals(98, resolvedAddresses.bFieldWrite)

        resolvedAddresses = memoryCore.resolveFields(5)

        assertEquals(3, resolvedAddresses.aFieldRead)
        assertEquals(3, resolvedAddresses.bFieldRead)
        assertEquals(3, resolvedAddresses.aFieldWrite)
        assertEquals(3, resolvedAddresses.bFieldWrite)

        resolvedAddresses = memoryCore.resolveFields(10)

        assertEquals(8, resolvedAddresses.aFieldRead)
        assertEquals(8, resolvedAddresses.bFieldRead)
        assertEquals(8, resolvedAddresses.aFieldWrite)
        assertEquals(8, resolvedAddresses.bFieldWrite)

        resolvedAddresses = memoryCore.resolveFields(99)

        assertEquals(97, resolvedAddresses.aFieldRead)
        assertEquals(97, resolvedAddresses.bFieldRead)
        assertEquals(97, resolvedAddresses.aFieldWrite)
        assertEquals(97, resolvedAddresses.bFieldWrite)
    }

    @Test
    fun `test resolve bounds negative`() {
        val defaultInstruction =
            MockInstruction(-18, -18, AddressMode.DIRECT, AddressMode.DIRECT, Modifier.I)
        val memoryCore =
            MemoryCore(
                100,
                getDefaultInternalSettings(
                    defaultInstruction,
                    readDistance = 10,
                    writeDistance = 10,
                ),
            )

        var resolvedAddresses = memoryCore.resolveFields(0)

        assertEquals(2, resolvedAddresses.aFieldRead)
        assertEquals(2, resolvedAddresses.bFieldRead)
        assertEquals(2, resolvedAddresses.aFieldWrite)
        assertEquals(2, resolvedAddresses.bFieldWrite)

        resolvedAddresses = memoryCore.resolveFields(5)

        assertEquals(7, resolvedAddresses.aFieldRead)
        assertEquals(7, resolvedAddresses.bFieldRead)
        assertEquals(7, resolvedAddresses.aFieldWrite)
        assertEquals(7, resolvedAddresses.bFieldWrite)

        resolvedAddresses = memoryCore.resolveFields(10)

        assertEquals(12, resolvedAddresses.aFieldRead)
        assertEquals(12, resolvedAddresses.bFieldRead)
        assertEquals(12, resolvedAddresses.aFieldWrite)
        assertEquals(12, resolvedAddresses.bFieldWrite)

        resolvedAddresses = memoryCore.resolveFields(99)

        assertEquals(1, resolvedAddresses.aFieldRead)
        assertEquals(1, resolvedAddresses.bFieldRead)
        assertEquals(1, resolvedAddresses.aFieldWrite)
        assertEquals(1, resolvedAddresses.bFieldWrite)
    }

    @Test
    fun `test if the memory core integrates with the Game Data Collector`() {
        val settings = getDefaultInternalSettings(defaultInstruction)
        val shork = InternalShork(settings)
        val memoryCore = shork.memoryCore
        val gameDataCollector = shork.gameDataCollector
        val program = Program("Test", shork)
        program.createProcessAt(0)
        val process = program.processes.first()

        gameDataCollector.startRoundForProgram(program)
        gameDataCollector.collectProcessDataBeforeTick(process)

        memoryCore.loadAbsolute(42)
        memoryCore.loadAbsolute(0)
        memoryCore.loadAbsolute(4200)

        memoryCore.storeAbsolute(42, defaultInstruction)
        memoryCore.storeAbsolute(1337, defaultInstruction)
        memoryCore.storeAbsolute(666, defaultInstruction)

        gameDataCollector.collectProcessDataAfterTick(process)
        gameDataCollector.endRoundForProgram(program)

        val reads = gameDataCollector.getGameStatistics().map { it.memoryReads }.flatten()
        assertEquals(listOf(42, 0, 4200), reads)

        val writes = gameDataCollector.getGameStatistics().map { it.memoryWrites }.flatten()
        assertEquals(listOf(42, 1337, 666), writes)
    }
}
