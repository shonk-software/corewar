package software.shonk.lobby.application.service

import io.mockk.clearMocks
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.*
import software.shonk.lobby.adapters.incoming.setLobbySettings.SetLobbySettingsCommand
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException

class SetLobbySettingsServiceTest {

    private lateinit var setLobbySettingsService: SetLobbySettingsService
    private lateinit var saveLobbyPort: SaveLobbyPort
    private lateinit var loadLobbyPort: LoadLobbyPort

    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        saveLobbyPort = lobbyManager
        loadLobbyPort = lobbyManager
        setLobbySettingsService = SetLobbySettingsService(loadLobbyPort, saveLobbyPort)
    }

    @Test
    fun `set settings for a valid, existing lobby succeeds and updates the settings`() {
        // Given...
        saveLobbyPort.saveLobby(
            aLobby(A_VALID_LOBBY_ID, joinedPlayers = mutableListOf(A_VALID_PLAYERNAME))
        )
        val someSettings = someValidInterpreterSettings()
        clearMocks(saveLobbyPort)

        // When...
        val result =
            setLobbySettingsService.setLobbySettings(
                SetLobbySettingsCommand(A_VALID_LOBBY_ID, someSettings)
            )

        // Then...
        assertTrue(result.isSuccess)
        verify(exactly = 1) {
            saveLobbyPort.saveLobby(
                match { it.id == A_VALID_LOBBY_ID && it.currentSettings == someSettings }
            )
        }
    }

    @Test
    fun `set settings for a lobby that does not exist fails with LobbyNotFoundException`() {
        // Given...
        val someSettings = someValidInterpreterSettings()

        // When...
        val result =
            setLobbySettingsService.setLobbySettings(
                SetLobbySettingsCommand(A_LOBBY_ID_THAT_HAS_NOT_BEEN_CREATED, someSettings)
            )

        // Then...
        assertTrue(result.isFailure)
        assertTrue { result.exceptionOrNull() is LobbyNotFoundException }
    }
}
