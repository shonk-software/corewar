package software.shonk.lobby.adapters.outgoing

import software.shonk.lobby.application.port.outgoing.DeleteLobbyPort
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.Lobby
import software.shonk.lobby.domain.LobbyStatus
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException

class MemoryLobbyManager : LoadLobbyPort, SaveLobbyPort, DeleteLobbyPort {

    val lobbies: HashMap<Long, Lobby> = HashMap()

    override fun getLobby(lobbyId: Long): Result<Lobby> {
        return lobbies[lobbyId]?.let { Result.success(it) }
            ?: Result.failure(LobbyNotFoundException(lobbyId))
    }

    override fun getAllLobbies(): Result<List<LobbyStatus>> {
        return Result.success(
            lobbies.values.toList().map { lobby ->
                LobbyStatus(
                    id = lobby.id,
                    playersJoined = lobby.joinedPlayers,
                    gameState = lobby.gameState.toString(),
                )
            }
        )
    }

    override fun saveLobby(lobby: Lobby): Result<Unit> {
        lobbies[lobby.id] = lobby
        return Result.success(Unit)
    }

    override fun deleteLobby(lobbyId: Long): Result<Unit> {
        lobbies.remove(lobbyId)
        return Result.success(Unit)
    }
}
