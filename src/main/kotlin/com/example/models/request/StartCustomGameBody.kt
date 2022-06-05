package com.example.models.request

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class StartCustomGameBody(
    @SerialName("rows")
    val rows: Int,
    @SerialName("columns")
    val columns: Int,
    @SerialName("bombs")
    val bombs: Int,
)
