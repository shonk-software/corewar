package software.shonk.lobby.adapters.incoming.joinLobby

import software.shonk.lobby.domain.PlayerNameString

data class JoinLobbyCommand(val lobbyId: Long, val playerName: PlayerNameString) {
    init {
        require(lobbyId >= 0) { "The Lobby id must be non-negative." }
    }

    constructor(
        lobbyIdString: String?,
        playerName: PlayerNameString,
    ) : this(parseLobbyId(lobbyIdString), playerName)

    companion object {
        fun parseLobbyId(lobbyIdString: String?): Long {
            return lobbyIdString?.toLongOrNull()?.takeIf { it >= 0 }
                ?: throw IllegalArgumentException("Failed to parse Lobby id: $lobbyIdString")
        }
    }
}
