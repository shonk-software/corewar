package software.shonk.lobby.application.port.incoming

import software.shonk.lobby.adapters.incoming.joinLobby.JoinLobbyCommand

interface JoinLobbyUseCase {
    fun joinLobby(joinLobbyCommand: JoinLobbyCommand): Result<Unit>
}
