package software.shonk.lobby.domain

import kotlinx.serialization.Serializable

// todo lobby itself should only contain metadata in the future and not the visu data and live game
// data and stuff. once we do that, this can be removed
@Serializable
data class LobbyStatus(val id: Long, val playersJoined: List<String>, val gameState: String)
