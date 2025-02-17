package software.shonk.lobby.adapters.incoming.getLobbyStatus

data class GetLobbyStatusCommand(val lobbyId: Long, val showVisualization: Boolean = true) {
    init {
        require(lobbyId >= 0) { "The Lobby id must be non-negative." }
    }
}