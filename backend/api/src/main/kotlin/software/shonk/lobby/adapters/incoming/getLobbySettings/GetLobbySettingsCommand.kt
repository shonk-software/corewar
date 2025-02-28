package software.shonk.lobby.adapters.incoming.getLobbySettings

import software.shonk.lobby.domain.LobbyId

data class GetLobbySettingsCommand(val lobbyId: LobbyId) {

    constructor(lobbyId: String?) : this(LobbyId(lobbyId))

    constructor(lobbyId: Long) : this(LobbyId(lobbyId))
}
