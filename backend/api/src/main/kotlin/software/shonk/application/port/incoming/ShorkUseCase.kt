package software.shonk.application.port.incoming

import software.shonk.domain.Player
import software.shonk.domain.Status

interface ShorkUseCase {
    fun addProgramToLobby(lobbyId: Long, player: Player, program: String): Result<Unit>

    fun getLobbyStatus(lobbyId: Long, includeRoundInformation: Boolean = true): Result<Status>
}
