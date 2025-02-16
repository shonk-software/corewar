package software.shonk.lobby.domain

import kotlinx.serialization.Serializable

@Serializable
data class V0Status(
    val playerASubmitted: Boolean,
    val playerBSubmitted: Boolean,
    val gameState: GameState,
    val result: V0Result,
)
