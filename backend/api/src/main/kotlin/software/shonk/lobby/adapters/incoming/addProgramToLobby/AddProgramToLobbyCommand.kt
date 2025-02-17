package software.shonk.lobby.adapters.incoming.addProgramToLobby

import software.shonk.lobby.domain.Player

// todo introduce Lobby class instead of primitive Long, same for program
class AddProgramToLobbyCommand(val lobbyId: Long, val player: Player, val program: String) {
    init {
        require(lobbyId >= 0) { "The Lobby id must be non-negative." }
    }
}
