package software.shonk.lobby.adapters.incoming.addProgramToLobby

import software.shonk.lobby.domain.PlayerNameString

// todo introduce Lobby class instead of primitive Long, same for program
class AddProgramToLobbyCommand(
    val lobbyId: Long,
    val playerNameString: PlayerNameString,
    val program: String,
) {
    init {
        require(lobbyId >= 0) { "The Lobby id must be non-negative." }
    }
}
