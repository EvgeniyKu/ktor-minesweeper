package com.example

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.concurrent.atomic.AtomicLong

class Connection private constructor(
    private val connectionId: Long,
    val session: WebSocketServerSession
) {

    override fun equals(other: Any?): Boolean {
        if (other !is Connection) return false
        return other.connectionId == connectionId
    }

    override fun hashCode(): Int {
        return connectionId.hashCode()
    }

    companion object {
        private var lastId = AtomicLong(0)

        operator fun invoke(session: WebSocketServerSession): Connection {
            return Connection(
                connectionId = lastId.incrementAndGet(),
                session = session
            )
        }
    }
}