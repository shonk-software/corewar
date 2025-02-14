package software.shonk.application.port.incoming

import software.shonk.domain.Status

interface GetLobbyStatusQuery {

    fun getLobbyStatus(lobbyId: Long, includeRoundInformation: Boolean = true): Result<Status>
}
