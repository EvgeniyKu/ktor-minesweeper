package com.example.domain

import com.example.Gamer
import com.example.models.request.ConnectRequest
import com.example.models.response.ErrorResponse
import com.example.models.response.Message
import com.example.models.response.MessageType
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import org.slf4j.Logger
import java.util.*
import kotlin.collections.LinkedHashSet

class RoomsController(private val logger: Logger) {
    private val rooms = Collections.synchronizedSet<GameRoom>(LinkedHashSet())

    fun createRoom(name: String, settings: GameSettings) {
        val room = GameRoom(name, logger, settings)
        rooms += room
    }

    fun getRoom(name: String): GameRoom? {
        return rooms.firstOrNull { it.roomName == name }
    }

    suspend fun connect(session: WebSocketServerSession, request: ConnectRequest) {
        val room = getRoom(request.roomName)
            ?: error("room ${request.roomName} not found")
        val gamer = Gamer(session, request.playerName)
        room.connectNewGamer(gamer)
        if (room.isEmpty) {
            rooms -= room
            logger.info("room ${room.roomName} closed")
        }
    }
}