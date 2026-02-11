package com.piepie.brainhouse.game.schulte

data class SchulteLevelConfig(
    val levelId: Int,
    val gridSize: Int, // e.g., 2 for 2x2
    val timeLimitSec: Int, // Max time for challenge (optional)
    val star3TimeSec: Int, // Time to get 3 stars
    val star2TimeSec: Int  // Time to get 2 stars
)

object SchulteLevels {
    val levels = listOf(
        SchulteLevelConfig(1, 2, 60, 5, 10),   // 4 items
        SchulteLevelConfig(2, 2, 60, 3, 7),    // Faster
        SchulteLevelConfig(3, 3, 90, 10, 20),  // 9 items
        SchulteLevelConfig(4, 3, 90, 8, 15),
        SchulteLevelConfig(5, 4, 120, 25, 40), // 16 items
        SchulteLevelConfig(6, 4, 120, 20, 35),
        SchulteLevelConfig(7, 5, 180, 45, 70), // 25 items
        SchulteLevelConfig(8, 6, 240, 80, 120) // 36 items, Boss level
    )

    fun getLevel(id: Int): SchulteLevelConfig {
        return levels.find { it.levelId == id } ?: levels.first()
    }
}
