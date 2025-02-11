package software.shonk.application.port.incoming

import software.shonk.domain.InterpreterSettings
import software.shonk.domain.LobbyStatus
import software.shonk.domain.Status
import software.shonk.interpreter.Settings

interface ShorkUseCase {
    fun addProgramToLobby(lobbyId: Long, name: String?, program: String): Result<Unit>

    fun setLobbySettings(lobbyId: Long, settings: Settings): Result<Unit>

    fun getLobbySettings(lobbyId: Long): Result<InterpreterSettings>

    fun joinLobby(lobbyId: Long, playerName: String): Result<Unit>

    fun deleteLobby(lobbyId: Long): Result<Unit>

    fun getLobbyStatus(lobbyId: Long, includeRoundInformation: Boolean = true): Result<Status>

    fun getAllLobbies(): Result<List<LobbyStatus>>
}
