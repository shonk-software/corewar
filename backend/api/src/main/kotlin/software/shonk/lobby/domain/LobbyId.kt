package software.shonk.lobby.domain

data class LobbyId(val id: Long) {
    init {
        require(id >= 0) { "The Lobby id must be non-negative." }
    }

    constructor(id: String?) : this(from(id))

    companion object {
        fun from(lobbyIdString: String?): Long {
            return lobbyIdString?.toLongOrNull()?.takeIf { it >= 0 }
                ?: throw IllegalArgumentException("Failed to parse Lobby id: $lobbyIdString")
        }
    }
}
