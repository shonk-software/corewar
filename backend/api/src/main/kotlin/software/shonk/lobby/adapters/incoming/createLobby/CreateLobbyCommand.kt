package software.shonk.lobby.adapters.incoming.createLobby

import software.shonk.lobby.domain.PlayerNameString

data class CreateLobbyCommand(val playerName: PlayerNameString) {

    constructor(playerName: String?) : this(PlayerNameString.from(playerName))
}
