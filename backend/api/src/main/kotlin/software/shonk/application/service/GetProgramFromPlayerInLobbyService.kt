package software.shonk.application.service

import software.shonk.adapters.incoming.GetProgramFromPlayerInLobbyCommand
import software.shonk.application.port.incoming.GetProgramFromPlayerInLobbyQuery
import software.shonk.application.port.outgoing.LoadLobbyPort

class GetProgramFromPlayerInLobbyService(private val loadLobbyPort: LoadLobbyPort) :
    GetProgramFromPlayerInLobbyQuery {
    override fun getProgramFromPlayerInLobby(
        getProgramFromPlayerInLobbyCommand: GetProgramFromPlayerInLobbyCommand
    ): Result<String> {
        val lobby =
            loadLobbyPort.getLobby(getProgramFromPlayerInLobbyCommand.lobbyId).getOrElse {
                return Result.failure(it)
            }

        val result = lobby.programs[getProgramFromPlayerInLobbyCommand.playerName]
        return if (result == null) {
            Result.failure(IllegalArgumentException("No player with that name in the lobby"))
        } else {
            Result.success(result)
        }
    }
}
