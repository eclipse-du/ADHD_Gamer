package com.piepie.brainhouse.game.schulte

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SchulteGameState {
    IDLE, PLAYING, COMPLETED, FAILED
}

class SchulteEngine(private val scope: CoroutineScope) {
    private val _gameState = MutableStateFlow(SchulteGameState.IDLE)
    val gameState = _gameState.asStateFlow()

    private val _gridItems = MutableStateFlow<List<Int>>(emptyList())
    val gridItems = _gridItems.asStateFlow()

    private val _nextNumber = MutableStateFlow(1)
    val nextNumber = _nextNumber.asStateFlow()

    private val _timeElapsed = MutableStateFlow(0L)
    val timeElapsed = _timeElapsed.asStateFlow() // Milliseconds

    private var timerJob: Job? = null
    var currentConfig: SchulteLevelConfig? = null

    fun startLevel(config: SchulteLevelConfig) {
        currentConfig = config
        val size = config.gridSize * config.gridSize
        val numbers = (1..size).shuffled()
        _gridItems.value = numbers
        _nextNumber.value = 1
        _timeElapsed.value = 0L
        _gameState.value = SchulteGameState.PLAYING
        
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_gameState.value == SchulteGameState.PLAYING) {
                delay(100)
                _timeElapsed.value += 100
            }
        }
    }

    fun onNumberClicked(number: Int) {
        if (_gameState.value != SchulteGameState.PLAYING) return

        if (number == _nextNumber.value) {
            val maxNumber = currentConfig?.let { it.gridSize * it.gridSize } ?: 0
            if (number == maxNumber) {
                completeLevel()
            } else {
                _nextNumber.value += 1
            }
        } else {
            // Wrong click logic (Optional: penalty or shake)
            // For now, do nothing (Friendly mode)
        }
    }

    private fun completeLevel() {
        _gameState.value = SchulteGameState.COMPLETED
        timerJob?.cancel()
    }
    
    fun calculateStars(): Int {
        val seconds = _timeElapsed.value / 1000
        val config = currentConfig ?: return 0
        return when {
            seconds <= config.star3TimeSec -> 3
            seconds <= config.star2TimeSec -> 2
            else -> 1
        }
    }

    fun cleanup() {
        timerJob?.cancel()
    }
}
