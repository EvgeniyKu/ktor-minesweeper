package com.example.models.request

enum class Action(val apiKey: String) {
    StartGame("startGame"),
    StartCustomGame("startCustomGame"),
    OpenCell("openCell"),
    SetFlag("setFlag"),
    MousePosition("mousePosition"),
}