package software.shonk.application.service

import software.shonk.application.port.incoming.SetLobbySettingsUseCase
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort
import software.shonk.interpreter.Settings

class SetLobbySettingsService(
    private val loadLobbyPort: LoadLobbyPort,
    private val saveLobbyPort: SaveLobbyPort,
) : SetLobbySettingsUseCase {

    override fun setLobbySettings(lobbyId: Long, settings: Settings): Result<Unit> {
        val lobby =
            loadLobbyPort.getLobby(lobbyId).getOrElse {
                return Result.failure(it)
            }

        lobby.setSettings(settings)
        return saveLobbyPort.saveLobby(lobby)
    }
}
