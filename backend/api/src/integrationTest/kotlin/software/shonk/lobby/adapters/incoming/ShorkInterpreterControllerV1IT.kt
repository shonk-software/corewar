package software.shonk.lobby.adapters.incoming

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import software.shonk.basicModule
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

    private suspend fun parseStatus(response: HttpResponse): Map<String, String> {
        val responseJson = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val resultWinner =
            responseJson["result"]?.jsonObject?.get("winner")?.jsonPrimitive?.content ?: "DRAW"
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
