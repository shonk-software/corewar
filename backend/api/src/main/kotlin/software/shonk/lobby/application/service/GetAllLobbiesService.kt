package software.shonk.lobby.application.service

import software.shonk.lobby.application.port.incoming.GetAllLobbiesQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.domain.LobbyStatus

class GetAllLobbiesService(val loadLobbyPort: LoadLobbyPort) : GetAllLobbiesQuery {
    override fun getAllLobbies(): Result<List<LobbyStatus>> {
        return loadLobbyPort.getAllLobbies()
    }
}
