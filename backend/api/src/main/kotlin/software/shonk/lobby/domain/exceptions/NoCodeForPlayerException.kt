package software.shonk.lobby.domain.exceptions

import software.shonk.lobby.domain.PlayerNameString

class NoCodeForPlayerException(playerNameString: PlayerNameString, val lobbyId: Long) :
    Exception(
        "Player ${playerNameString.getName()} has not submitted any code in lobby $lobbyId yet"
    )
