package com.example.models.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class Player(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("color")
    val color: String
)
