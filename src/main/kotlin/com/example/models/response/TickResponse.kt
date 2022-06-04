package com.example.models.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
class TickResponse(
    @SerialName("seconds")
    val seconds: Int
)