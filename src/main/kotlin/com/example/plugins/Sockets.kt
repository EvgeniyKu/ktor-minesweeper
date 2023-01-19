package com.example.plugins

import com.example.domain.RoomsController
import com.example.models.response.ErrorResponse
import com.example.models.response.Message
import com.example.models.response.MessageType
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import java.time.Duration

fun Application.configureSockets() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        val roomsController = RoomsController(application.environment.log)
        webSocket("/minesweeper-socket") {
            handleExceptions {
                roomsController.onNewConnectionToDefaultRoom(this)
            }
        }
        webSocket("/minesweeper-rooms-socket") {
            handleExceptions {
                roomsController.onNewConnection(this)
            }
        }
    }
}

private suspend inline fun WebSocketServerSession.handleExceptions(block: () -> Unit) {
    try {
        block()
    } catch (t: Throwable) {
        val message = Message(MessageType.Error.apiKey, ErrorResponse(t.message ?: t.toString()))
        sendSerialized(message)
        throw t
    }
}