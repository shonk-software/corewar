package software.shonk.lobby.application.service

import software.shonk.lobby.adapters.incoming.addProgramToLobby.AddProgramToLobbyCommand
import software.shonk.lobby.application.port.incoming.AddProgramToLobbyUseCase
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.GameState

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
            return Result.failure(
                IllegalStateException("You can't submit code to a lobby that is finished!")
            )
        }

        if (!lobby.containsPlayer(addProgramToLobbyCommand.player.name)) {
            return Result.failure(
                IllegalStateException("You can't submit code to a lobby you have not joined!")
            )
        }

        lobby.addProgram(addProgramToLobbyCommand.player.name, addProgramToLobbyCommand.program)
        return saveLobbyPort.saveLobby(lobby)
    }
}
