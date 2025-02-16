package software.shonk.lobby.application.service

import io.mockk.clearMocks
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.incoming.GetLobbySettingsCommand
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.incoming.GetLobbySettingsQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.InterpreterSettings
import software.shonk.lobby.domain.Lobby

class GetLobbySettingsServiceTest {

    lateinit var loadLobbyPort: LoadLobbyPort
    // todo the stuff we do here with saveLobbyPort should be testhelpers that directly access the
    // MemoryLobbyManager stuff or something similar
    lateinit var saveLobbyPort: SaveLobbyPort
    lateinit var getLobbySettingsQuery: GetLobbySettingsQuery

    // The in-memory lobby management also serves as a kind of mock here.
    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        loadLobbyPort = lobbyManager
        saveLobbyPort = lobbyManager
        getLobbySettingsQuery = GetLobbySettingsService(loadLobbyPort)
    }

    @Test
    fun `get lobby settings for valid lobby`() {
        val someSettings = InterpreterSettings(coreSize = 1234)
        val aLobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(aLobbyId, hashMapOf(), MockShork(), currentSettings = someSettings)
        )

        clearMocks(saveLobbyPort)
        assertEquals<InterpreterSettings?>(
            someSettings,
            getLobbySettingsQuery.getLobbySettings(GetLobbySettingsCommand(aLobbyId)).getOrNull(),
        )
        verify(exactly = 1) { loadLobbyPort.getLobby(aLobbyId) }
    }

    @Test
    fun `test get lobby settings for an invalid lobby`() {
        val result = getLobbySettingsQuery.getLobbySettings(GetLobbySettingsCommand(999L))
        assertFalse(result.isSuccess)
        assertEquals("No lobby with that id", result.exceptionOrNull()?.message)
    }
}
