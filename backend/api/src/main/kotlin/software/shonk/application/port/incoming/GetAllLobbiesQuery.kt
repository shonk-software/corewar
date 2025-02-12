package software.shonk.application.port.incoming

import software.shonk.domain.LobbyStatus

interface GetAllLobbiesQuery {

    fun getAllLobbies(): Result<List<LobbyStatus>>
}
