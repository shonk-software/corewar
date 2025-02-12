package software.shonk.application.port.incoming

import software.shonk.adapters.incoming.JoinLobbyCommand

interface JoinLobbyUseCase {
    fun joinLobby(joinLobbyCommand: JoinLobbyCommand): Result<Unit>
}
