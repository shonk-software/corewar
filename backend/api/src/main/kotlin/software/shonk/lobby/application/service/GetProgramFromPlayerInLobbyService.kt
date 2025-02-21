package software.shonk.lobby.application.service

import kotlin.collections.get
import software.shonk.lobby.adapters.incoming.getProgramFromPlayerInLobby.GetProgramFromPlayerInLobbyCommand
import software.shonk.lobby.application.port.incoming.GetProgramFromPlayerInLobbyQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.domain.exceptions.NoCodeForPlayerException
import software.shonk.lobby.domain.exceptions.PlayerNotInLobbyException

class GetProgramFromPlayerInLobbyService(private val loadLobbyPort: LoadLobbyPort) :
    GetProgramFromPlayerInLobbyQuery {
    override fun getProgramFromPlayerInLobby(
        getProgramFromPlayerInLobbyCommand: GetProgramFromPlayerInLobbyCommand
    ): Result<String> {
        val lobby =
            loadLobbyPort.getLobby(getProgramFromPlayerInLobbyCommand.lobbyId).getOrElse {
                return Result.failure(it)
            }

        if (
            !lobby.joinedPlayers.contains(getProgramFromPlayerInLobbyCommand.playerNameString.name)
        ) {
            return Result.failure(
                PlayerNotInLobbyException(
                    getProgramFromPlayerInLobbyCommand.playerNameString,
                    getProgramFromPlayerInLobbyCommand.lobbyId,
                )
            )
        }
        val result = lobby.programs[getProgramFromPlayerInLobbyCommand.playerNameString.name]
        return if (result == null) {
            Result.failure(
                NoCodeForPlayerException(
                    getProgramFromPlayerInLobbyCommand.playerNameString,
                    getProgramFromPlayerInLobbyCommand.lobbyId,
                )
            )
        } else {
            Result.success(result)
        }
    }
}
