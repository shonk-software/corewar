package software.shonk.application.port.incoming

interface GetProgramFromPlayerInLobbyQuery {
    fun getProgramFromPlayerInLobby(lobbyId: Long, name: String?): Result<String>
}
