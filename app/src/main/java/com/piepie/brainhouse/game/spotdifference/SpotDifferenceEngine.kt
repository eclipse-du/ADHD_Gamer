package com.piepie.brainhouse.game.spotdifference

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

sealed class SpotDifferenceClickResult {
    data class Correct(val spotIndex: Int) : SpotDifferenceClickResult()
    data object AlreadyFound : SpotDifferenceClickResult()
    data object Wrong : SpotDifferenceClickResult()
}

class SpotDifferenceEngine(private val level: SpotDifferenceLevel) {
    private val _foundSpotIds = MutableStateFlow<Set<Int>>(emptySet())
    val foundSpotIds = _foundSpotIds.asStateFlow()

    private val _isComplete = MutableStateFlow(false)
    val isComplete = _isComplete.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _wrongCount = MutableStateFlow(0)
    val wrongCount = _wrongCount.asStateFlow()

    private val _shouldSkipImage = MutableStateFlow(false)
    val shouldSkipImage = _shouldSkipImage.asStateFlow()

    fun onTap(x: Float, y: Float): SpotDifferenceClickResult {
        val spotIndex = level.spots.indexOfFirst { spot ->
            distance(x, y, spot.x, spot.y) <= spot.radius
        }

        if (spotIndex == -1) {
            _wrongCount.value += 1
            _score.value -= 50
            if (_wrongCount.value >= 3) {
                _shouldSkipImage.value = true
            }
            return SpotDifferenceClickResult.Wrong
        }
        if (spotIndex in _foundSpotIds.value) return SpotDifferenceClickResult.AlreadyFound

        _foundSpotIds.value = _foundSpotIds.value + spotIndex
        _score.value += 50
        _isComplete.value = _foundSpotIds.value.size >= level.requiredFinds
        return SpotDifferenceClickResult.Correct(spotIndex)
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }
}
