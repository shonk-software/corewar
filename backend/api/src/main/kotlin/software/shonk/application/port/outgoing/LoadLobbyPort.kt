package software.shonk.application.port.outgoing

import software.shonk.domain.Lobby
import software.shonk.domain.LobbyStatus

interface LoadLobbyPort {
    fun getLobby(lobbyId: Long): Result<Lobby>

    fun getAllLobbies(): Result<List<LobbyStatus>>
}
