package software.shonk.lobby.application.service

class ShorkServiceTest {

    // This is a DEFINITELY an integration test!!!
    /*
    @Test
    fun `result contains visualization data`() {
        val lobbyId = shorkService.createLobby("playerA").getOrThrow()
        shorkService.addProgramToLobby(lobbyId, "playerA", "mov 0, 1")
        shorkService.joinLobby(lobbyId, "playerB")
        shorkService.addProgramToLobby(lobbyId, "playerB", "mov 0, 1")

        val result = shorkService.getLobbyStatus(lobbyId).getOrThrow()
        assertEquals(GameState.FINISHED, result.gameState)
        assertEquals(Winner.DRAW, result.result.winner)
        assertTrue(result.visualizationData.isNotEmpty())
    }

    @Test
    fun `game visualization data absent before first run`() {
        val lobbyId = shorkService.createLobby("playerA").getOrThrow()
        shorkService.setLobbySettings(lobbyId, Settings())

        val result = shorkService.getLobbyStatus(lobbyId).getOrThrow()
        assertEquals(GameState.NOT_STARTED, result.gameState)
        assertEquals(Winner.DRAW, result.result.winner)
        assertTrue(result.visualizationData.isEmpty())
    }

    @Test
    fun `game visualization data can be disabled (not included)`() {
        val lobbyId = shorkService.createLobby("playerA").getOrThrow()
        shorkService.addProgramToLobby(lobbyId, "playerA", "mov 0, 1")
        shorkService.joinLobby(0L, "playerB")
        shorkService.addProgramToLobby(lobbyId, "playerB", "jmp 42")

        val result =
            shorkService.getLobbyStatus(lobbyId, includeRoundInformation = false).getOrThrow()
        assertEquals(GameState.FINISHED, result.gameState)
        assertTrue(result.visualizationData.isEmpty())
    }
    */
}
