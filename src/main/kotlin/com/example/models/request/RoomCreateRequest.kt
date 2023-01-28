package com.example.models.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomCreateRequest(
    @SerialName("nameRoom")
    val roomName: String,
    @SerialName("difficulty")
    val difficulty: String? = null,
    @SerialName("settings")
    val settings: Settings? = null
) {
    @Serializable
    data class Settings(
        @SerialName("rows")
        val rows: Int,
        @SerialName("columns")
        val columns: Int,
        @SerialName("bombs")
        val bombs: Int
    )
}
