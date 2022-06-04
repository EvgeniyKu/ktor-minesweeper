package com.example.mappers

import com.example.domain.GameController
import com.example.models.response.GameStateResponse

fun GameController.toResponse(): GameStateResponse {
    return GameStateResponse(
        gameState = gameState.apiKey,
        score = flagsSet,
        board = cells.map { row ->
            row.map { cell ->
                GameStateResponse.Cell(
                    row = cell.row,
                    column = cell.column,
                    isOpened = cell.isOpened,
                    isFlagged = cell.isFlagged,
                    countOfBombs = if (cell.isOpened) {
                        if (cell.hasBomb) 9
                        else cell.bombsNear
                    } else {
                        null
                    }
                )
            }
        }
    )
}