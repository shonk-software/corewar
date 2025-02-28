package software.shonk.lobby.domain.exceptions

import software.shonk.lobby.domain.PlayerNameString

class PlayerAlreadyJoinedLobbyException(playerNameString: PlayerNameString, val lobbyId: Long) :
    Exception("Player ${playerNameString.getName()} already joined lobby $lobbyId")
