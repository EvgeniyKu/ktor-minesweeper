package com.example

import io.ktor.server.websocket.*
import java.util.concurrent.atomic.AtomicLong

class Gamer private constructor(
    val connectionId: Long,
    val session: WebSocketServerSession,
    val name: String,
): WebSocketServerSession by session {

    override fun equals(other: Any?): Boolean {
        if (other !is Gamer) return false
        return other.connectionId == connectionId
    }

    override fun hashCode(): Int {
        return connectionId.hashCode()
    }

    override fun toString(): String {
        return "$name($connectionId)"
    }

    companion object {
        private var lastId = AtomicLong(0)

        operator fun invoke(session: WebSocketServerSession, name: String): Gamer {
            return Gamer(
                connectionId = lastId.incrementAndGet(),
                session = session,
                name = name
            )
        }
    }
}