package software.shonk.application.port.outgoing

import software.shonk.domain.Lobby

interface SaveLobbyPort {
    fun saveLobby(lobby: Lobby): Result<Unit>
}
