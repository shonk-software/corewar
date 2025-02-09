package software.shonk.application.port.outgoing

interface DeleteLobbyPort {
    fun deleteLobby(lobbyId: Long): Result<Unit>
}
