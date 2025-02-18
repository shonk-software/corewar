package software.shonk.lobby.domain.exceptions

class LobbyNotFoundException(val lobbyId: Long) :
    NoSuchElementException("Lobby with id $lobbyId not found!")
