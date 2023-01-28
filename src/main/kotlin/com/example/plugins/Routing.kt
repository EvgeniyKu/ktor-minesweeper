package com.example.plugins

import com.example.domain.GameSettings
import com.example.domain.RoomsController
import com.example.models.request.ConnectRequest
import com.example.models.request.RoomCreateRequest
import com.example.models.request.RoomInfoRequest
import com.example.models.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import java.time.Duration
import kotlin.math.min

fun Application.configureRouting() {
    routing {
        val roomsController = RoomsController(application.environment.log)

        get("/") {
            call.respondRedirect("/index.html")
        }
        static("/") {
            resources("files")
        }

        post("/roominfo") {
            val request = call.receive<RoomInfoRequest>()
            val room = roomsController.getRoom(request.nameRoom)
            if (room == null) {
                call.respond(RoomInfoResponse(false, null))
            } else {
                call.respond(RoomInfoResponse(true, RoomInfoResponse.Body(room.roomName, room.gamersCount)))
            }
        }

        post("/createroom") {
            val request = call.receive<RoomCreateRequest>()
            try {
                if (roomsController.getRoom(request.roomName) != null) {
                    error("room ${request.roomName} already exist")
                }

                if (request.difficulty == null && request.settings == null) {
                    error("`difficulty` or `settings` should be specified")
                }

                if (request.difficulty != null && request.settings != null) {
                    error("use only `difficulty` or `settings`")
                }

                if (request.difficulty != null) {
                    val difficulty = when(request.difficulty) {
                        "easy" -> GameSettings.EASY
                        "medium" -> GameSettings.MEDIUM
                        "hard" -> GameSettings.EXPERT
                        else -> error("unknown difficulty ${request.difficulty}. Difficulty should be easy|medium|hard")
                    }
                    roomsController.createRoom(request.roomName, difficulty)
                }

                if (request.settings != null) {
                    val settings = GameSettings(
                        rows = request.settings.rows,
                        columns = request.settings.columns,
                        mines = request.settings.bombs
                    )
                    roomsController.createRoom(request.roomName, settings)
                }

                call.respond(RoomCreateResponse(true))
            } catch (t: Throwable) {
                call.respond(RoomCreateResponse(false, t.message))
            }
        }

        webSocket("/minesweeper-socket") {
            handleExceptions {
                val playerName = call.request.queryParameters["playerName"]
                    ?: error("argument playerName not found")
                val roomName = call.request.queryParameters["roomName"]
                    ?: error("argument roomName not found")
                val request = ConnectRequest(
                    playerName = playerName,
                    roomName = roomName
                )
                roomsController.connect(this, request)
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
