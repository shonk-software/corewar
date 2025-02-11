package software.shonk.application.service

import io.mockk.clearMocks
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.adapters.outgoing.MemoryLobbyManager
import software.shonk.application.port.incoming.GetLobbySettingsQuery
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort
import software.shonk.domain.InterpreterSettings
import software.shonk.domain.Lobby
import software.shonk.interpreter.MockShork

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
            getLobbySettingsQuery.getLobbySettings(aLobbyId).getOrNull(),
        )
        verify(exactly = 1) { loadLobbyPort.getLobby(aLobbyId) }
    }

    @Test
    fun `test get lobby settings for an invalid lobby`() {
        val result = getLobbySettingsQuery.getLobbySettings(999L)
        assertFalse(result.isSuccess)
        assertEquals("No lobby with that id", result.exceptionOrNull()?.message)
    }
}
