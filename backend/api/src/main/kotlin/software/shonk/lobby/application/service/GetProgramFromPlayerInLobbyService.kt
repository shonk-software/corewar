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
            loadLobbyPort.getLobby(getProgramFromPlayerInLobbyCommand.lobbyId.id).getOrElse {
                return Result.failure(it)
            }

        if (
            !lobby.joinedPlayers.contains(
                getProgramFromPlayerInLobbyCommand.playerNameString.getName()
            )
        ) {
            return Result.failure(
                PlayerNotInLobbyException(
                    getProgramFromPlayerInLobbyCommand.playerNameString,
                    getProgramFromPlayerInLobbyCommand.lobbyId.id,
                )
            )
        }
        val result = lobby.programs[getProgramFromPlayerInLobbyCommand.playerNameString.getName()]
        return if (result == null) {
            Result.failure(
                NoCodeForPlayerException(
                    getProgramFromPlayerInLobbyCommand.playerNameString,
                    getProgramFromPlayerInLobbyCommand.lobbyId.id,
                )
            )
        } else {
            Result.success(result)
        }
    }
}
