package software.shonk.lobby.domain

import kotlinx.serialization.Serializable
import software.shonk.interpreter.internal.statistics.RoundInformation as InterpreterRoundInformation

@Serializable
data class RoundInformation(
    val playerId: String,
    val programCounterBefore: Int,
    val programCounterAfter: Int,
    val programCountersOfOtherProcesses: List<Int>,
    val memoryReads: List<Int>,
    val memoryWrites: List<Int>,
    val processDied: Boolean,
)

fun InterpreterRoundInformation.toDomainRoundInformation(): RoundInformation {
    return RoundInformation(
        playerId = this.playerId,
        programCounterBefore = this.programCounterBefore,
        programCounterAfter = this.programCounterAfter,
        programCountersOfOtherProcesses = this.programCountersOfOtherProcesses,
        memoryReads = this.memoryReads,
        memoryWrites = this.memoryWrites,
        processDied = this.processDied,
    )
}
