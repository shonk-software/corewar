package software.shonk.adapters.incoming

data class CreateLobbyCommand(val playerName: String) {
    init {
        require(playerName.isNotBlank() && isAlphaNumerical(playerName)) {
            "Your player name is invalid"
        }
    }

    private fun isAlphaNumerical(playerName: String): Boolean {
        return playerName.matches("^[a-zA-Z0-9]+$".toRegex()) && playerName.isNotBlank()
    }
}
