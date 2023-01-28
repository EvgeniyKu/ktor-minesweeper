package com.example.models.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
class Message<T: Any>(
    @SerialName("messageType")
    val messageType: String,
    @SerialName("body")
    val body: T
)

enum class MessageType(val apiKey: String) {
    GameState("game_state"),
    Error("error"),
    Tick("tick"),
    PlayerPosition("player_positions"),
    PlayersChanged("players_changed")
}