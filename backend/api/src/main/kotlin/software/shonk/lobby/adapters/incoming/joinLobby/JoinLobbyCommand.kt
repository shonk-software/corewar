package software.shonk.lobby.adapters.incoming.joinLobby

import software.shonk.lobby.domain.LobbyId
import software.shonk.lobby.domain.PlayerNameString

data class JoinLobbyCommand(val lobbyId: LobbyId, val playerName: PlayerNameString) {
    constructor(
        lobbyIdString: String?,
        playerName: String?,
    ) : this(LobbyId(lobbyIdString), PlayerNameString.from(playerName))

    constructor(
        lobbyIdString: Long,
        playerName: String?,
    ) : this(LobbyId(lobbyIdString), PlayerNameString.from(playerName))
}
