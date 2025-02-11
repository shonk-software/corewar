package software.shonk.application.port.incoming

import software.shonk.domain.InterpreterSettings

interface GetLobbySettingsQuery {

    fun getLobbySettings(lobbyId: Long): Result<InterpreterSettings>
}
