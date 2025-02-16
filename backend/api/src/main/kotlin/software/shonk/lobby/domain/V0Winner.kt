package software.shonk.lobby.domain

import kotlinx.serialization.Serializable

@Serializable
enum class V0Winner {
    A,
    B,
    UNDECIDED,
}
