package software.shonk.lobby.application.service

import software.shonk.lobby.adapters.incoming.joinLobby.JoinLobbyCommand
import software.shonk.lobby.application.port.incoming.JoinLobbyUseCase
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.exceptions.PlayerAlreadyJoinedLobbyException

class JoinLobbyService(
    private val loadLobbyPort: LoadLobbyPort,
    private val saveLobbyPort: SaveLobbyPort,
) : JoinLobbyUseCase {
    override fun joinLobby(joinLobbyCommand: JoinLobbyCommand): Result<Unit> {
        val lobby =
            loadLobbyPort.getLobby(joinLobbyCommand.lobbyId.id).getOrElse {
                return Result.failure(it)
            }

        if (lobby.containsPlayer(joinLobbyCommand.playerName.getName())) {
            return Result.failure(
                PlayerAlreadyJoinedLobbyException(
                    joinLobbyCommand.playerName,
                    joinLobbyCommand.lobbyId.id,
                )
            )
        }
        lobby.joinedPlayers.add(joinLobbyCommand.playerName.getName())
        return saveLobbyPort.saveLobby(lobby)
    }
}
