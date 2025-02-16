package software.shonk.lobby.application.service

import software.shonk.lobby.adapters.incoming.JoinLobbyCommand
import software.shonk.lobby.application.port.incoming.JoinLobbyUseCase
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort

class JoinLobbyService(
    private val loadLobbyPort: LoadLobbyPort,
    private val saveLobbyPort: SaveLobbyPort,
) : JoinLobbyUseCase {
    override fun joinLobby(joinLobbyCommand: JoinLobbyCommand): Result<Unit> {
        val lobby =
            loadLobbyPort.getLobby(joinLobbyCommand.lobbyId).getOrElse {
                return Result.failure(it)
            }

        if (!lobby.containsPlayer(joinLobbyCommand.playerName)) {
            lobby.joinedPlayers.add(joinLobbyCommand.playerName)
            return saveLobbyPort.saveLobby(lobby)
        }
        return Result.failure(
            IllegalArgumentException("Your player name is invalid OR Lobby doesn't exist")
        )
    }
}
