package software.shonk.lobby.application.service

import io.mockk.clearMocks
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.A_LOBBY_ID_THAT_HAS_NOT_BEEN_CREATED
import software.shonk.A_VALID_LOBBY_ID
import software.shonk.aLobby
import software.shonk.lobby.adapters.incoming.getLobbySettings.GetLobbySettingsCommand
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.GetLobbySettingsQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.InterpreterSettings
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException

class GetLobbySettingsServiceTest {

    lateinit var loadLobbyPort: LoadLobbyPort
    lateinit var saveLobbyPort: SaveLobbyPort
    lateinit var getLobbySettingsQuery: GetLobbySettingsQuery

    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        loadLobbyPort = lobbyManager
        saveLobbyPort = lobbyManager
        getLobbySettingsQuery = GetLobbySettingsService(loadLobbyPort)
    }

    @Test
    fun `get lobby settings for valid lobby`() {
        // Given...
        val someSettings = InterpreterSettings(coreSize = 1234)
        saveLobbyPort.saveLobby(aLobby(id = A_VALID_LOBBY_ID, currentSettings = someSettings))
        clearMocks(saveLobbyPort)

        // When...
        val result =
            getLobbySettingsQuery.getLobbySettings(GetLobbySettingsCommand(A_VALID_LOBBY_ID))

        // Then...
        assertTrue(result.isSuccess)
        assertEquals(someSettings, result.getOrNull())
        verify(exactly = 1) { loadLobbyPort.getLobby(A_VALID_LOBBY_ID) }
    }

    @Test
    fun `test get lobby settings for lobby that does not exist lobby`() {
        // Given...

        // When...
        val result =
            getLobbySettingsQuery.getLobbySettings(
                GetLobbySettingsCommand(A_LOBBY_ID_THAT_HAS_NOT_BEEN_CREATED)
            )

        // Then...
        assertFalse(result.isSuccess)
        assertTrue { result.exceptionOrNull() is LobbyNotFoundException }
    }
}
