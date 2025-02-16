package software.shonk.lobby.application.port.incoming

import software.shonk.lobby.domain.LobbyStatus

interface GetAllLobbiesQuery {

    fun getAllLobbies(): Result<List<LobbyStatus>>
}
