package software.shonk.application.port.incoming

import software.shonk.domain.InterpreterSettings

interface SetLobbySettingsUseCase {

    fun setLobbySettings(lobbyId: Long, settings: InterpreterSettings): Result<Unit>
}
