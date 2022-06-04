package com.example.models.request

enum class Action(val apiKey: String) {
    StartGame("startGame"),
    OpenCell("openCell"),
    SetFlag("setFlag")
}