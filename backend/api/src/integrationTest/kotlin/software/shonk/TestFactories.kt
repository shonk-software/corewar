package software.shonk

import software.shonk.interpreter.IShork
import software.shonk.interpreter.MockShork
import software.shonk.lobby.domain.GameState
import software.shonk.lobby.domain.InterpreterSettings
import software.shonk.lobby.domain.Lobby
import software.shonk.lobby.domain.Winner

val GET_ALL_LOBBIES_ENDPOINT = "/api/v1/lobby"
val CREATE_LOBBY_ENDPOINT = "/api/v1/lobby"

val A_VALID_LOBBY_ID = 0L
val ANOTHER_VALID_LOBBY_ID = 1L
val AN_INVALID_LOBBY_ID = -1L
val A_LOBBY_ID_THAT_HAS_NOT_BEEN_CREATED = 1337L

val A_VALID_PLAYERNAME = "playerA"
val ANOTHER_VALID_PLAYERNAME = "playerB"
val AN_INVALID_PLAYERNAME = "invalidName :3"

val A_REDCODE_PROGRAM = "MOV 0, 1"

fun aLobby(
    id: Long = A_VALID_LOBBY_ID,
    programs: HashMap<String, String> = hashMapOf(),
    shork: IShork = MockShork(),
    gameState: GameState = GameState.NOT_STARTED,
    winner: Winner = Winner.DRAW,
    currentSettings: InterpreterSettings = InterpreterSettings(),
    joinedPlayers: MutableList<String> = mutableListOf(),
): Lobby {
    return Lobby(id, programs, shork, gameState, winner, currentSettings, joinedPlayers)
}

fun aLobbyThatHasAlreadyRun(
    id: Long = A_VALID_LOBBY_ID,
    programs: HashMap<String, String> =
        hashMapOf(
            A_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
            ANOTHER_VALID_PLAYERNAME to A_REDCODE_PROGRAM,
        ),
    shork: IShork = MockShork(),
    gameState: GameState = GameState.NOT_STARTED,
    winner: Winner = Winner.DRAW,
    currentSettings: InterpreterSettings = InterpreterSettings(),
    joinedPlayers: MutableList<String> = mutableListOf(A_VALID_PLAYERNAME, ANOTHER_VALID_PLAYERNAME),
): Lobby {
    val lobby = Lobby(id, programs, shork, gameState, winner, currentSettings, joinedPlayers)
    lobby.run()
    return lobby
}

fun someValidInterpreterSettings(
    coreSize: Int = 2048,
    instructionLimit: Int = 500,
    initialInstruction: String = "ADD",
    maximumTicks: Int = 100000,
    maximumProcessesPerPlayer: Int = 16,
    readDistance: Int = 100,
    writeDistance: Int = 100,
    minimumSeparation: Int = 50,
    separation: Int = 50,
    randomSeparation: Boolean = true,
): InterpreterSettings {
    return InterpreterSettings(
        coreSize,
        instructionLimit,
        initialInstruction,
        maximumTicks,
        maximumProcessesPerPlayer,
        readDistance,
        writeDistance,
        minimumSeparation,
        separation,
        randomSeparation,
    )
}
