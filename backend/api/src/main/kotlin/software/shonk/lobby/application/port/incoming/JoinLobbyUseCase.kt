package software.shonk.lobby.application.port.incoming

import software.shonk.lobby.adapters.incoming.JoinLobbyCommand

interface JoinLobbyUseCase {
    fun joinLobby(joinLobbyCommand: JoinLobbyCommand): Result<Unit>
}
