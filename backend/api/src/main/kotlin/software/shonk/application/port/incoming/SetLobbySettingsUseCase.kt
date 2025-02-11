package software.shonk.application.port.incoming

import software.shonk.interpreter.Settings

interface SetLobbySettingsUseCase {

    fun setLobbySettings(lobbyId: Long, settings: Settings): Result<Unit>
}
