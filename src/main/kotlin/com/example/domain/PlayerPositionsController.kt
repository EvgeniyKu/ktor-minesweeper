package com.example.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PlayerPositionsController {

    private val _positionsFlow = MutableStateFlow<Map<Long, Position>>(emptyMap())
    val positions = _positionsFlow.sample(200)

    private val mutex = Mutex()

    suspend fun onNewPosition(playerId: Long, x: Int, y: Int) {
        mutex.withLock {
            val currentPositions = _positionsFlow.value.toMutableMap()
            currentPositions[playerId] = Position(x, y)
            _positionsFlow.emit(currentPositions)
        }
    }

    suspend fun removePlayer(playerId: Long) {
        mutex.withLock {
            _positionsFlow.value = _positionsFlow.value.filterKeys { it != playerId }
        }
    }

    data class Position(val x: Int, val y: Int)
}