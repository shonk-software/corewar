package software.shonk.application.port.incoming

import software.shonk.adapters.incoming.SetLobbySettingsCommand

interface SetLobbySettingsUseCase {

    fun setLobbySettings(setLobbySettingsCommand: SetLobbySettingsCommand): Result<Unit>
}
