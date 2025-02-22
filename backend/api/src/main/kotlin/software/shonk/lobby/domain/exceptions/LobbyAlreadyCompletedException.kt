package software.shonk.lobby.domain.exceptions

class LobbyAlreadyCompletedException(val lobbyId: Long) :
    Exception("Lobby with id $lobbyId has already completed!")
