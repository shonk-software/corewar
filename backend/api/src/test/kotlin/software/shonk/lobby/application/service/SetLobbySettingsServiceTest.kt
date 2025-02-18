package software.shonk.lobby.application.service

import io.mockk.clearMocks
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.shonk.interpreter.MockShork
import software.shonk.lobby.adapters.incoming.setLobbySettings.SetLobbySettingsCommand
import software.shonk.lobby.adapters.outgoing.MemoryLobbyManager
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.application.port.outgoing.SaveLobbyPort
import software.shonk.lobby.domain.InterpreterSettings
import software.shonk.lobby.domain.Lobby
import software.shonk.lobby.domain.exceptions.LobbyNotFoundException

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

        setLobbySettingsService.setLobbySettings(SetLobbySettingsCommand(aLobbyId, someSettings))
        verify(exactly = 1) {
            saveLobbyPort.saveLobby(
                match { it.id == aLobbyId && it.currentSettings == someSettings }
            )
        }
    }

    // todo this might already be an integration test with how its using the memoryLobbyManager,
    // maybe take another look
    @Test
    fun `set settings for invalid lobby`() {
        val aLobbyIdThatDoesNotExist = 0L
        val someSettings = InterpreterSettings(69, 123, "NOP", 0)
        val result =
            setLobbySettingsService.setLobbySettings(
                SetLobbySettingsCommand(aLobbyIdThatDoesNotExist, someSettings)
            )

        assertFalse(result.isSuccess)
        assertEquals(
            LobbyNotFoundException(aLobbyIdThatDoesNotExist).message,
            result.exceptionOrNull()?.message,
        )
    }
}
