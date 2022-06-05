package com.example.models.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class PlayersPosition(
    val positions: List<Position>
) {

    @kotlinx.serialization.Serializable
    data class Position(
        @SerialName("x")
        val x: Int,
        @SerialName("y")
        val y: Int,
        @SerialName("player")
        val player: Player
    )
}