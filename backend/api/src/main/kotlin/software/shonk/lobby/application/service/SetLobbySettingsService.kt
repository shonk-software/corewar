package software.shonk.lobby.application.service

import software.shonk.lobby.adapters.incoming.setLobbySettings.SetLobbySettingsCommand
import software.shonk.lobby.application.port.incoming.SetLobbySettingsUseCase
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort

class SetLobbySettingsService(
    private val loadLobbyPort: LoadLobbyPort,
    private val saveLobbyPort: SaveLobbyPort,
) : SetLobbySettingsUseCase {

    override fun setLobbySettings(setLobbySettingsCommand: SetLobbySettingsCommand): Result<Unit> {
        val lobby =
            loadLobbyPort.getLobby(setLobbySettingsCommand.lobbyId.id).getOrElse {
                return Result.failure(it)
            }

        lobby.setSettings(setLobbySettingsCommand.settings)
        return saveLobbyPort.saveLobby(lobby)
    }
}
