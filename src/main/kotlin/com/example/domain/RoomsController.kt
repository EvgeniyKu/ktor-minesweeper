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

    suspend fun onNewConnection(session: WebSocketServerSession) {
        val connectionRequest = takeRequestFromQuery(session.call)
            ?: try {
                session.receiveDeserialized()
            } catch (t: Throwable) {
                error("Connect failed. First message must contains `playerName` and `roomName` properties in json format. Or pass this properties in query parameters ")
            }

        connect(session, connectionRequest)
    }

    suspend fun onNewConnectionToDefaultRoom(session: WebSocketServerSession) {
        val connectionRequest = takeRequestFromQuery(session.call)
            ?: ConnectRequest(
                playerName = "anonymous",
                roomName = DEFAULT_ROOM_ID
            )

        connect(session, connectionRequest)
    }

    private suspend fun connect(session: WebSocketServerSession, request: ConnectRequest) {
        val room = getOrCreateRoom(request.roomName)
        val gamer = Gamer(session, request.playerName)
        room.connectNewGamer(gamer)
        if (room.isEmpty) {
            rooms -= room
            logger.info("room ${room.roomName} closed")
        }
    }

    private fun takeRequestFromQuery(call: ApplicationCall): ConnectRequest? {
        val playerName = call.request.queryParameters["playerName"]
            ?: return null
        val roomName = call.request.queryParameters["roomName"]
            ?: return null
        return ConnectRequest(
            playerName = playerName,
            roomName = roomName
        )
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