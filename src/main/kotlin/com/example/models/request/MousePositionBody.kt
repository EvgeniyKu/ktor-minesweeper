package com.example.models.request

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class MousePositionBody(
    @SerialName("x")
    val x: Int,
    @SerialName("y")
    val y: Int
)
