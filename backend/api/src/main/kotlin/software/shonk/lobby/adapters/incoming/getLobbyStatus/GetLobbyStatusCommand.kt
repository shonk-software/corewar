package software.shonk.lobby.adapters.incoming.getLobbyStatus

data class GetLobbyStatusCommand(val lobbyId: Long, val showVisualization: Boolean = true) {
    init {
        require(lobbyId >= 0) { "The Lobby id must be non-negative." }
    }

    constructor(
        lobbyIdString: String?,
        showVisualizationString: String?,
    ) : this(parseLobbyId(lobbyIdString), parseShowVisualization(showVisualizationString))

    companion object {
        private fun parseLobbyId(lobbyIdString: String?): Long {
            return lobbyIdString?.toLongOrNull()?.takeIf { it >= 0 }
                ?: throw IllegalArgumentException("Failed to parse Lobby id: $lobbyIdString")
        }

        private fun parseShowVisualization(showVisualizationString: String?): Boolean {
            return when (showVisualizationString?.lowercase()) {
                "true" -> true
                "false" -> false
                else -> true
            }
        }
    }
}
