package software.shonk.lobby.application.port.incoming

import software.shonk.lobby.adapters.incoming.addProgramToLobby.AddProgramToLobbyCommand

interface AddProgramToLobbyUseCase {
    fun addProgramToLobby(addProgramToLobbyCommand: AddProgramToLobbyCommand): Result<Unit>
}
