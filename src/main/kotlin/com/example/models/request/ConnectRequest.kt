package com.example.models.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectRequest(
    @SerialName("playerName")
    val playerName: String,
    @SerialName("roomName")
    val roomName: String
)
