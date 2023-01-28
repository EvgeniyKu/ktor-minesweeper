package com.example.mappers

import com.example.Gamer
import com.example.models.response.Player
import com.example.models.response.PlayersChangedResponse

fun Iterable<Gamer>.toResponse(): PlayersChangedResponse {
    return PlayersChangedResponse(
        players = map {
            Player(
                id = it.connectionId,
                name = it.name,
                color = it.color
            )
        }
    )
}