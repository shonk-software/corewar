package software.shonk.application.service

import software.shonk.application.port.incoming.GetLobbySettingsQuery
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.domain.InterpreterSettings

class GetLobbySettingsService(private val loadLobbyPort: LoadLobbyPort) : GetLobbySettingsQuery {

    override fun getLobbySettings(lobbyId: Long): Result<InterpreterSettings> {
        return loadLobbyPort.getLobby(lobbyId).map { lobby ->
            with(lobby.getSettings()) {
                InterpreterSettings(
                    coreSize = coreSize,
                    instructionLimit = instructionLimit,
                    initialInstruction = initialInstruction,
                    maximumTicks = maximumTicks,
                    maximumProcessesPerPlayer = maximumProcessesPerPlayer,
                    readDistance = readDistance,
                    writeDistance = writeDistance,
                    minimumSeparation = minimumSeparation,
                    separation = separation,
                    randomSeparation = randomSeparation,
                )
            }
        }
    }
}
