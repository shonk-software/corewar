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

    constructor(
        lobbyIdString: String?,
        playerNameString: PlayerNameString,
        program: String,
    ) : this(parseLobbyId(lobbyIdString), playerNameString, program)

    companion object {
        // todo move this and all similar occurences to some kind of helper
        fun parseLobbyId(lobbyIdString: String?): Long {
            return lobbyIdString?.toLongOrNull()?.takeIf { it >= 0 }
                ?: throw IllegalArgumentException("Failed to parse Lobby id: $lobbyIdString")
        }
    }
}
