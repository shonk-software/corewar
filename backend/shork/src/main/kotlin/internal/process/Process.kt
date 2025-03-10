package software.shonk.interpreter.internal.process

import software.shonk.interpreter.internal.program.AbstractProgram

internal class Process(program: AbstractProgram, programCounter: Int) :
    AbstractProcess(program, programCounter) {
    override fun tick() {
        val instruction = this.program.shork.memoryCore.loadAbsolute(programCounter)
        val resolvedAddresses = this.program.shork.memoryCore.resolveFields(programCounter)
        instruction.execute(this, resolvedAddresses.first)
        resolvedAddresses.second.forEach { it.run() }
        if (!this.dontIncrementProgramCounter) {
            this.programCounter++
        }

        this.dontIncrementProgramCounter = false
    }
}
