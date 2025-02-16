package software.shonk.lobby.domain

import kotlinx.serialization.Serializable

@Serializable data class GameResult(val winner: Winner)
