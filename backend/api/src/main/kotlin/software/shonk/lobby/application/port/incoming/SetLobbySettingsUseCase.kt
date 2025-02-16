package software.shonk.lobby.application.port.incoming

import software.shonk.lobby.adapters.incoming.SetLobbySettingsCommand

interface SetLobbySettingsUseCase {

    fun setLobbySettings(setLobbySettingsCommand: SetLobbySettingsCommand): Result<Unit>
}
