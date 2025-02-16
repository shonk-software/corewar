package software.shonk.lobby.application.port.incoming

import software.shonk.lobby.domain.Player

interface AddProgramToLobbyUseCase {
    fun addProgramToLobby(lobbyId: Long, player: Player, program: String): Result<Unit>
}
