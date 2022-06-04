package com.example.models.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ErrorResponse(
    @SerialName("errorMessage")
    val errorMessage: String
)