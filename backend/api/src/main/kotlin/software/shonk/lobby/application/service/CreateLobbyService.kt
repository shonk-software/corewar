package software.shonk.lobby.application.service

import software.shonk.interpreter.IShork
import software.shonk.lobby.adapters.incoming.createLobby.CreateLobbyCommand
import software.shonk.lobby.application.port.incoming.CreateLobbyUseCase
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.Lobby

class CreateLobbyService(private val shork: IShork, private var saveLobbyPort: SaveLobbyPort) :
    CreateLobbyUseCase {

    private var lobbyCounter = 0L

    override fun createLobby(createLobbyCommand: CreateLobbyCommand): Result<Long> {
        val newLobby = Lobby(lobbyCounter, HashMap(), shork)
        newLobby.joinedPlayers.add(createLobbyCommand.playerName.getName())
        val saveResult = saveLobbyPort.saveLobby(newLobby)
        saveResult.onSuccess {
            return Result.success(lobbyCounter++)
        }
        return Result.failure(IllegalStateException("Error saving lobby"))
    }
}
