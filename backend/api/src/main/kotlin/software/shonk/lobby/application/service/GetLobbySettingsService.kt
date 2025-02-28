package software.shonk.lobby.application.service

import software.shonk.lobby.adapters.incoming.getLobbySettings.GetLobbySettingsCommand
import software.shonk.lobby.application.port.incoming.GetLobbySettingsQuery
import software.shonk.lobby.application.port.outgoing.LoadLobbyPort
import software.shonk.lobby.domain.InterpreterSettings

class GetLobbySettingsService(private val loadLobbyPort: LoadLobbyPort) : GetLobbySettingsQuery {

    override fun getLobbySettings(
        getLobbySettingsCommand: GetLobbySettingsCommand
    ): Result<InterpreterSettings> {
        return loadLobbyPort.getLobby(getLobbySettingsCommand.lobbyId.id).map { lobby ->
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
