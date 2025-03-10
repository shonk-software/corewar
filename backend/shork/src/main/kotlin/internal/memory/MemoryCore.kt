package software.shonk.interpreter.internal.memory

import software.shonk.interpreter.internal.PostExecuteAction
import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.instruction.AbstractInstruction
import software.shonk.interpreter.internal.settings.InternalSettings

internal class MemoryCore(val memorySize: Int, val settings: InternalSettings) : ICore {
    private val memory: Array<AbstractInstruction> =
        Array(memorySize) { settings.initialInstruction.deepCopy() }

    override fun loadAbsolute(address: Int): AbstractInstruction {
        val resolvedAddress = resolvedAddressBounds(address)
        settings.gameDataCollector.collectMemoryRead(resolvedAddress)
        return memory[resolvedAddress]
    }

    override fun storeAbsolute(address: Int, instruction: AbstractInstruction) {
        val resolvedAddress = resolvedAddressBounds(address)
        settings.gameDataCollector.collectMemoryWrite(resolvedAddress, instruction)
        memory[resolvedAddress] = instruction
    }

    private fun resolvedAddressBounds(address: Int): Int {
        return if (address < 0) {
            val negativeIndex = address % memory.size
            (memory.size + negativeIndex) % memory.size
        } else {
            address % memory.size
        }
    }

    /**
     * Resolve the absolute address of a field based on the address mode and the content of the
     * field. In case the AddressMode is one of the Pre/Post In/Decrement modes, the resolve
     * function **will** modify the destination field.
     *
     * @param sourceAddress The address of the instruction that is executing the operation
     * @param field The field to resolve the address of, either the A or B field
     * @param mode The address mode to use, must be the address mode of the field (A or B)
     * @return The resolved absolute address
     */
    private fun resolve(
        sourceAddress: Int,
        field: Int,
        mode: AddressMode,
    ): Pair<Int, PostExecuteAction?> {
        val referenceAddress = sourceAddress + field // Address we are pointing to
        val instruction = loadAbsolute(referenceAddress)
        var postExecuteAction: PostExecuteAction? = null

        val addressOffset =
            when (mode) {
                AddressMode.IMMEDIATE -> {
                    0
                }
                AddressMode.DIRECT -> {
                    field
                }
                AddressMode.A_INDIRECT -> {
                    field + instruction.aField
                }
                AddressMode.B_INDIRECT -> {
                    field + instruction.bField
                }
                AddressMode.A_PRE_DECREMENT -> {
                    instruction.writeToMemory(this, referenceAddress) {
                        it.aField -= 1
                        field + it.aField
                    }
                }
                AddressMode.B_PRE_DECREMENT -> {
                    instruction.writeToMemory(this, referenceAddress) {
                        it.bField -= 1
                        field + it.bField
                    }
                }
                AddressMode.A_POST_INCREMENT -> {
                    postExecuteAction = PostExecuteAction {
                        // Load it again, because the instruction might have changed
                        val instr = loadAbsolute(referenceAddress)
                        instr.writeToMemory(this, referenceAddress) { it.aField += 1 }
                    }

                    field + instruction.aField
                }
                AddressMode.B_POST_INCREMENT -> {
                    postExecuteAction = PostExecuteAction {
                        // Load it again, because the instruction might have changed
                        val instr = loadAbsolute(referenceAddress)
                        instr.writeToMemory(this, referenceAddress) { it.bField += 1 }
                    }

                    field + instruction.bField
                }
            }

        return Pair(sourceAddress + addressOffset, postExecuteAction)
    }

    /**
     * Resolves all addresses that a given instruction can read or write from. It makes sure the
     * addresses are constricted to the maximum read and write distances.
     *
     * @param sourceAddress The address to resolve
     * @return All of the resolved addresses
     */
    override fun resolveFields(
        sourceAddress: Int
    ): Pair<ResolvedAddresses, List<PostExecuteAction>> {
        val sourceInstruction = loadAbsolute(sourceAddress)
        val aField = sourceInstruction.aField
        val bField = sourceInstruction.bField
        val aMode = sourceInstruction.addressModeA
        val bMode = sourceInstruction.addressModeB

        val (resolvedA, postActionA) = resolve(sourceAddress, aField, aMode)
        val (resolvedB, postActionB) = resolve(sourceAddress, bField, bMode)

        val resolvedARead =
            sourceAddress + resolveDistanceBounds(resolvedA - sourceAddress, settings.readDistance)
        val resolvedAWrite =
            sourceAddress + resolveDistanceBounds(resolvedA - sourceAddress, settings.writeDistance)
        val resolvedBRead =
            sourceAddress + resolveDistanceBounds(resolvedB - sourceAddress, settings.readDistance)
        val resolvedBWrite =
            sourceAddress + resolveDistanceBounds(resolvedB - sourceAddress, settings.writeDistance)

        return Pair(
            ResolvedAddresses(
                resolvedAddressBounds(resolvedARead),
                resolvedAddressBounds(resolvedAWrite),
                resolvedAddressBounds(resolvedBRead),
                resolvedAddressBounds(resolvedBWrite),
            ),
            listOfNotNull(postActionA, postActionB),
        )
    }

    /**
     * Constricts a read or write operation within specified maximum distance. Adapted from
     * http://www.koth.org/info/icws94.html#5.6
     *
     * @param offset The offset of the read or write operation
     * @param maxDistance The maximum distance of the operation
     * @return The resolved absolute address, constriced to the maximum distance
     */
    private fun resolveDistanceBounds(offset: Int, maxDistance: Int): Int {
        var result = offset % maxDistance
        // The implementation this is based on doesn't handle negative offsets properly,
        // so we have to make sure it is positive.
        if (result < 0) {
            result += maxDistance
        }
        if (result > (maxDistance / 2)) {
            result += memorySize - maxDistance
        }
        return result
    }
}
