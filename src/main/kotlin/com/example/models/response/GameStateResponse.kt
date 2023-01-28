package com.example.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameStateResponse(
    @SerialName("roomName")
    val roomName: String,
    @SerialName("gameState")
    val gameState: String, // could be: not_started, running, win, lose
    @SerialName("score")
    val score: Int,
    @SerialName("seconds")
    val seconds: Int,
    @SerialName("board")
    val board: List<List<Cell>>,
    @SerialName("players")
    val players: List<PlayerInfo>,
    @SerialName("lastInteractedUserId")
    val lastInteractedUserId: Long?,
) {

    @Serializable
    data class Cell(
        @SerialName("row")
        val row: Int,
        @SerialName("column")
        val column: Int,
        @SerialName("isOpened")
        val isOpened: Boolean,
        @SerialName("isFlagged")
        val isFlagged: Boolean,
        @SerialName("countOfBombs")
        val countOfBombs: Int?, // 0 - is empty, 9 - is bomb. But if `isOpened` is false, `countOfBombs` is null
        @SerialName("interacted_player_id")
        val playerId: Long? // the player who last interacted with the cell
    )

    @Serializable
    data class PlayerInfo(
        @SerialName("playerId")
        val playerId: Long,
        @SerialName("openedCells")
        val openedCells: Int,
        @SerialName("toggledFlags")
        val toggledFlags: Int
    )
}
