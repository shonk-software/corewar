package software.shonk.lobby.application.port.outgoing

import software.shonk.lobby.domain.Lobby

interface SaveLobbyPort {
    fun saveLobby(lobby: Lobby): Result<Unit>
}
