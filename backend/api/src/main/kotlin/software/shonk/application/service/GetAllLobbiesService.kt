package software.shonk.application.service

import software.shonk.application.port.incoming.GetAllLobbiesQuery
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.domain.LobbyStatus

class GetAllLobbiesService(val loadLobbyPort: LoadLobbyPort) : GetAllLobbiesQuery {
    override fun getAllLobbies(): Result<List<LobbyStatus>> {
        return loadLobbyPort.getAllLobbies()
    }
}
