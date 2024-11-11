package software.shonk.application.port.incoming

import software.shonk.domain.LobbyStatus
import software.shonk.domain.Status
import software.shonk.interpreter.Settings

interface ShorkUseCase {
    fun addProgramToLobby(lobbyId: Long, name: String?, program: String): Result<Unit>

    fun getProgramFromLobby(lobbyId: Long, name: String?): Result<String>

    fun setLobbySettings(lobbyId: Long, settings: Settings): Result<Unit>

    fun createLobby(playerName: String): Result<Long>

    fun deleteLobby(lobbyId: Long): Result<Unit>

    fun getLobbyStatus(lobbyId: Long): Result<Status>

    fun getAllLobbies(): List<LobbyStatus>
}
