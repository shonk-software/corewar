package software.shonk.lobby.adapters.incoming

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import software.shonk.basicModule
import software.shonk.interpreter.domain.CompileError
import software.shonk.lobby.domain.InterpreterSettings
import software.shonk.lobby.domain.LobbyStatus
import software.shonk.moduleApiV1

// todo big split
class ShorkInterpreterControllerV1IT : AbstractControllerTest() {

    override fun applyTestEngineApplication() {
        testEngine.application.apply {
            basicModule()
            moduleApiV1()
        }
    }

    @Serializable data class Program(val code: String)

    // Workaround for content negotiation plugin bullshit
    fun Program.json() = Json.encodeToString(Program.serializer(), this)

    @Serializable data class CompileErrorResponse(val errors: List<CompileError>)

    @Serializable data class JoinLobbyRequest(val playerName: String)

    fun JoinLobbyRequest.json() = Json.encodeToString(JoinLobbyRequest.serializer(), this)

    @Serializable data class JoinLobbyResponse(val lobbyId: Long)

    @Serializable
    data class VisualizationData(
        val playerId: String,
        val programCounterBefore: Int,
        val programCounterAfter: Int,
        val programCountersOfOtherProcesses: List<Int>,
        val memoryReads: List<Int>,
        val memoryWrites: List<Int>,
        val processDied: Boolean,
    )

    @Serializable
    enum class GameState {
        NOT_STARTED,
        RUNNING,
        FINISHED,
    }

    @Serializable
    enum class GameWinner {
        A,
        B,
        DRAW,
    }

    @Serializable data class GameResult(val winner: GameWinner)

    @Serializable
    data class LobbyStatusResponse(
        val playerASubmitted: Boolean,
        val playerBSubmitted: Boolean,
        val gameState: GameState,
        val result: GameResult,
        val visualizationData: List<VisualizationData>,
    )

    private suspend fun parseStatus(response: HttpResponse): Map<String, String> {
        val responseJson = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val result = responseJson["result"]?.jsonObject
        val resultWinner = result?.get("winner")?.jsonPrimitive?.content ?: "DRAW"
        return mapOf(
            "playerASubmitted" to responseJson["playerASubmitted"]!!.jsonPrimitive.content,
            "playerBSubmitted" to responseJson["playerBSubmitted"]!!.jsonPrimitive.content,
            "gameState" to responseJson["gameState"]!!.jsonPrimitive.content,
            "result.winner" to resultWinner,
        )
    }

    private suspend fun parseAllLobbies(response: HttpResponse): List<LobbyStatus> {
        val responseJson = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val lobbiesArray = responseJson["lobbies"]!!.jsonArray
        return lobbiesArray.map { Json.decodeFromJsonElement<LobbyStatus>(it) }
    }

    private suspend fun parseSettings(response: HttpResponse): InterpreterSettings {
        val responseJson = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val settingsJson = responseJson["settings"]!!.jsonObject["interpreterSettings"]!!.jsonObject
        return Json.decodeFromJsonElement(settingsJson)
    }

