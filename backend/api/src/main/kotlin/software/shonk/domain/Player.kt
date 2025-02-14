package software.shonk.domain

data class Player(val name: String) {
    init {
        require(name.isNotBlank()) { "Name must not be blank" }
        require(name == "playerA" || name == "playerB")
    }
}
