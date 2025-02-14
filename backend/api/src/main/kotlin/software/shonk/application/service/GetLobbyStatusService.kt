package software.shonk.application.service

import software.shonk.application.port.incoming.GetLobbyStatusQuery
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.domain.Status

class GetLobbyStatusService(private val loadLobbyPort: LoadLobbyPort) : GetLobbyStatusQuery {
    override fun getLobbyStatus(lobbyId: Long, includeRoundInformation: Boolean): Result<Status> {
        val lobby =
            loadLobbyPort.getLobby(lobbyId).getOrElse {
                return Result.failure(it)
            }

        val status = lobby.getStatus()
        if (!includeRoundInformation) {
            status.visualizationData = emptyList()
        }

        return Result.success(status)
    }
}
