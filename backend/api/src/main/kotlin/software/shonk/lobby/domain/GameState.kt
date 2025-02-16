package software.shonk.lobby.domain

import kotlinx.serialization.Serializable

@Serializable
enum class GameState {
    NOT_STARTED,
    RUNNING,
    FINISHED,
}
