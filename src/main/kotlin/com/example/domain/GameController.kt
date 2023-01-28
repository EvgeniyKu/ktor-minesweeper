package com.example.domain

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Closeable
import kotlin.random.Random

class GameController(private val options: GameSettings) {
    /** Number of rows in current board */
    val rows: Int
        get() = options.rows
    /** Number of columns in current board */
    val columns: Int
        get() = options.columns
    /** Number of bombs in current board */
    val bombs: Int
        get() = options.mines

    var gameState: GameState = GameState.NOT_STARTED
        private set

    /** True if current game has started, false if game is finished or until first cell is opened or flagged */
    val running: Boolean
        get() = gameState == GameState.RUNNING

    /** True if game is ended (win or lose) */
    private val finished: Boolean
        get() = gameState == GameState.WIN || gameState == GameState.LOSE

    /** Total number of flags set on cells, used for calculation of number of remaining bombs */
    var flagsSet: Int = 0
        private set
    /** Number of remaining cells */
    var cellsToOpen: Int = options.rows * options.columns - options.mines
        private set
    /** Game timer, increments every second while game is running */
    val seconds: Int
        get() {
            if (gameState == GameState.NOT_STARTED) return 0
            return (((finishTime ?: System.currentTimeMillis()) - startTime) / 1000L).toInt()
        }

    /** Global monotonic time, updated with [onTimeTick] */
    /** The time when user starts the game by opening or flagging any cell */
    private var startTime = 0L
    private var finishTime: Long? = null

    /** The game board of size (rows * columns) */
    val cells = Array(options.rows) { row ->
        Array(options.columns) { column ->
            Cell(row, column)
        }
    }

    private var isFirstOpenedCell = true

    var lastInteractedUserId: Long? = null
        private set

    init {
        // validate options
        check(options.rows * options.columns > bombs) {
            "too many bombs"
        }
        check(options.rows > 0 && options.columns > 0 && options.mines > 0) {
            "game options must be greater than 0"
        }
        // Put [options.mines] bombs on random positions
        for (i in 1..options.mines) {
            putBomb()
        }
    }

    /**
     * Get cell at given position, or null if any index is out of bounds
     */
    private fun cellAt(row: Int, column: Int) = cells.getOrNull(row)?.getOrNull(column)

    /**
     * Open given cell:
     * - If cell is opened or flagged, or game is finished, does nothing
     * - If cell contains bomb, opens it and stops the game (lose)
     * - If cell has no bombs around int, recursively opens cells around current
     *
     * When cell opens, decrements [cellsToOpen],
     * if it becomes zero, stops the game (win). First call starts the game.
     *
     * @param cell Cell to open, **must** belong to current game board
     */
    fun openCell(row: Int, column: Int, userId: Long?) {
        val cell = cellAt(row, column)
            ?: throw IllegalArgumentException("missing cell at row: $row and column $column")

        if (finished || cell.isOpened || cell.isFlagged) return
        if (!running) {
            startGame()
        }

        cell.isOpened = true
        cell.userId = userId
        lastInteractedUserId = userId
        if (cell.hasBomb) {
            if (isFirstOpenedCell) {
                ensureNotLoseAtFirstClick(cell)
            } else {
                lose()
                return
            }
        }
        isFirstOpenedCell = false

        cellsToOpen -= 1
        if (cellsToOpen == 0) {
            win()
            return
        }

        if (cell.bombsNear == 0) {
            neighborsOf(cell).forEach {
                openCell(it.row, it.column, null)
            }
        }
    }

    /**
     * Sets or drops flag on given [cell]. Flagged cell can not be opened until flag drop
     * If game is finished, or cell is opened, does nothing. First call starts the game.
     *
     * Setting flag increments [flagsSet], dropping - decrements
     *
     * @param cell Cell to toggle flag, **must** belong to current game board
     */
    fun toggleFlag(row: Int, column: Int, userId: Long?) {
        val cell = cellAt(row, column)
            ?: throw IllegalArgumentException("missing cell at row: $row and column $column")
        if (finished || cell.isOpened) return
        if (!running) {
            startGame()
        }

        cell.isFlagged = !cell.isFlagged
        cell.userId = userId
        lastInteractedUserId = userId
        if (cell.isFlagged) {
            flagsSet += 1
        } else {
            flagsSet -= 1
        }
    }

