package software.shonk.lobby.domain

import kotlinx.serialization.Serializable

@Serializable
data class Status(
    val playerASubmitted: Boolean,
    val playerBSubmitted: Boolean,
    val gameState: GameState,
    val result: GameResult,
    var visualizationData: List<RoundInformation>,
)
