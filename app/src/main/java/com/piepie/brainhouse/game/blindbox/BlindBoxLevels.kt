package com.piepie.brainhouse.game.blindbox

data class BlindBoxLevelConfig(
    val levelId: Int,
    val boxCount: Int,
    val memorizeTimeSec: Int
)

object BlindBoxLevels {
    val levels = listOf(
        BlindBoxLevelConfig(1, 3, 5),
        BlindBoxLevelConfig(2, 4, 6),
        BlindBoxLevelConfig(3, 5, 8),
        BlindBoxLevelConfig(4, 6, 10),
        BlindBoxLevelConfig(5, 7, 12),
        BlindBoxLevelConfig(6, 8, 15),
        BlindBoxLevelConfig(7, 9, 18),
        BlindBoxLevelConfig(8, 10, 20)
    )

    fun getLevel(id: Int): BlindBoxLevelConfig {
        return levels.find { it.levelId == id } ?: levels.first()
    }
}
