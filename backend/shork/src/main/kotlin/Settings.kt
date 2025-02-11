package software.shonk.interpreter

import software.shonk.interpreter.internal.compiler.Compiler
import software.shonk.interpreter.internal.settings.InternalSettings

/**
 * The settings for the interpreter
 *
 * @param coreSize The size of the core, in number of instructions it will fit.
 * @param instructionLimit The maximum number of instructions will be executed before the
 *   interpreter stops. -1 for no limit
 */
class Settings(
    /** The size of the core, in number of instructions it will fit. */
    val coreSize: Int = 8192,
    /**
     * The maximum number of instructions will be executed before the interpreter stops and a draw
     * is called.
     */
    val instructionLimit: Int = 100000,
    /** The initial instruction the core will be filled with. */
    val initialInstruction: String = "DAT $0, $0",
    /**
     * The maximum number of cycles the interpreter will run before stopping. A cycle is an
     * execution of a single instruction of every players' program.
     */
    val maximumTicks: Int = 80000,

    /** The maximum number of processes that can be running at the same time. */
    val maximumProcessesPerPlayer: Int = 64,

    /** The maximum distance a read operation can access. */
    val readDistance: Int = coreSize,
    /** The maximum distance a write operation can access. */
    val writeDistance: Int = coreSize,

    /** The minimum separation between two processes. */
    val minimumSeparation: Int = 100,
    /** The number of instructions between two processes when created */
    val separation: Int = 100,
    /**
     * Whether the separation between two processes is random. `separation` is ignored if this is
     * true
     */
    val randomSeparation: Boolean = false,
) {
    internal fun toInternalSettings(): Result<InternalSettings> {
        val compiler = Compiler(initialInstruction)
        val instructions = compiler.instructions

        if (compiler.errorsOccured) {
            return Result.failure(
                IllegalArgumentException("Initital instruction is not a valid redcode instruction")
            )
        } else if (instructions.size > 1) {
            return Result.failure(
                IllegalArgumentException(
                    "Initial instruction cannot contain more than one instruction."
                )
            )
        } else if (instructions.size < 1) {
            return Result.failure(
                IllegalArgumentException("Initial instruction cannot be an empty String")
            )
        }

        return Result.success(
            InternalSettings(
                coreSize,
                instructionLimit,
                instructions.get(0),
                maximumTicks,
                maximumProcessesPerPlayer,
                readDistance,
                writeDistance,
                minimumSeparation,
                separation,
                randomSeparation,
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Settings) return false

        return other.coreSize == this.coreSize &&
            other.instructionLimit == this.instructionLimit &&
            other.initialInstruction == this.initialInstruction &&
            other.maximumTicks == this.maximumTicks &&
            other.maximumProcessesPerPlayer == this.maximumProcessesPerPlayer &&
            other.readDistance == this.readDistance &&
            other.writeDistance == this.writeDistance &&
            other.minimumSeparation == this.minimumSeparation &&
            other.separation == this.separation &&
            other.randomSeparation == this.randomSeparation
    }
}
