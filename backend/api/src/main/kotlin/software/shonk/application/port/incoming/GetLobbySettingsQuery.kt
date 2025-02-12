package software.shonk.application.port.incoming

import software.shonk.adapters.incoming.GetLobbySettingsCommand
import software.shonk.domain.InterpreterSettings

interface GetLobbySettingsQuery {

    fun getLobbySettings(
        getLobbySettingsCommand: GetLobbySettingsCommand
    ): Result<InterpreterSettings>
}
