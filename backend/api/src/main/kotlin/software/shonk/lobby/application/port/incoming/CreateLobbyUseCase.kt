package software.shonk.lobby.application.port.incoming

import software.shonk.lobby.adapters.incoming.createLobby.CreateLobbyCommand

interface CreateLobbyUseCase {
    fun createLobby(createLobbyCommand: CreateLobbyCommand): Result<Long>
}
