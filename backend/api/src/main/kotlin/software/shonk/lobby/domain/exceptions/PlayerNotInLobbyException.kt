package software.shonk.lobby.domain.exceptions

import software.shonk.lobby.domain.PlayerNameString

class PlayerNotInLobbyException(playerNameString: PlayerNameString, val lobbyId: Long) :
    Exception("Player ${playerNameString.name} has not joined lobby $lobbyId yet")
