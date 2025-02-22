package software.shonk.lobby.application.service

import software.shonk.lobby.adapters.incoming.addProgramToLobby.AddProgramToLobbyCommand
import software.shonk.lobby.application.port.incoming.AddProgramToLobbyUseCase
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.GameState
import software.shonk.lobby.domain.exceptions.LobbyAlreadyCompletedException
import software.shonk.lobby.domain.exceptions.PlayerNotInLobbyException

class AddProgramToLobbyService(
    private val loadLobbyPort: LoadLobbyPort,
    private val saveLobbyPort: SaveLobbyPort,
) : AddProgramToLobbyUseCase {

    override fun addProgramToLobby(
        addProgramToLobbyCommand: AddProgramToLobbyCommand
    ): Result<Unit> {
        val lobby =
            loadLobbyPort.getLobby(addProgramToLobbyCommand.lobbyId).getOrElse {
                return Result.failure(it)
            }
        if (lobby.gameState == GameState.FINISHED) {
            return Result.failure(LobbyAlreadyCompletedException(addProgramToLobbyCommand.lobbyId))
        }

        if (!lobby.containsPlayer(addProgramToLobbyCommand.playerNameString.name)) {
            return Result.failure(
                PlayerNotInLobbyException(addProgramToLobbyCommand.playerNameString, lobby.id)
            )
        }

        lobby.addProgram(
            addProgramToLobbyCommand.playerNameString.name,
            addProgramToLobbyCommand.program,
        )
        return saveLobbyPort.saveLobby(lobby)
    }
}
