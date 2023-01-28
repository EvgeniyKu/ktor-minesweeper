package com.example.models.request

enum class Action(val apiKey: String) {
    Restart("restart"),
    OpenCell("openCell"),
    SetFlag("setFlag"),
    MousePosition("mousePosition"),
}