package software.shonk.lobby.application.port.incoming

import software.shonk.lobby.domain.Status

interface GetLobbyStatusQuery {

    fun getLobbyStatus(lobbyId: Long, includeRoundInformation: Boolean = true): Result<Status>
}
