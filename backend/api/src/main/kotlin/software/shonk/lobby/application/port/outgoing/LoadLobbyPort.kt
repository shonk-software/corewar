package software.shonk.lobby.application.port.outgoing

import software.shonk.lobby.domain.Lobby
import software.shonk.lobby.domain.LobbyStatus

interface LoadLobbyPort {
    fun getLobby(lobbyId: Long): Result<Lobby>

    fun getAllLobbies(): Result<List<LobbyStatus>>
}