    fun getUsersSummary(): Map<Long, UserBoardSummary> {
        val openedCellsByUser = mutableMapOf<Long, Int>()
        val toggledCellsByUser = mutableMapOf<Long, Int>()

        cells.flatten().forEach { cell ->
            val userId = cell.userId
            if (userId != null) {
                if (cell.isOpened) {
                    val currentOpenedCount = openedCellsByUser[userId] ?: 0
                    openedCellsByUser[userId] = currentOpenedCount + 1
                } else if (cell.isFlagged) {
                    val currentToggledCount = toggledCellsByUser[userId] ?: 0
                    toggledCellsByUser[userId] = currentToggledCount + 1
                }
            }
        }
        val allUsers = openedCellsByUser.keys + toggledCellsByUser.keys
        return allUsers.associateWith { id ->
            UserBoardSummary(
                openedCellsCount = openedCellsByUser[id] ?: 0,
                toggledCellsCount = toggledCellsByUser[id] ?: 0
            )
        }
    }

    private fun putBomb() {
        var cell: Cell
        do {
            // This strategy can create infinite loop, but for simplicity we can assume
            // that mine count is small enough
            val random = Random.nextInt(options.rows * options.columns)
            cell = cells[random / columns][random % columns]
        } while (cell.hasBomb)

        cell.hasBomb = true
        neighborsOf(cell).forEach {
            it.bombsNear += 1
        }
    }

    private fun flagAllBombs() {
        cells.forEach { row ->
            row.forEach { cell ->
                if (!cell.isOpened) {
                    cell.isFlagged = true
                }
            }
        }
    }

    private fun openAllBombs() {
        cells.forEach { row ->
            row.forEach { cell ->
                if (cell.hasBomb && !cell.isFlagged) {
                    cell.isOpened = true
                }
            }
        }
    }

    private fun neighborsOf(cell: Cell): List<Cell> = neighborsOf(cell.row, cell.column)

    private fun neighborsOf(row: Int, column: Int): List<Cell> {
        val result = mutableListOf<Cell>()
        cellAt(row - 1, column - 1)?.let { result.add(it) }
        cellAt(row - 1, column)?.let { result.add(it) }
        cellAt(row - 1, column + 1)?.let { result.add(it) }
        cellAt(row, column - 1)?.let { result.add(it) }
        cellAt(row, column + 1)?.let { result.add(it) }
        cellAt(row + 1, column - 1)?.let { result.add(it) }
        cellAt(row + 1, column)?.let { result.add(it) }
        cellAt(row + 1, column + 1)?.let { result.add(it) }

        return result
    }

    private fun win() {
        endGame(true)
        flagAllBombs()
    }

    private fun lose() {
        endGame(false)
        openAllBombs()
    }

    private fun endGame(win: Boolean) {
        finishTime = System.currentTimeMillis()
        gameState = if (win) {
            GameState.WIN
        } else {
            GameState.LOSE
        }
    }

    private fun startGame() {
        if (!finished) {
            finishTime = null
            startTime = System.currentTimeMillis()
            gameState = GameState.RUNNING
        }
    }

    private fun ensureNotLoseAtFirstClick(firstCell: Cell) {
        putBomb()
        firstCell.hasBomb = false
        neighborsOf(firstCell).forEach {
            it.bombsNear -= 1
        }
    }

    override fun toString(): String {
        return buildString {
            for (row in cells) {
                for (cell in row) {
                    if (cell.hasBomb) {
                        append('*')
                    } else if (cell.isFlagged) {
                        append('!')
                    } else if (cell.bombsNear > 0) {
                        append(cell.bombsNear)
                    } else {
                        append(' ')
                    }
                }
                append('\n')
            }
            deleteAt(length - 1)
        }
    }
}

enum class GameState(val apiKey: String) {
    NOT_STARTED("not_started"),
    RUNNING("running"),
    WIN("win"),
    LOSE("lose")
}

data class GameSettings(val rows: Int, val columns: Int, val mines: Int) {
    companion object {
        val EASY = GameSettings(9, 9, 10)
        val MEDIUM = GameSettings(16, 16, 40)
        val EXPERT = GameSettings(16, 30, 99)
    }
}

class Cell(val row: Int, val column: Int) {
    var hasBomb = false
    var isOpened = false
    var isFlagged = false
    var bombsNear = 0
    var userId: Long? = null
}

class UserBoardSummary(
    val openedCellsCount: Int,
    val toggledCellsCount: Int
)