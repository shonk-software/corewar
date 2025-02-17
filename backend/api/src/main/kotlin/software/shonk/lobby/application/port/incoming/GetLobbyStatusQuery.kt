package software.shonk.lobby.application.port.incoming

import software.shonk.lobby.adapters.incoming.getLobbyStatus.GetLobbyStatusCommand
import software.shonk.lobby.domain.Status

interface GetLobbyStatusQuery {

    fun getLobbyStatus(getLobbyStatusCommand: GetLobbyStatusCommand): Result<Status>
}
