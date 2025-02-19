package software.shonk.lobby.adapters.incoming.joinLobby

import software.shonk.lobby.domain.PlayerNameString

data class JoinLobbyCommand(val lobbyId: Long, val playerName: PlayerNameString) {
    init {
        require(lobbyId >= 0) { "The Lobby id must be non-negative." }
    }
}
