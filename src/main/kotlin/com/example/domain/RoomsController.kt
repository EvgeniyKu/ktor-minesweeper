package com.example.domain

import com.example.Gamer
import com.example.models.request.ConnectRequest
import com.example.models.response.ErrorResponse
import com.example.models.response.Message
import com.example.models.response.MessageType
import io.ktor.server.websocket.*
import org.slf4j.Logger
import java.util.*
import kotlin.collections.LinkedHashSet

class RoomsController(private val logger: Logger) {
    private val rooms = Collections.synchronizedSet<GameRoom>(LinkedHashSet())

    suspend fun onNewConnection(session: WebSocketServerSession) {
        val connectionRequest = try {
            session.receiveDeserialized<ConnectRequest>()
        } catch (t: Throwable) {
            val message = Message(MessageType.Error.apiKey, ErrorResponse("unrecognized request. First message must contains `playerName` and `roomName` properties in json format"))
            session.sendSerialized(message)
            throw t
        }
        val room = getOrCreateRoom(connectionRequest.roomName)
        val gamer = Gamer(session, connectionRequest.playerName)
        room.connectNewGamer(gamer)
        if (room.isEmpty) {
            rooms -= room
            logger.info("room ${room.roomName} closed")
        }
    }

    suspend fun onNewConnectionToDefaultRoom(session: WebSocketServerSession) {
        val room = getOrCreateRoom(DEFAULT_ROOM_ID)
        val gamer = Gamer(session, "anonymous")
        room.connectNewGamer(gamer)
        if (room.isEmpty) {
            rooms -= room
            logger.info("room ${room.roomName} closed")
        }
    }

    private fun getOrCreateRoom(name: String): GameRoom {
        return synchronized(rooms) {
            rooms.firstOrNull {
                it.roomName == name
            } ?: GameRoom(name, logger).also {
                rooms += it
            }
        }
    }

    companion object {
        private const val DEFAULT_ROOM_ID = "default"
    }
}