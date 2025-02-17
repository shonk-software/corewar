package software.shonk.lobby.application.port.incoming

import software.shonk.lobby.adapters.incoming.setLobbySettings.SetLobbySettingsCommand

interface SetLobbySettingsUseCase {

    fun setLobbySettings(setLobbySettingsCommand: SetLobbySettingsCommand): Result<Unit>
}
