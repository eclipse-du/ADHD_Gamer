package com.piepie.brainhouse.game.spotdifference

import kotlin.random.Random

class SpotDifferenceDeck(
    private val levels: List<SpotDifferenceLevel>,
    seed: Int? = null
) {
    private val random = seed?.let { Random(it) } ?: Random.Default
    private var queue: List<SpotDifferenceLevel> = emptyList()
    private var lastLevelId: Int? = null

    fun nextLevel(): SpotDifferenceLevel {
        if (levels.isEmpty()) error("Spot difference levels cannot be empty")

        if (queue.isEmpty()) {
            queue = levels.shuffled(random)
            val last = lastLevelId
            if (last != null && queue.size > 1 && queue.first().id == last) {
                queue = queue.drop(1) + queue.first()
            }
        }

        val next = queue.first()
        queue = queue.drop(1)
        lastLevelId = next.id
        return next
    }
}
