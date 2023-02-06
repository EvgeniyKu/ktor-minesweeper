package com.example.domain

import com.example.Gamer
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
import kotlinx.serialization.json.*
import org.slf4j.Logger
import java.awt.Color
import java.util.*
import kotlin.collections.LinkedHashSet

class GameRoom(
    val roomName: String,
    private val logger: Logger,
    private val initialSettings: GameSettings
) {
    private val gamers = Collections.synchronizedSet<Gamer?>(LinkedHashSet())
    private val gameUpdateMutex = Mutex()
    private var gameController = GameController(initialSettings)
    private val positionsController = PlayerPositionsController()

    val isEmpty: Boolean
        get() = gamers.isEmpty()

    val gamersCount: Int
        get() = gamers.size

    suspend fun connectNewGamer(gamer: Gamer) {
        gamers += gamer
        logger.info("connect new gamer $gamer. Room $roomName contains ${gamers.size} gamers")
        with(gamer) {
            sendMessage(MessageType.GameState, gameController.toResponse(roomName, gamers.toList()))
            sendMessageForAll(MessageType.PlayersChanged, gamers.toResponse())
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
                        .map { it.filterKeys { it != gamer.connectionId } }
                        .distinctUntilChanged()
                        .collect {
                            val playerPositions = it.mapNotNull map@{ (id, position) ->
                                PlayersPosition.Position(
                                    x = position.x,
                                    y = position.y,
                                    playerId = id
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
                logger.error("throws error in room $name: $t", t)
            } finally {
                positionsController.removePlayer(gamer.connectionId)
                gamers -= gamer
                withContext(NonCancellable) {
                    sendMessageForAll(MessageType.PlayersChanged, gamers.toResponse(), excludeSelf = true)
                }
                logger.info("Gamer $gamer disconnected. Already connected: ${gamers.size} gamers")
            }
        }
    }

    fun generateColorForNewUser(): String {
        val availableColors = setOf<String>(
            "#4a4e4d",
            "#0e9aa7",
            "#f6cd61",
            "#fe8a71",
            "#009688",
            "#f37736",
            "#854442"
        )
        val currentUsedColors = gamers.map { it.color }.toSet()
        val freeColors = availableColors - currentUsedColors
        return if (freeColors.isEmpty()) {
            availableColors.random()
        } else {
            freeColors.random()
        }
    }

    private suspend fun Gamer.performMessages() {
        for (frame in incoming) {
            frame as? Frame.Text ?: continue
            performMessage(frame.readText())
        }
    }

    private suspend fun Gamer.performMessage(text: String) {
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
            logger.error("failure occurs in room $name", t)
            sendMessage(MessageType.Error, ErrorResponse(t.message ?: t.toString()))
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
    private suspend fun Gamer.handleGameUpdateAction(action: Action, bodyElement: JsonElement) {
        logger.info("user $this make action ${action.apiKey} with params: $bodyElement")
        when(action) {
            Action.Restart -> {
                gameController = GameController(initialSettings)
            }
            Action.OpenCell -> {
                val body = parseJsonBody<CellPositionBody>(bodyElement)
                gameController.openCell(body.row, body.column, connectionId)
            }
            Action.SetFlag -> {
                val body = parseJsonBody<CellPositionBody>(bodyElement)
                gameController.toggleFlag(body.row, body.column, connectionId)
            }
            Action.MousePosition -> throw IllegalArgumentException("action $action is not a game update action. It must be handled in other way")
        }.let { }

        sendMessageForAll(MessageType.GameState, gameController.toResponse(roomName, gamers.toList()))

    }

    @kotlin.jvm.Throws(Throwable::class)
    private suspend fun Gamer.handleMousePositionAction(bodyElement: JsonElement) {
        val body = parseJsonBody<MousePositionBody>(bodyElement, " expected: fields: x: Int, y: Int")
        positionsController.onNewPosition(connectionId, body.x, body.y)
    }

    private inline fun <reified T: Any> parseJsonBody(bodyElement: JsonElement, errorMessage: String = ""): T {
        return kotlin.runCatching {
            Json.decodeFromJsonElement<T>(bodyElement)
        }.getOrNull() ?: throw IllegalArgumentException("Illegal format of body: $bodyElement. $errorMessage")
    }

    private suspend inline fun <reified T: Any> Gamer.sendMessageForAll(
        type: MessageType,
        body: T,
        excludeSelf: Boolean = false
    )  {
        coroutineScope {
            gamers.forEach { connection ->
                if (excludeSelf && connection.connectionId == this@sendMessageForAll.connectionId) {
                    return@forEach
                }
                launch {
                    connection.sendMessage(type, body)
                }
            }
        }
    }
    private suspend inline fun <reified T: Any> Gamer.sendMessage(type: MessageType, body: T) {
        val message = Message(type.apiKey, body)
        sendSerialized(message)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameRoom

        if (roomName != other.roomName) return false

        return true
    }

    override fun hashCode(): Int {
        return roomName.hashCode()
    }
}
