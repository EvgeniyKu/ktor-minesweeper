package com.example.models.request

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CellPositionBody(
    @SerialName("row")
    val row: Int,
    @SerialName("column")
    val column: Int
)