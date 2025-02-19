package software.shonk.lobby.domain.exceptions

import software.shonk.lobby.domain.PlayerNameString

class PlayerAlreadyJoinedLobbyException(val playerNameString: PlayerNameString, val lobbyId: Long) :
    Exception("Player ${playerNameString.name} already joined lobby $lobbyId")
