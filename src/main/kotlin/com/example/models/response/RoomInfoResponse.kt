package com.example.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomInfoResponse(
    @SerialName("isExist")
    val isExist: Boolean,
    @SerialName("body")
    val body: Body?
) {

    @Serializable
    data class Body(
        @SerialName("roomName")
        val roomName: String,
        @SerialName("countUsers")
        val countUsers: Int
    )
}
