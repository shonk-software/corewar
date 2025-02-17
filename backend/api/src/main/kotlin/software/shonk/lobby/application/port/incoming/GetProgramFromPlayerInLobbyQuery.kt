package software.shonk.lobby.application.port.incoming

import software.shonk.lobby.adapters.incoming.getProgramFromPlayerInLobby.GetProgramFromPlayerInLobbyCommand

interface GetProgramFromPlayerInLobbyQuery {
    fun getProgramFromPlayerInLobby(
        getProgramFromPlayerInLobbyCommand: GetProgramFromPlayerInLobbyCommand
    ): Result<String>
}
