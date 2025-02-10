package software.shonk.adapters.incoming

// todo move all these error messages into cool objects and stuff and organize them
data class GetProgramFromPlayerInLobbyCommand(val lobbyId: Long, val playerName: String?) {
    init {
        require(playerName != null) { "Player must not be null" }
        require(lobbyId >= 0) { "The Lobby id must be non-negative." }
        require(playerName.isNotBlank() && isAlphaNumerical(playerName)) {
            "Your player name is invalid"
        }
    }

    // todo move to PlayerName class once it exists
    private fun isAlphaNumerical(playerName: String): Boolean {
        return playerName.matches("^[a-zA-Z0-9]+$".toRegex()) && playerName.isNotBlank()
    }
}
