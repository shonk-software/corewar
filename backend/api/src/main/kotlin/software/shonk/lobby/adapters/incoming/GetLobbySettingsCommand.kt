package software.shonk.lobby.adapters.incoming

data class GetLobbySettingsCommand(val lobbyId: Long) {
    init {
        require(lobbyId >= 0) { "The Lobby id must be non-negative." }
    }
}
