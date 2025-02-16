package software.shonk.lobby.application.port.outgoing

interface DeleteLobbyPort {
    fun deleteLobby(lobbyId: Long): Result<Unit>
}
