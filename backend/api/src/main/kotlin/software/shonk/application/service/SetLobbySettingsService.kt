package software.shonk.application.service

import software.shonk.adapters.incoming.SetLobbySettingsCommand
import software.shonk.application.port.incoming.SetLobbySettingsUseCase
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort

class SetLobbySettingsService(
    private val loadLobbyPort: LoadLobbyPort,
    private val saveLobbyPort: SaveLobbyPort,
) : SetLobbySettingsUseCase {

    override fun setLobbySettings(setLobbySettingsCommand: SetLobbySettingsCommand): Result<Unit> {
        val lobby =
            loadLobbyPort.getLobby(setLobbySettingsCommand.lobbyId).getOrElse {
                return Result.failure(it)
            }

        lobby.setSettings(setLobbySettingsCommand.settings)
        return saveLobbyPort.saveLobby(lobby)
    }
}
