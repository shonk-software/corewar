package software.shonk.lobby.domain

data class PlayerNameString(private val name: String) {
    init {
        require(isAlphaNumerical(name)) { "Name must only consist of valid characters" }
        require(name.isNotBlank()) { "Name must not be blank" }
        require(name == "playerA" || name == "playerB")
    }

    fun getName() = name

    private fun isAlphaNumerical(playerName: String): Boolean {
        return playerName.matches("^[a-zA-Z0-9]+$".toRegex()) && playerName.isNotBlank()
    }

    companion object {
        fun from(name: String?): PlayerNameString {
            require(name != null) { "Name must not be null" }
            return PlayerNameString(name)
        }
    }
}
