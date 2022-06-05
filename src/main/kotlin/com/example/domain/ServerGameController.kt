package com.example.domain

import com.example.Connection
import com.example.mappers.toResponse
import com.example.models.request.Action
import com.example.models.request.CellPositionBody
import com.example.models.request.MousePositionBody
import com.example.models.request.StartGameBody
import com.example.models.response.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.util.*
import kotlin.collections.LinkedHashSet

class ServerGameController {
    private val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    private val gameUpdateMutex = Mutex()
    private var gameController = GameController(GameSettings.EASY)
    private val positionsController = PlayerPositionsController()

    suspend fun onNewConnection(connection: Connection) {
        connections += connection
        println("connect new user. Users connected: ${connections.size}")
        with(connection.session) {
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
                println("throws error: $t")
                t.printStackTrace()
            } finally {
                positionsController.removePlayer(connection.connectionId)
                connections -= connection
                println("disconnect user. Users connected: ${connections.size}")
                if (connections.isEmpty()) {
                    gameController = GameController(GameSettings.EASY)
                }
            }
        }
    }

    private suspend fun WebSocketServerSession.performMessages() {
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
    private suspend fun WebSocketServerSession.handleGameUpdateAction(action: Action, bodyElement: JsonElement) {

        when(action) {
            Action.StartGame -> {
                val body = kotlin.runCatching {
                    Json.decodeFromJsonElement<StartGameBody>(bodyElement)
                }.getOrNull() ?: throw IllegalArgumentException("Illegal format of body: $bodyElement")
                val difficulty = when(body.difficulty) {
                    "easy" -> GameSettings.EASY
                    "medium" -> GameSettings.MEDIUM
                    "hard" -> GameSettings.EXPERT
                    else -> throw IllegalArgumentException("unknown difficulty: ${body.difficulty}. Expected: easy, medium, hard")
                }
                gameController = GameController(difficulty)
            }
            Action.OpenCell -> {
                val body = kotlin.runCatching {
                    Json.decodeFromJsonElement<CellPositionBody>(bodyElement)
                }.getOrNull() ?: throw IllegalArgumentException("Illegal format of body: $bodyElement")
                val cell = gameController.cellAt(body.row, body.column)
                    ?: throw IllegalArgumentException("missing cell at row: ${body.row} and column ${body.column}")
                gameController.openCell(cell)
            }
            Action.SetFlag -> {
                val body = kotlin.runCatching {
                    Json.decodeFromJsonElement<CellPositionBody>(bodyElement)
                }.getOrNull() ?: throw IllegalArgumentException("Illegal format of body: $bodyElement")
                val cell = gameController.cellAt(body.row, body.column)
                    ?: throw IllegalArgumentException("missing cell at row: ${body.row} and column ${body.column}")
                gameController.toggleFlag(cell)
            }
            Action.MousePosition -> throw IllegalArgumentException("action $action is not a game update action. It must be handled in other way")
        }.let { }

        sendMessageForAll(MessageType.GameState, gameController.toResponse())

    }

    @kotlin.jvm.Throws(Throwable::class)
    private suspend fun WebSocketServerSession.handleMousePositionAction(bodyElement: JsonElement) {
        val body = kotlin.runCatching {
            Json.decodeFromJsonElement<MousePositionBody>(bodyElement)
        }.getOrNull() ?: throw IllegalArgumentException("Illegal format of body: $bodyElement, expected: fields: x: Int, y: Int")

        val id = connections.first { it.session == this }.connectionId
        positionsController.onNewPosition(id, body.x, body.y)
    }

    private suspend inline fun <reified T: Any> WebSocketServerSession.sendMessageForAll(
        type: MessageType,
        body: T,
        excludeSelf: Boolean = false
    )  {
        coroutineScope {
            connections.forEach { connection ->
                if (excludeSelf && connection.session == this) {
                    return@forEach
                }
                launch {
                    connection.session.sendMessage(type, body)
                }
            }
        }
    }
    private suspend inline fun <reified T: Any> WebSocketServerSession.sendMessage(type: MessageType, body: T) {
        val message = Message(type.apiKey, body)
        val messageJson = Json.encodeToString(message)
        send(messageJson)
    }
}
