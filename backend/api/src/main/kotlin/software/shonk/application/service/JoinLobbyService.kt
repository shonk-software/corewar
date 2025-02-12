package software.shonk.application.service

import software.shonk.application.port.incoming.JoinLobbyUseCase
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort

class JoinLobbyService(
    private val loadLobbyPort: LoadLobbyPort,
    private val saveLobbyPort: SaveLobbyPort,
) : JoinLobbyUseCase {
    override fun joinLobby(lobbyId: Long, playerName: String): Result<Unit> {
        val lobby =
            loadLobbyPort.getLobby(lobbyId).getOrElse {
                return Result.failure(it)
            }

        // todo restore this
        if (/*isAlphaNumerical(playerName)*/ true && !lobby.containsPlayer(playerName)) {
            lobby.joinedPlayers.add(playerName)
            return saveLobbyPort.saveLobby(lobby)
        }
        return Result.failure(
            IllegalArgumentException("Your player name is invalid OR Lobby doesn't exist")
        )
    }
}
