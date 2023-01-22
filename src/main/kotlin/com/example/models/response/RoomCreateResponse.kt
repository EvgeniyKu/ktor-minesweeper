package com.example.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomCreateResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null
)
