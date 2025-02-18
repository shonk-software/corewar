package software.shonk.lobby.domain

class LobbyNotFoundException(val lobbyId: Long) :
    NoSuchElementException("Lobby with id $lobbyId not found!")
