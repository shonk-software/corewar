package software.shonk.lobby.adapters.incoming.getLobbySettings

data class GetLobbySettingsCommand(val lobbyId: Long) {
    init {
        require(lobbyId >= 0) { "The Lobby id must be non-negative." }
    }

    constructor(lobbyId: String?) : this(parseLobbyId(lobbyId))

    companion object {
        fun parseLobbyId(lobbyIdString: String?): Long {
            return lobbyIdString?.toLongOrNull()?.takeIf { it >= 0 }
                ?: throw IllegalArgumentException("Failed to parse Lobby id: $lobbyIdString")
        }
    }
}
