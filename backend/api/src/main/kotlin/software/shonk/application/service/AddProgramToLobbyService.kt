package software.shonk.application.service

import software.shonk.application.port.incoming.AddProgramToLobbyUseCase
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort
import software.shonk.domain.GameState
import software.shonk.domain.Player

class AddProgramToLobbyService(
    private val loadLobbyPort: LoadLobbyPort,
    private val saveLobbyPort: SaveLobbyPort,
) : AddProgramToLobbyUseCase {

    override fun addProgramToLobby(lobbyId: Long, player: Player, program: String): Result<Unit> {
        val lobby =
            loadLobbyPort.getLobby(lobbyId).getOrElse {
                return Result.failure(it)
            }
        if (lobby.gameState == GameState.FINISHED) {
            return Result.failure(
                IllegalStateException("You can't submit code to a lobby that is finished!")
            )
        }

        if (!lobby.containsPlayer(player.name)) {
            return Result.failure(
                IllegalStateException("You can't submit code to a lobby you have not joined!")
            )
        }

        lobby.addProgram(player.name, program)
        return saveLobbyPort.saveLobby(lobby)
    }
}
