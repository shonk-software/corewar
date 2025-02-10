package software.shonk.application.service

import software.shonk.adapters.incoming.CreateLobbyCommand
import software.shonk.application.port.incoming.CreateLobbyUseCase
import software.shonk.application.port.outgoing.SaveLobbyPort
import software.shonk.domain.Lobby
import software.shonk.interpreter.IShork

class CreateLobbyService(private val shork: IShork, private var saveLobbyPort: SaveLobbyPort) :
    CreateLobbyUseCase {

    private var lobbyCounter = 0L

    override fun createLobby(createLobbyCommand: CreateLobbyCommand): Result<Long> {
        val newLobby = Lobby(lobbyCounter, HashMap(), shork)
        newLobby.joinedPlayers.add(createLobbyCommand.playerName)
        val saveResult = saveLobbyPort.saveLobby(newLobby)
        saveResult.onSuccess {
            return Result.success(lobbyCounter++)
        }
        return Result.failure(IllegalStateException("Error saving lobby"))
    }
}
