package software.shonk.lobby.domain

import kotlinx.serialization.Serializable
import software.shonk.interpreter.Settings

@Serializable
data class InterpreterSettings(
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
)

fun InterpreterSettings.toSettings(): Settings {
    return Settings(
        coreSize = this.coreSize,
        instructionLimit = this.instructionLimit,
        initialInstruction = this.initialInstruction,
        maximumTicks = this.maximumTicks,
        maximumProcessesPerPlayer = this.maximumProcessesPerPlayer,
        readDistance = this.readDistance,
        writeDistance = this.writeDistance,
        minimumSeparation = this.minimumSeparation,
        separation = this.separation,
        randomSeparation = this.randomSeparation,
    )
}
