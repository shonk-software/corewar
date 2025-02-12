package software.shonk.application.port.incoming

interface JoinLobbyUseCase {
    fun joinLobby(lobbyId: Long, playerName: String): Result<Unit>
}
