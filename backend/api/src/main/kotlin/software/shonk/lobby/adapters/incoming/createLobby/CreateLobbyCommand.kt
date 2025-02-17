package software.shonk.lobby.adapters.incoming.createLobby

data class CreateLobbyCommand(val playerName: String) {
    init {
        require(playerName.isNotBlank() && isAlphaNumerical(playerName)) {
            "Your player name is invalid"
        }
    }

    // todo move to PlayerName class once it exists
    private fun isAlphaNumerical(playerName: String): Boolean {
        return playerName.matches("^[a-zA-Z0-9]+$".toRegex()) && playerName.isNotBlank()
    }
}
