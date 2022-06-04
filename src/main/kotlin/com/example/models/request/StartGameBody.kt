package com.example.models.request

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class StartGameBody(
    @SerialName("difficulty")
    val difficulty: String // should be: easy, medium, hard
)
