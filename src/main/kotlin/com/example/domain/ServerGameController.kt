package com.example.domain

import com.example.Connection
import com.example.mappers.toResponse
import com.example.models.request.Action
import com.example.models.request.CellPositionBody
import com.example.models.request.StartGameBody
import com.example.models.response.Message
import com.example.models.response.MessageType
import com.example.models.response.ErrorResponse
import com.example.models.response.TickResponse
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.util.*
import kotlin.collections.LinkedHashSet

class ServerGameController {
    private val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    private val mutex = Mutex()
    private var gameController = GameController(GameSettings.EASY)

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
                performMessages()
                tickerJob.cancel()
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                println("throws error: $t")
                t.printStackTrace()
            } finally {
                connections -= connection
                println("disconnect user. Users connected: ${connections.size}")
                if (connections.isEmpty()) {
                    gameController.close()
                    gameController = GameController(GameSettings.EASY)
                }
            }
        }
    }

    private suspend fun WebSocketServerSession.performMessages() {
        for (frame in incoming) {
            frame as? Frame.Text ?: continue
            val text = frame.readText()
            val successfullyHandled = try {
                mutex.withLock {
                    handleIncomingMessage(text)
                }
                true
            } catch (t: Throwable) {
                sendMessage(MessageType.Error, ErrorResponse(t.message ?: t.toString()))
                false
            }
            if (successfullyHandled) {
                connections.forEach { connection ->
                    connection.session.sendMessage(MessageType.GameState, gameController.toResponse())
                }
            }
        }
    }

    @kotlin.jvm.Throws(Throwable::class)
    private fun handleIncomingMessage(text: String) {
        val json = kotlin.runCatching {
            Json.decodeFromString<JsonObject>(text)
        }.getOrNull() ?: throw IllegalArgumentException("Input data could not be represented as Json.")
        val messageType = json["action"]?.jsonPrimitive?.content?.let { action ->
            Action.values().firstOrNull { it.apiKey == action }
                ?: throw IllegalArgumentException("Illegal action: $action. Available actions: ${Action.values()}")
        } ?: throw IllegalArgumentException("'action' argument not found")

        val bodyElement = json["body"] ?: throw IllegalArgumentException("not found 'body' argument")

        when(messageType) {
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
                gameController.close()
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
        }
    }

    private suspend inline fun <reified T: Any> WebSocketServerSession.sendMessage(type: MessageType, body: T) {
        val message = Message(type.apiKey, body)
        val messageJson = Json.encodeToString(message)
        send(messageJson)
    }
}