    @Nested
    inner class ReadAndWritePlayerCode {
        @Test
        fun `test post and get player code`() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }
            client.post("/api/v1/lobby/0/code/playerA") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\": \"someString\"}")
            }

            client.post("/api/v1/lobby/0/join") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerB\"}")
            }

            client.post("/api/v1/lobby/0/code/playerB") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\": \"someOtherString\"}")
            }

            val result = client.get("/api/v1/lobby/0/code/playerA")
            assertEquals(
                "someString",
                Json.parseToJsonElement(result.bodyAsText())
                    .jsonObject["code"]
                    ?.jsonPrimitive
                    ?.content,
            )

            val resultB = client.get("/api/v1/lobby/0/code/playerB")
            assertEquals(
                "someOtherString",
                Json.parseToJsonElement(resultB.bodyAsText())
                    .jsonObject["code"]
                    ?.jsonPrimitive
                    ?.content,
            )
        }

        @Test
        fun testGetPlayerCodeNotSubmitted() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }
            val result = client.get("/api/v1/lobby/0/code/playerA")
            assertEquals(HttpStatusCode.BadRequest, result.status)
            assert(result.bodyAsText().contains("No player with that name in the lobby"))
        }

        @Test
        fun `test player empty code submission`() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }
            val player = "playerA"
            val result =
                client.post("/api/v1/lobby/0/code/$player") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"code\":\"weDontCareWhatsInHereForThisTest\"}")
                }
            assertEquals(HttpStatusCode.OK, result.status)
        }

        @Test
        fun `test player code submission in invalid lobby`() = runTest {
            val player = "playerA"
            val result =
                client.post("/api/v1/lobby/0/code/$player") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"code\":\"weDontCareWhatsInHereForThisTest\"}")
                }
            assertEquals(HttpStatusCode.NotFound, result.status)
        }

        @Test
        fun `test player code submission with invalid username does not change lobby status`() =
            runTest {
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }
                val player = "playerC"
                client.post("/api/v1/lobby/0/code/$player") {
                    contentType(ContentType.Application.Json)
                    setBody("someString")
                }

                val result = client.get("/api/v1/lobby/0/code/playerC")
                assertEquals(HttpStatusCode.BadRequest, result.status)
                assert(result.bodyAsText().contains("No player with that name in the lobby"))

                val resultStatus = client.get("/api/v1/lobby/0/status")
                val responseData = parseStatus(resultStatus)
                assertEquals("false", responseData["playerASubmitted"])
                assertEquals("false", responseData["playerBSubmitted"])
                assertEquals("NOT_STARTED", responseData["gameState"])
                assertEquals("DRAW", responseData["result.winner"])
            }

        @Test
        fun `test player code submission reflecting in lobby status`() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }
            val player = "playerA"
            client.post("/api/v1/lobby/0/code/$player") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\":\"someString\"}")
            }

            val result = client.get("/api/v1/lobby/0/status")
            val responseData = parseStatus(result)
            assertEquals("true", responseData["playerASubmitted"])
            assertEquals("false", responseData["playerBSubmitted"])
            assertEquals("NOT_STARTED", responseData["gameState"])
            assertEquals("DRAW", responseData["result.winner"])
        }

        @Test
        fun `test game starts when both players submit`() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }
            client.post("/api/v1/lobby/0/code/playerA") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\":\"someVeryLongString\"}")
            }

            client.post("/api/v1/lobby/0/join") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerB\"}")
            }

            client.post("/api/v1/lobby/0/code/playerB") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\":\"someShortString\"}")
            }

            val result = client.get("/api/v1/lobby/0/status")
            val responseData = parseStatus(result)
            assertEquals("FINISHED", responseData["gameState"])
        }

        @Test
        fun `test lobby status resets after new code submission by player A`() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }
            client.post("/api/v1/lobby/0/code/playerA") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\":\"someString\"}")
            }

            client.post("/api/v1/lobby/0/code/playerB") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\":\"someOtherString\"}")
            }
            client.post("/api/v1/lobby/0/code/playerA") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\":\"someNewString\"}")
            }

            val result = client.get("/api/v1/lobby/0/status")
            val responseData = parseStatus(result)
            assertEquals("true", responseData["playerASubmitted"])
            assertEquals("false", responseData["playerBSubmitted"])
            assertEquals("NOT_STARTED", responseData["gameState"])
            assertEquals("DRAW", responseData["result.winner"])
        }

        @Test
        fun `test post player code to a lobby which you have not joined yet`() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }
            val player = "playerB"
            val result =
                client.post("/api/v1/lobby/0/code/$player") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"code\":\"ForbiddenPlayerCode\"}")
                }
            assertEquals(HttpStatusCode.Forbidden, result.status)
        }

        @Test
        fun `test submitting code only works after joining`() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }
            val resultA =
                client.post("/api/v1/lobby/0/code/playerA") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"code\":\"PlayerCode\"}")
                }
            assertEquals(HttpStatusCode.OK, resultA.status)

            val resultTryToPostNotInLobby =
                client.post("/api/v1/lobby/0/code/playerB") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"code\":\"ForbiddenPlayerCode\"}")
                }
            assertEquals(HttpStatusCode.Forbidden, resultTryToPostNotInLobby.status)

            client.post("/api/v1/lobby/0/join") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerB\"}")
            }

            val resultB =
                client.post("/api/v1/lobby/0/code/playerB") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"code\":\"PlayerCode\"}")
                }
            assertEquals(HttpStatusCode.OK, resultB.status)
        }
    }

    @Nested
    inner class GetLobbyStatus {
        @Test
        fun `test get lobby status with lobby id that doesn't exist`() = runTest {
            val result = client.get("/api/v1/lobby/1/status")
            assertEquals(HttpStatusCode.NotFound, result.status)
            assert(result.bodyAsText().contains("Lobby with id 1 not found!"))
        }

        @Test
        fun `test get lobby status with valid default ID`() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }
            val result = client.get("/api/v1/lobby/0/status")
            val responseData = parseStatus(result)
            assertEquals("false", responseData["playerASubmitted"])
            assertEquals("false", responseData["playerBSubmitted"])
            assertEquals("NOT_STARTED", responseData["gameState"])
            assertEquals("DRAW", responseData["result.winner"])
        }

        @Test
        fun `test get lobby status with valid custom ID`() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }
            client.post("/api/v1/lobby/0/code/playerA") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\":\"someString\"}")
            }

            client.post("/api/v1/lobby/0/join") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerB\"}")
            }

            client.post("/api/v1/lobby/0/code/playerB") {
                contentType(ContentType.Application.Json)
                setBody("{\"code\":\"someOtherString\"}")
            }

            val result = client.get("/api/v1/lobby/0/status")
            val responseData = parseStatus(result)
            assertEquals("true", responseData["playerASubmitted"])
            assertEquals("true", responseData["playerBSubmitted"])
            assertEquals("FINISHED", responseData["gameState"])
            assertEquals("B", responseData["result.winner"])
        }

        @Test
        fun `test if game visualization data is absent before any game round has run`() = runTest {
            val joinLobbyResponse =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody(JoinLobbyRequest("playerA").json())
                }
            assertEquals(HttpStatusCode.Created, joinLobbyResponse.status)

            val lobby =
                Json.decodeFromString(
                    JoinLobbyResponse.serializer(),
                    joinLobbyResponse.bodyAsText(),
                )
            val lobbyId = lobby.lobbyId

            val lobbyStatusResponse = client.get("/api/v1/lobby/$lobbyId/status")
            assertEquals(HttpStatusCode.OK, lobbyStatusResponse.status)

            val lobbyStatus =
                Json.decodeFromString(
                    LobbyStatusResponse.serializer(),
                    lobbyStatusResponse.bodyAsText(),
                )
            assertEquals(GameState.NOT_STARTED, lobbyStatus.gameState)
            assertTrue(lobbyStatus.visualizationData.isEmpty())
        }

        @Test
        fun `test if game visualization data is present after a game round has run`() = runTest {
            val joinLobbyResponse =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody(JoinLobbyRequest("playerA").json())
                }
            assertEquals(HttpStatusCode.Created, joinLobbyResponse.status)

            val lobby =
                Json.decodeFromString(
                    JoinLobbyResponse.serializer(),
                    joinLobbyResponse.bodyAsText(),
                )
            val lobbyId = lobby.lobbyId

            val playerACode =
                client.post("/api/v1/lobby/$lobbyId/code/playerA") {
                    contentType(ContentType.Application.Json)
                    setBody(Program("MOV 0, 1").json())
                }
            assertEquals(HttpStatusCode.OK, playerACode.status)

            client.post("/api/v1/lobby/0/join") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerB\"}")
            }

            val playerBCode =
                client.post("/api/v1/lobby/$lobbyId/code/playerB") {
                    contentType(ContentType.Application.Json)
                    setBody(Program("MOV 0, 1").json())
                }
            assertEquals(HttpStatusCode.OK, playerBCode.status)

            val lobbyStatusResponse = client.get("/api/v1/lobby/$lobbyId/status")
            assertEquals(HttpStatusCode.OK, lobbyStatusResponse.status)

            val lobbyStatus =
                Json.decodeFromString(
                    LobbyStatusResponse.serializer(),
                    lobbyStatusResponse.bodyAsText(),
                )
            assertEquals(GameState.FINISHED, lobbyStatus.gameState)
            assertTrue(lobbyStatus.visualizationData.isNotEmpty())
        }

        @Test
        fun `test if game visualization data can be excluded with query parameter`() = runTest {
            val joinLobbyResponse =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody(JoinLobbyRequest("playerA").json())
                }
            assertEquals(HttpStatusCode.Created, joinLobbyResponse.status)

            val lobby =
                Json.decodeFromString(
                    JoinLobbyResponse.serializer(),
                    joinLobbyResponse.bodyAsText(),
                )
            val lobbyId = lobby.lobbyId

            val playerACode =
                client.post("/api/v1/lobby/$lobbyId/code/playerA") {
                    contentType(ContentType.Application.Json)
                    setBody(Program("MOV 0, 1").json())
                }
            assertEquals(HttpStatusCode.OK, playerACode.status)

            client.post("/api/v1/lobby/0/join") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerB\"}")
            }

            val playerBCode =
                client.post("/api/v1/lobby/$lobbyId/code/playerB") {
                    contentType(ContentType.Application.Json)
                    setBody(Program("MOV 0, 1").json())
                }
            assertEquals(HttpStatusCode.OK, playerBCode.status)

            // Test if the query parameter works
            val lobbyStatusResponse =
                client.get("/api/v1/lobby/$lobbyId/status?showVisualizationData=true")
            assertEquals(HttpStatusCode.OK, lobbyStatusResponse.status)

            val lobbyStatus =
                Json.decodeFromString(
                    LobbyStatusResponse.serializer(),
                    lobbyStatusResponse.bodyAsText(),
                )
            assertEquals(GameState.FINISHED, lobbyStatus.gameState)
            assertTrue(lobbyStatus.visualizationData.isNotEmpty())

            // Now to test if the query parameter also works to disable the visualization data
            val lobbyStatusNoVisualizationData =
                client.get("/api/v1/lobby/$lobbyId/status?showVisualizationData=false")
            assertEquals(HttpStatusCode.OK, lobbyStatusNoVisualizationData.status)

            val lobbyStatusNoVisualizationDataParsed =
                Json.decodeFromString(
                    LobbyStatusResponse.serializer(),
                    lobbyStatusNoVisualizationData.bodyAsText(),
                )
            assertEquals(GameState.FINISHED, lobbyStatusNoVisualizationDataParsed.gameState)
            assertTrue(lobbyStatusNoVisualizationDataParsed.visualizationData.isEmpty())
        }
    }

    @Nested
    inner class CreateLobby {
        @Test
        fun `test create a new lobby with playerName`() = runTest {
            val result =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }
            assertEquals(HttpStatusCode.Created, result.status)
            val responseId =
                Json.parseToJsonElement(result.bodyAsText())
                    .jsonObject["lobbyId"]!!
                    .jsonPrimitive
                    .content
            assertEquals("0", responseId)
        }

        @Test
        fun `test create a multiple new lobbies`() = runTest {
            val result =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }
            val result2 =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerB\"}")
                }
            val result3 =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerC\"}")
                }
            assertEquals(HttpStatusCode.Created, result.status)
            val responseId0 =
                Json.parseToJsonElement(result.bodyAsText())
                    .jsonObject["lobbyId"]!!
                    .jsonPrimitive
                    .content
            assertEquals("0", responseId0)
            assertEquals(HttpStatusCode.Created, result2.status)
            val responseId1 =
                Json.parseToJsonElement(result2.bodyAsText())
                    .jsonObject["lobbyId"]!!
                    .jsonPrimitive
                    .content
            assertEquals("1", responseId1)
            assertEquals(HttpStatusCode.Created, result3.status)
            val responseId2 =
                Json.parseToJsonElement(result3.bodyAsText())
                    .jsonObject["lobbyId"]!!
                    .jsonPrimitive
                    .content
            assertEquals("2", responseId2)
        }

        @Test
        fun `test create a new lobby with invalid playerName`() = runTest {
            val result =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"\"}")
                }
            assertEquals(HttpStatusCode.BadRequest, result.status)
            assertEquals("Your player name is invalid", result.bodyAsText())
        }

        @Test
        fun `test create lobby with invalid json body`() = runTest {
            val result =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{ invalid, : json :3")
                }
            assertEquals(HttpStatusCode.BadRequest, result.status)
        }

        @Test
        fun `test create lobby with playerName null`() = runTest {
            val result =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{ }")
                }

            assertEquals(HttpStatusCode.BadRequest, result.status)
        }
    }

    @Nested
    inner class JoinLobby {
        @Test
        fun `test join lobby with valid playerName`() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }
            val result =
                client.post("/api/v1/lobby/0/join") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerB\"}")
                }
            assertEquals(HttpStatusCode.OK, result.status)
        }

        @Test
        fun `test join lobby with duplicate (invalid) playerName`() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }
            val result =
                client.post("/api/v1/lobby/0/join") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }
            assertEquals(HttpStatusCode.Conflict, result.status)
        }

        @Test
        fun `test join lobby with invalid json`() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }
            val result =
                client.post("/api/v1/lobby/0/join") {
                    contentType(ContentType.Application.Json)
                    setBody("{ invalid, : json :3")
                }
            assertEquals(HttpStatusCode.BadRequest, result.status)
        }

        @Test
        fun `test join nonexistent lobby`() = runTest {
            val result =
                client.post("/api/v1/lobby/0/join") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }
            assertEquals(HttpStatusCode.NotFound, result.status)
        }
    }

    @Nested
    inner class GetAllLobbies {
        @Test
        fun `test get all lobbies when there is only one and it's not initialized`() = runTest {
            client.post("/api/v1/lobby") {
                contentType(ContentType.Application.Json)
                setBody("{\"playerName\":\"playerA\"}")
            }

            val result = client.get("/api/v1/lobby")
            val parsedLobbies = parseAllLobbies(result)

            assertEquals(HttpStatusCode.OK, result.status)
            assertEquals(1, parsedLobbies.size)

            assertTrue(
                parsedLobbies.contains(
                    LobbyStatus(
                        id = 0L,
                        playersJoined = listOf("playerA"),
                        gameState = "NOT_STARTED",
                    )
                )
            )
        }

        @Test
        fun `test get all lobbies as a list with players and games status for more than one lobby`() =
            runTest {
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }

                val result = client.get("/api/v1/lobby")
                val parsedLobbies = parseAllLobbies(result)

                assertEquals(HttpStatusCode.OK, result.status)
                assertEquals(3, parsedLobbies.size)

                assertTrue(
                    parsedLobbies.contains(
                        LobbyStatus(
                            id = 0L,
                            playersJoined = listOf("playerA"),
                            gameState = "NOT_STARTED",
                        )
                    )
                )

                assertTrue(
                    parsedLobbies.contains(
                        LobbyStatus(
                            id = 1L,
                            playersJoined = listOf("playerA"),
                            gameState = "NOT_STARTED",
                        )
                    )
                )

                assertTrue(
                    parsedLobbies.contains(
                        LobbyStatus(
                            id = 2L,
                            playersJoined = listOf("playerA"),
                            gameState = "NOT_STARTED",
                        )
                    )
                )
            }
    }

    @Nested
    inner class RedcodeCompileErrors {
        @Test
        fun `test compile invalid json`() = runTest {
            val result =
                client.post("/api/v1/redcode/compile/errors") {
                    contentType(ContentType.Application.Json)
                    setBody("{ invalid, : json >:3c")
                }

            assertEquals(HttpStatusCode.BadRequest, result.status)
        }

        @Test
        fun `test compile no errors`() = runTest {
            val result =
                client.post("/api/v1/redcode/compile/errors") {
                    contentType(ContentType.Application.Json)
                    setBody(Program("MOV 0, 1").json())
                }

            assertEquals(HttpStatusCode.OK, result.status)
        }

        @Test
        fun `test compile with errors`() = runTest {
            val result =
                client.post("/api/v1/redcode/compile/errors") {
                    contentType(ContentType.Application.Json)
                    setBody(Program("ASduhsdlfuhsdf dlfasuihfals Totally valid code :333").json())
                }

            assertEquals(HttpStatusCode.OK, result.status)
            val response =
                Json.decodeFromString(CompileErrorResponse.serializer(), result.bodyAsText())
            assertTrue(response.errors.isNotEmpty())
        }
    }

    @Nested
    inner class GetLobbySettings {
        @Test
        fun `test get lobby settings for an existing lobby`() = runTest {
            val clientLobby =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }
            val lobbyId =
                Json.parseToJsonElement(clientLobby.bodyAsText())
                    .jsonObject["lobbyId"]!!
                    .jsonPrimitive
                    .content
            val result = client.get("/api/v1/lobby/$lobbyId/settings")
            val parsedSettings = parseSettings(result)
            val defaultSettings = InterpreterSettings()

            assertEquals(HttpStatusCode.OK, result.status)
            assertTrue(
                parsedSettings ==
                    InterpreterSettings(
                        coreSize = defaultSettings.coreSize,
                        instructionLimit = defaultSettings.instructionLimit,
                        initialInstruction = defaultSettings.initialInstruction,
                        maximumTicks = defaultSettings.maximumTicks,
                        maximumProcessesPerPlayer = defaultSettings.maximumProcessesPerPlayer,
                        readDistance = defaultSettings.readDistance,
                        writeDistance = defaultSettings.writeDistance,
                        minimumSeparation = defaultSettings.minimumSeparation,
                        separation = defaultSettings.separation,
                        randomSeparation = defaultSettings.randomSeparation,
                    )
            )
        }

        @Test
        fun `test get lobby settings for a non-existing lobby`() = runTest {
            val result = client.get("/api/v1/lobby/0/settings")
            assertEquals(HttpStatusCode.NotFound, result.status)
        }
    }

    @Nested
    inner class PostLobbySettings {
        @Test
        fun `update settings for an existing lobby`() = runTest {
            val lobby =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }
            val lobbyId =
                Json.parseToJsonElement(lobby.bodyAsText())
                    .jsonObject["lobbyId"]!!
                    .jsonPrimitive
                    .content
            val updatedSettings =
                InterpreterSettings(
                    coreSize = 2048,
                    instructionLimit = 500,
                    initialInstruction = "ADD",
                    maximumTicks = 100000,
                    maximumProcessesPerPlayer = 16,
                    readDistance = 100,
                    writeDistance = 100,
                    minimumSeparation = 50,
                    separation = 50,
                    randomSeparation = true,
                )

            val customSettings =
                client.post("/api/v1/lobby/$lobbyId/settings") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(updatedSettings))
                }

            assertEquals(HttpStatusCode.OK, customSettings.status)

            // test if the settings got updated
            val getSettingsResponse = client.get("/api/v1/lobby/$lobbyId/settings")
            val parsedSettings = parseSettings(getSettingsResponse)

            assertEquals(updatedSettings, parsedSettings)
        }

        @Test
        fun `fail to update settings for a non-existent lobby`() = runTest {
            val updatedSettings =
                InterpreterSettings(
                    coreSize = 2048,
                    instructionLimit = 500,
                    initialInstruction = "ADD",
                    maximumTicks = 100000,
                    maximumProcessesPerPlayer = 16,
                    readDistance = 100,
                    writeDistance = 100,
                    minimumSeparation = 50,
                    separation = 50,
                    randomSeparation = true,
                )

            val updateResponse =
                client.post("/api/v1/lobby/999/settings") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(updatedSettings))
                }

            assertEquals(HttpStatusCode.NotFound, updateResponse.status)
        }

        @Test
        fun `fail to update settings with invalid settings`() = runTest {
            val clientLobby =
                client.post("/api/v1/lobby") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"playerName\":\"playerA\"}")
                }
            val lobbyId =
                Json.parseToJsonElement(clientLobby.bodyAsText())
                    .jsonObject["lobbyId"]!!
                    .jsonPrimitive
                    .content

            val invalidSettings = """{ "coreSize": "INVALID_NUMBER" }"""

            val updateResponse =
                client.post("/api/v1/lobby/$lobbyId/settings") {
                    contentType(ContentType.Application.Json)
                    setBody(invalidSettings)
                }

            assertEquals(HttpStatusCode.BadRequest, updateResponse.status)
        }
    }
}
