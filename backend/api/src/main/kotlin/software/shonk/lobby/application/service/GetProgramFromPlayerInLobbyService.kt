package software.shonk.lobby.application.service

import kotlin.collections.get
import software.shonk.lobby.adapters.incoming.GetProgramFromPlayerInLobbyCommand
import software.shonk.lobby.application.port.incoming.GetProgramFromPlayerInLobbyQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort

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
