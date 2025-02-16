package software.shonk.application.port.incoming

import software.shonk.domain.Player

interface AddProgramToLobbyUseCase {
    fun addProgramToLobby(lobbyId: Long, player: Player, program: String): Result<Unit>
}
