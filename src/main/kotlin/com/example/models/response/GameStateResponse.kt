package com.example.models.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
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
    val board: List<List<Cell>>
) {

    @kotlinx.serialization.Serializable
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
    )
}
