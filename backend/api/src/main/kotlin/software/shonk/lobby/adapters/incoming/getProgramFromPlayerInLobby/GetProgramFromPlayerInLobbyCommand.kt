package software.shonk.lobby.adapters.incoming.getProgramFromPlayerInLobby

import software.shonk.lobby.domain.LobbyId
import software.shonk.lobby.domain.PlayerNameString

// todo move all these error messages into cool objects and stuff and organize them
data class GetProgramFromPlayerInLobbyCommand(
    val lobbyId: LobbyId,
    val playerNameString: PlayerNameString,
) {
    constructor(
        lobbyIdString: String?,
        playerName: String?,
    ) : this(LobbyId(lobbyIdString), PlayerNameString.from(playerName))

    constructor(
        lobbyIdString: Long,
        playerName: String?,
    ) : this(LobbyId(lobbyIdString), PlayerNameString.from(playerName))
}
