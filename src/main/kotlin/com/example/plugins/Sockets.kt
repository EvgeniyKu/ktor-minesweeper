package com.example.plugins

import com.example.domain.RoomsController
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
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
            roomsController.onNewConnectionToDefaultRoom(this)
        }
        webSocket("/minesweeper-rooms-socket") {
            roomsController.onNewConnection(this)
        }
    }
}