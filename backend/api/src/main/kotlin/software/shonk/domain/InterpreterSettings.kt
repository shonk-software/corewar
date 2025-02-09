package software.shonk.domain

import kotlinx.serialization.Serializable
import software.shonk.interpreter.Settings

@Serializable
data class InterpreterSettings(
    val coreSize: Int,
    val instructionLimit: Int,
    val initialInstruction: String,
    val maximumTicks: Int,
    val maximumProcessesPerPlayer: Int,
    val readDistance: Int,
    val writeDistance: Int,
    val minimumSeparation: Int,
    val separation: Int,
    val randomSeparation: Boolean,
)

// This should not exist if InterpreterSettings is the DO
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
