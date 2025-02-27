package software.shonk.lobby.adapters.incoming.setLobbySettings

import software.shonk.lobby.domain.InterpreterSettings

data class SetLobbySettingsCommand(val lobbyId: Long, val settings: InterpreterSettings) {
    init {
        require(lobbyId >= 0) { "The Lobby id must be non-negative." }
    }

    constructor(
        lobbyId: String?,
        settings: InterpreterSettings,
    ) : this(parseLobbyId(lobbyId), settings)

    companion object {
        // todo move this and all similar occurences to some kind of helper
        fun parseLobbyId(lobbyIdString: String?): Long {
            return lobbyIdString?.toLongOrNull()?.takeIf { it >= 0 }
                ?: throw IllegalArgumentException("Failed to parse Lobby id: $lobbyIdString")
        }
    }
}
