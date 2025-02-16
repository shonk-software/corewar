package software.shonk.lobby.application.port.incoming

import software.shonk.lobby.adapters.incoming.GetLobbySettingsCommand
import software.shonk.lobby.domain.InterpreterSettings

interface GetLobbySettingsQuery {

    fun getLobbySettings(
        getLobbySettingsCommand: GetLobbySettingsCommand
    ): Result<InterpreterSettings>
}
