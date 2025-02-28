package software.shonk.lobby.adapters.incoming.setLobbySettings

import software.shonk.lobby.domain.InterpreterSettings
import software.shonk.lobby.domain.LobbyId

data class SetLobbySettingsCommand(val lobbyId: LobbyId, val settings: InterpreterSettings) {

    constructor(lobbyId: String?, settings: InterpreterSettings) : this(LobbyId(lobbyId), settings)

    constructor(lobbyId: Long, settings: InterpreterSettings) : this(LobbyId(lobbyId), settings)
}
