package software.shonk.application.service

import kotlin.Result
import software.shonk.application.port.incoming.ShorkUseCase
import software.shonk.application.port.outgoing.DeleteLobbyPort
import software.shonk.application.port.outgoing.LoadLobbyPort
import software.shonk.application.port.outgoing.SaveLobbyPort
import software.shonk.domain.*
import software.shonk.interpreter.IShork
import software.shonk.interpreter.Settings
import software.shonk.interpreter.internal.compiler.Compiler

const val NO_LOBBY_MESSAGE = "No lobby with that id"

class ShorkService(
    private val shork: IShork,
    private val loadLobbyPort: LoadLobbyPort,
    private val saveLobbyPort: SaveLobbyPort,
    private val deleteLobbyPort: DeleteLobbyPort,
) : ShorkUseCase {

    private var lobbyCounter = 0L

    override fun setLobbySettings(lobbyId: Long, settings: Settings): Result<Unit> {
        val lobby =
            loadLobbyPort.getLobby(lobbyId).getOrElse {
                return Result.failure(it)
            }

        lobby.setSettings(settings)
        return Result.success(Unit)
    }

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

    override fun addProgramToLobby(lobbyId: Long, name: String?, program: String): Result<Unit> {
        val lobby =
            loadLobbyPort.getLobby(lobbyId).getOrElse {
                return Result.failure(it)
            }
        if (lobby.gameState == GameState.FINISHED) {
            return Result.failure(
                IllegalStateException("You can't submit code to a lobby that is finished!")
            )
        }

        if (name == null || !verifyPlayerName(name)) {
            return Result.failure(IllegalArgumentException("Invalid player name"))
        }

        if (playerIsInLobby(name.toString(), lobbyId).isFailure) {
            return Result.failure(
                IllegalStateException("You can't submit code to a lobby you have not joined!")
            )
        }

        lobby.addProgram(name, program)
        return saveLobbyPort.saveLobby(lobby)
    }

    override fun getProgramFromLobbyWithId(lobbyId: Long, name: String?): Result<String> {
        val lobby =
            loadLobbyPort.getLobby(lobbyId).getOrElse {
                return Result.failure(it)
            }

        val result = lobby.programs[name]
        return if (result == null) {
            Result.failure(IllegalArgumentException("No player with that name in the lobby"))
        } else {
            Result.success(result)
        }
    }

    override fun getLobbyStatus(lobbyId: Long, includeRoundInformation: Boolean): Result<Status> {
        val lobby =
            loadLobbyPort.getLobby(lobbyId).getOrElse {
                return Result.failure(it)
            }

        val status = lobby.getStatus()
        if (!includeRoundInformation) {
            status.visualizationData = emptyList()
        }

        return Result.success(status)
    }

    override fun getCompilationErrors(code: String): List<CompileError> {
        val compiler = Compiler(code)
        return compiler.allErrors.map {
            CompileError(
                line = it.lineNumber,
                message = it.message,
                columnStart = it.lineCharIndexStart,
                columnEnd = it.lineCharIndexEnd,
            )
        }
    }

    override fun createLobby(playerName: String): Result<Long> {
        if (isAlphaNumerical(playerName)) {
            val newLobby = Lobby(lobbyCounter, HashMap(), shork)
            newLobby.joinedPlayers.add(playerName)
            if (saveLobbyPort.saveLobby(newLobby).isSuccess) {
                return Result.success(lobbyCounter++)
            }
        }
        return Result.failure(IllegalArgumentException("Your player name is invalid"))
    }

    override fun joinLobby(lobbyId: Long, playerName: String): Result<Unit> {
        val lobby =
            loadLobbyPort.getLobby(lobbyId).getOrElse {
                return Result.failure(it)
            }

        if (isAlphaNumerical(playerName) && playerIsInLobby(playerName, lobbyId).isFailure) {
            lobby.joinedPlayers.add(playerName)
            return saveLobbyPort.saveLobby(lobby)
        }
        return Result.failure(
            IllegalArgumentException("Your player name is invalid OR Lobby doesn't exist")
        )
    }

    // todo: why does this say lobby but returns lobby status?
    override fun getAllLobbies(): Result<List<LobbyStatus>> {
        return loadLobbyPort.getAllLobbies()
    }

    // todo move to lobby domain object
    // todo introduce player object instead of stirng primitive
    // todo introduce lobby identifier DO instead of long
    fun playerIsInLobby(playerName: String, lobbyId: Long): Result<Unit> {
        val lobby =
            loadLobbyPort.getLobby(lobbyId).getOrElse {
                return Result.failure(it)
            }
        if (lobby.joinedPlayers.contains(playerName)) {
            return Result.success(Unit)
        }
        return Result.failure(IllegalArgumentException("You have not joined a lobby!"))
    }

    override fun deleteLobby(lobbyId: Long): Result<Unit> {
        return deleteLobbyPort.deleteLobby(lobbyId)
    }

    private fun verifyPlayerName(player: String?): Boolean {
        return player == "playerA" || player == "playerB"
    }

    // todo This should be checkd in a command obj
    private fun isAlphaNumerical(playerName: String): Boolean {
        return playerName.matches("^[a-zA-Z0-9]+$".toRegex()) && playerName.isNotBlank()
    }
}
