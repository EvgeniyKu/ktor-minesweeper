package com.example.models.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RoomInfoRequest(
    @SerialName("nameRoom")
    val nameRoom: String
)