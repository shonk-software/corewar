package software.shonk.application.port.incoming

import software.shonk.adapters.incoming.CreateLobbyCommand

interface CreateLobbyUseCase {
    fun createLobby(createLobbyCommand: CreateLobbyCommand): Result<Long>
}
