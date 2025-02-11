package software.shonk.application.service

import io.mockk.clearMocks
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.adapters.outgoing.MemoryLobbyManager
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort
import software.shonk.domain.InterpreterSettings
import software.shonk.domain.Lobby
import software.shonk.interpreter.MockShork

class SetLobbySettingsServiceTest {

    private lateinit var setLobbySettingsService: SetLobbySettingsService
    // todo the stuff we do here with saveLobbyPort should be testhelpers that directly access the
    // MemoryLobbyManager stuff or something similar
    private lateinit var saveLobbyPort: SaveLobbyPort
    private lateinit var loadLobbyPort: LoadLobbyPort

    // The in-memory lobby management also serves as a kind of mock here.
    @BeforeEach
    fun setUp() {
        val lobbyManager = spyk<MemoryLobbyManager>()
        saveLobbyPort = lobbyManager
        loadLobbyPort = lobbyManager
        setLobbySettingsService = SetLobbySettingsService(loadLobbyPort, saveLobbyPort)
    }

    @Test
    fun `set settings for the lobby`() {
        val aLobbyId = 0L
        saveLobbyPort.saveLobby(
            Lobby(aLobbyId, hashMapOf(), MockShork(), joinedPlayers = mutableListOf("playerA"))
        )
        val someSettings = InterpreterSettings(69, 123, "NOP", 0)
        // Clear recorded interactions, this is needed because the previous saveLobby call would in
        // combination with
        // the field being changed below suffices to fulfill the matcher!
        clearMocks(saveLobbyPort)

        setLobbySettingsService.setLobbySettings(aLobbyId, someSettings)
        verify(exactly = 1) {
            saveLobbyPort.saveLobby(
                match { it.id == aLobbyId && it.currentSettings == someSettings }
            )
        }
    }

    @Test
    fun `set settings for invalid lobby`() {
        val someSettings = InterpreterSettings(69, 123, "NOP", 0)
        val result = setLobbySettingsService.setLobbySettings(0, someSettings)

        assertFalse(result.isSuccess)
        assertEquals("No lobby with that id", result.exceptionOrNull()?.message)
    }
}
