package software.shonk.lobby.adapters.incoming

import software.shonk.lobby.domain.InterpreterSettings

// todo move the call of the InterpreterSettings constructor to the service layer (it it even needs
// to be called)
// and instead just pass the changed concrete parameters into here and validate them here
data class SetLobbySettingsCommand(val lobbyId: Long, val settings: InterpreterSettings) {
    init {
        require(lobbyId >= 0) { "The Lobby id must be non-negative." }
    }
}
