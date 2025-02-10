package software.shonk.application.service

import software.shonk.application.port.incoming.GetProgramFromPlayerInLobbyQuery
import software.shonk.application.port.outgoing.LoadLobbyPort

class GetProgramFromPlayerInLobbyService(private val loadLobbyPort: LoadLobbyPort) :
    GetProgramFromPlayerInLobbyQuery {
    override fun getProgramFromPlayerInLobby(lobbyId: Long, name: String?): Result<String> {
        val lobby =
            loadLobbyPort.getLobby(lobbyId).getOrElse {
                return Result.failure(it)
            }

        val result = lobby.programs[name]
        return if (result == null) {
            Result.failure(IllegalArgumentException("No player with that name in the lobby"))
        } else {
            Result.success(result)
        }
    }
}
