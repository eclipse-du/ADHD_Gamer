package com.piepie.brainhouse.game.treasurehunt

data class TreasureHuntConfig(
    val maxCells: Int,
    val distance: Int,
    val sessionDurationSec: Int = 300,
    val trialDurationSec: Int = 15,
    val gridSize: Int = 6,
    val previewDurationMs: Long = 900L
)

object TreasureHuntLevels {
    private const val START_MAX_CELLS = 5
    private const val START_DISTANCE = 2
    private const val MAX_DISTANCE = 3

    fun getLevel(level: Int): TreasureHuntConfig {
        val safeLevel = level.coerceAtLeast(1)
        val distanceSpan = MAX_DISTANCE - START_DISTANCE + 1
        val maxCells = START_MAX_CELLS + (safeLevel - 1) / distanceSpan
        val distance = START_DISTANCE + (safeLevel - 1) % distanceSpan
        return TreasureHuntConfig(maxCells = maxCells, distance = distance)
    }

    fun levelNumber(maxCells: Int, distance: Int): Int {
        val distanceSpan = MAX_DISTANCE - START_DISTANCE + 1
        val safeMaxCells = maxCells.coerceAtLeast(START_MAX_CELLS)
        val safeDistance = distance.coerceIn(START_DISTANCE, MAX_DISTANCE)
        return (safeMaxCells - START_MAX_CELLS) * distanceSpan + (safeDistance - START_DISTANCE) + 1
    }

    fun nextDifficulty(maxCells: Int, distance: Int): Pair<Int, Int> {
        return if (distance < MAX_DISTANCE) {
            maxCells to distance + 1
        } else {
            maxCells + 1 to START_DISTANCE
        }
    }
}
