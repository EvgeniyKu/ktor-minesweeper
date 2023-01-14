package com.example.domain

import com.example.Connection
import com.example.mappers.toResponse
import com.example.models.request.*
import com.example.models.response.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.slf4j.Logger
import java.util.*
import kotlin.collections.LinkedHashSet

class GameRoom(
    private val logger: Logger
) {
    private val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    private val gameUpdateMutex = Mutex()
    private var gameController = GameController(GameSettings.EASY)
    private val positionsController = PlayerPositionsController()

    suspend fun onNewConnection(connection: Connection) {
        connections += connection
        logger.info("connect new user. Users connected: ${connections.size}")
        with(connection) {
            sendMessage(MessageType.GameState, gameController.toResponse())
            try {
                val tickerJob = launch {
                    while (true) {
                        if (gameController.running) {
                            sendMessage(MessageType.Tick, TickResponse(gameController.seconds))
                        }
                        delay(1000)
                    }
                }
                val playerPositionsJob = launch {
                    positionsController.positions
                        .map { it.filterKeys { it != connection.connectionId } }
                        .distinctUntilChanged()
                        .collect {
                            val playerPositions = it.map { (id, position) ->
                                PlayersPosition.Position(
                                    x = position.x,
                                    y = position.y,
                                    player = Player(id)
                                )
                            }
                            if (playerPositions.isNotEmpty()) {
                                sendMessage(MessageType.PlayerPosition, PlayersPosition(playerPositions))
                            }
                        }
                }
                performMessages()
                tickerJob.cancel()
                playerPositionsJob.cancel()
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                logger.error("throws error: $t", t)
            } finally {
                positionsController.removePlayer(connection.connectionId)
                connections -= connection
                logger.info("disconnect user. Users connected: ${connections.size}")
                if (connections.isEmpty()) {
                    gameController = GameController(GameSettings.EASY)
                }
            }
        }
    }

    private suspend fun Connection.performMessages() {
        for (frame in incoming) {
            frame as? Frame.Text ?: continue
            val text = frame.readText()
            try {
                val (action, body) = parseIncomingMessage(text)
                when(action) {
                    Action.MousePosition -> {
                        handleMousePositionAction(body)
                    }
                    else -> {
                        gameUpdateMutex.withLock {
                            handleGameUpdateAction(action, body)
                        }
                    }
                }
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                t.printStackTrace()
                logger.error("failure occurs", t)
                sendMessage(MessageType.Error, ErrorResponse(t.message ?: t.toString()))
            }
        }
    }

    private fun parseIncomingMessage(text: String): Pair<Action, JsonElement> {
        val json = kotlin.runCatching {
            Json.decodeFromString<JsonObject>(text)
        }.getOrNull() ?: throw IllegalArgumentException("Input data could not be represented as Json.")

        val action = json["action"]?.jsonPrimitive?.content?.let { action ->
            Action.values().firstOrNull { it.apiKey == action }
                ?: throw IllegalArgumentException("Illegal action: $action. Available actions: ${Action.values()}")
        } ?: throw IllegalArgumentException("'action' argument not found")

        val bodyElement = json["body"] ?: throw IllegalArgumentException("not found 'body' argument")

        return action to bodyElement
    }

    @kotlin.jvm.Throws(Throwable::class)
    private suspend fun Connection.handleGameUpdateAction(action: Action, bodyElement: JsonElement) {
        logger.info("user $connectionId make action ${action.apiKey} with params: $bodyElement")
        when(action) {
            Action.StartGame -> {
                val body = parseJsonBody<StartGameBody>(bodyElement)
                val difficulty = when(body.difficulty) {
                    "easy" -> GameSettings.EASY
                    "medium" -> GameSettings.MEDIUM
                    "hard" -> GameSettings.EXPERT
                    else -> throw IllegalArgumentException("unknown difficulty: ${body.difficulty}. Expected: easy, medium, hard")
                }
                gameController = GameController(difficulty)
            }
            Action.StartCustomGame -> {
                val body = parseJsonBody<StartCustomGameBody>(bodyElement)
                gameController = GameController(GameSettings(body.rows, body.columns, body.bombs))
            }
            Action.OpenCell -> {
                val body = parseJsonBody<CellPositionBody>(bodyElement)
                val cell = gameController.cellAt(body.row, body.column)
                    ?: throw IllegalArgumentException("missing cell at row: ${body.row} and column ${body.column}")
                gameController.openCell(cell)
            }
            Action.SetFlag -> {
                val body = parseJsonBody<CellPositionBody>(bodyElement)
                val cell = gameController.cellAt(body.row, body.column)
                    ?: throw IllegalArgumentException("missing cell at row: ${body.row} and column ${body.column}")
                gameController.toggleFlag(cell)
            }
            Action.MousePosition -> throw IllegalArgumentException("action $action is not a game update action. It must be handled in other way")
        }.let { }

        sendMessageForAll(MessageType.GameState, gameController.toResponse())

    }

    @kotlin.jvm.Throws(Throwable::class)
    private suspend fun Connection.handleMousePositionAction(bodyElement: JsonElement) {
        val body = parseJsonBody<MousePositionBody>(bodyElement, " expected: fields: x: Int, y: Int")
        positionsController.onNewPosition(connectionId, body.x, body.y)
    }

    private inline fun <reified T: Any> parseJsonBody(bodyElement: JsonElement, errorMessage: String = ""): T {
        return kotlin.runCatching {
            Json.decodeFromJsonElement<T>(bodyElement)
        }.getOrNull() ?: throw IllegalArgumentException("Illegal format of body: $bodyElement. $errorMessage")
    }

    private suspend inline fun <reified T: Any> Connection.sendMessageForAll(
        type: MessageType,
        body: T,
        excludeSelf: Boolean = false
    )  {
        coroutineScope {
            connections.forEach { connection ->
                if (excludeSelf && connection.connectionId == this@sendMessageForAll.connectionId) {
                    return@forEach
                }
                launch {
                    connection.sendMessage(type, body)
                }
            }
        }
    }
    private suspend inline fun <reified T: Any> Connection.sendMessage(type: MessageType, body: T) {
        val message = Message(type.apiKey, body)
        val messageJson = Json.encodeToString(message)
        send(messageJson)
    }
}
