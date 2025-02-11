package software.shonk.application.port.incoming

import software.shonk.domain.LobbyStatus
import software.shonk.domain.Status

interface ShorkUseCase {
    fun addProgramToLobby(lobbyId: Long, name: String?, program: String): Result<Unit>

    fun joinLobby(lobbyId: Long, playerName: String): Result<Unit>

    fun deleteLobby(lobbyId: Long): Result<Unit>

    fun getLobbyStatus(lobbyId: Long, includeRoundInformation: Boolean = true): Result<Status>

    fun getAllLobbies(): Result<List<LobbyStatus>>
}
