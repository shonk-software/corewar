package software.shonk.lobby.domain

import kotlinx.serialization.Serializable

@Serializable
enum class Winner {
    A,
    B,
    DRAW,
}
