package software.shonk.lobby.adapters.incoming.getLobbyStatus

import software.shonk.lobby.domain.LobbyId

data class GetLobbyStatusCommand(val lobbyId: LobbyId, val showVisualization: Boolean = true) {

    constructor(
        lobbyIdString: String?,
        showVisualizationString: String?,
    ) : this(LobbyId(lobbyIdString), parseShowVisualization(showVisualizationString))

    constructor(
        lobbyId: Long,
        showVisualization: Boolean = true,
    ) : this(LobbyId(lobbyId), showVisualization)

    companion object {

        private fun parseShowVisualization(showVisualizationString: String?): Boolean {
            return when (showVisualizationString?.lowercase()) {
                "true" -> true
                "false" -> false
                else -> true
            }
        }
    }
}
