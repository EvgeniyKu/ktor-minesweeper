package com.example.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayersChangedResponse(
    @SerialName("players")
    val players: List<Player>
)
