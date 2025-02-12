package software.shonk.application.service

import software.shonk.application.port.incoming.ShorkUseCase
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort
import software.shonk.domain.*

const val NO_LOBBY_MESSAGE = "No lobby with that id"

class ShorkService(
    private val loadLobbyPort: LoadLobbyPort,
    private val saveLobbyPort: SaveLobbyPort,
) : ShorkUseCase {

    override fun addProgramToLobby(lobbyId: Long, name: String?, program: String): Result<Unit> {
        val lobby =
            loadLobbyPort.getLobby(lobbyId).getOrElse {
                return Result.failure(it)
            }
        if (lobby.gameState == GameState.FINISHED) {
            return Result.failure(
                IllegalStateException("You can't submit code to a lobby that is finished!")
            )
        }

        if (name == null || !verifyPlayerName(name)) {
            return Result.failure(IllegalArgumentException("Invalid player name"))
        }

        if (!lobby.containsPlayer(name)) {
            return Result.failure(
                IllegalStateException("You can't submit code to a lobby you have not joined!")
            )
        }

        lobby.addProgram(name, program)
        return saveLobbyPort.saveLobby(lobby)
    }

    override fun getLobbyStatus(lobbyId: Long, includeRoundInformation: Boolean): Result<Status> {
        val lobby =
            loadLobbyPort.getLobby(lobbyId).getOrElse {
                return Result.failure(it)
            }

        val status = lobby.getStatus()
        if (!includeRoundInformation) {
            status.visualizationData = emptyList()
        }

        return Result.success(status)
    }

    private fun verifyPlayerName(player: String?): Boolean {
        return player == "playerA" || player == "playerB"
    }
}
