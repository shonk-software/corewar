package software.shonk.lobby.application.service

import software.shonk.lobby.adapters.incoming.getLobbyStatus.GetLobbyStatusCommand
import software.shonk.lobby.application.port.incoming.GetLobbyStatusQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.domain.Status

class GetLobbyStatusService(private val loadLobbyPort: LoadLobbyPort) : GetLobbyStatusQuery {
    override fun getLobbyStatus(getLobbyStatusCommand: GetLobbyStatusCommand): Result<Status> {
        val lobby =
            loadLobbyPort.getLobby(getLobbyStatusCommand.lobbyId.id).getOrElse {
                return Result.failure(it)
            }

        val status = lobby.getStatus()
        if (!getLobbyStatusCommand.showVisualization) {
            status.visualizationData = emptyList()
        }

        return Result.success(status)
    }
}
