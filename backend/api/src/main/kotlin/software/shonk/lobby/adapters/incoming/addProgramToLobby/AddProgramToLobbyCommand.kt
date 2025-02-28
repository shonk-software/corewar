package software.shonk.lobby.adapters.incoming.addProgramToLobby

import software.shonk.lobby.domain.LobbyId
import software.shonk.lobby.domain.PlayerNameString

class AddProgramToLobbyCommand(
    val lobbyId: LobbyId,
    val playerNameString: PlayerNameString,
    val program: String,
) {

    constructor(
        lobbyIdString: String?,
        playerNameString: String?,
        program: String,
    ) : this(LobbyId(lobbyIdString), PlayerNameString.from(playerNameString), program)

    constructor(
        lobbyId: Long,
        playerNameString: String?,
        program: String,
    ) : this(LobbyId(lobbyId), PlayerNameString.from(playerNameString), program)
}
