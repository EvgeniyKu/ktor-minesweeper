package com.example.plugins

import com.example.Connection
import com.example.domain.GameRoom
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import java.time.Duration

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        val gameController = GameRoom(application.environment.log)
        webSocket("/minesweeper-socket") {
            val thisConnection = Connection(this)
            gameController.onNewConnection(thisConnection)
        }
    }
}