package software.shonk.interpreter.internal.instruction

import software.shonk.interpreter.internal.addressing.AddressMode
import software.shonk.interpreter.internal.addressing.Modifier

internal class Div(
    aField: Int,
    bField: Int,
    addressModeA: AddressMode,
    addressModeB: AddressMode,
    modifier: Modifier,
) : AbstractArithmeticInstruction(aField, bField, addressModeA, addressModeB, modifier) {

    override fun runArithmeticOperation(
        sourceInstructionOperand: Int,
        destinationInstructionOperand: Int,
    ): Int {
        if (sourceInstructionOperand == 0) {
            throw ArithmeticException("Divide by zero")
        }
        return destinationInstructionOperand / sourceInstructionOperand
    }

    override fun newInstance(
        aField: Int,
        bField: Int,
        addressModeA: AddressMode,
        addressModeB: AddressMode,
        modifier: Modifier,
    ): AbstractInstruction {
        return Div(aField, bField, addressModeA, addressModeB, modifier)
    }

    override fun deepCopy(): AbstractInstruction {
        return Div(aField, bField, addressModeA, addressModeB, modifier)
    }
}
