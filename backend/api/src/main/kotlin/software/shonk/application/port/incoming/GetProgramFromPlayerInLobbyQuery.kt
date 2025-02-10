package software.shonk.application.port.incoming

import software.shonk.adapters.incoming.GetProgramFromPlayerInLobbyCommand

interface GetProgramFromPlayerInLobbyQuery {
    fun getProgramFromPlayerInLobby(
        getProgramFromPlayerInLobbyCommand: GetProgramFromPlayerInLobbyCommand
    ): Result<String>
}
