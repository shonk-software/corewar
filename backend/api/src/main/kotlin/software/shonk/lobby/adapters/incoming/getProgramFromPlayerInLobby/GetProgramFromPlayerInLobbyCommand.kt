package software.shonk.lobby.adapters.incoming.getProgramFromPlayerInLobby

import software.shonk.lobby.domain.PlayerNameString

// todo move all these error messages into cool objects and stuff and organize them
data class GetProgramFromPlayerInLobbyCommand(
    val lobbyId: Long,
    val playerNameString: PlayerNameString,
) {
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
