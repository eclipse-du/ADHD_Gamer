package com.piepie.brainhouse.game.ultraman

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class UltramanGameState {
    IDLE, COUNTDOWN, PLAYING, PAUSED, VICTORY, GAME_OVER
}

enum class UltramanLevel(val id: Int, val playerName: String, val enemyName: String, val actionName: String, val durationSec: Int) {
    LEVEL_1(1, "Belial", "Zero", "Leg Clip", 20),
    LEVEL_2(2, "Zero", "Belial", "Shield Block", 30),
    LEVEL_3(3, "Ace", "Belial", "Jump Dodge", 40)
}

data class Beam(
    val id: Long,
    val startTime: Long,
    val speed: Long, // Ms to travel
    val isFake: Boolean = false // For feints
)

class UltramanGameEngine(private val scope: CoroutineScope) {

    private val _gameState = MutableStateFlow(UltramanGameState.IDLE)
    val gameState: StateFlow<UltramanGameState> = _gameState.asStateFlow()

    private val _timeLeft = MutableStateFlow(0)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _score = MutableStateFlow(0) // For endless
    val score: StateFlow<Int> = _score.asStateFlow()

    // Active beams on screen
    private val _activeBeams = MutableStateFlow<List<Beam>>(emptyList())
    val activeBeams: StateFlow<List<Beam>> = _activeBeams.asStateFlow()

    // Player action state (visual feedback)
    // Player action state (visual feedback)
    private val _isActing = MutableStateFlow(false)
    val isActing: StateFlow<Boolean> = _isActing.asStateFlow()
    
    private val _countdown = MutableStateFlow(3)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private var currentLevel = UltramanLevel.LEVEL_1
    private var isEndless = false
    private var gameJob: Job? = null
    private var beamJob: Job? = null

    // Configuration
    private val hitWindowMs = 300L // Reduced window slightly for faster speed (1.5s total travel -> 300ms is 20%)
    private val beamTravelTimeMs = 1500L // Faster beams (1.5 seconds)

    fun startGame(level: UltramanLevel, endless: Boolean) {
        currentLevel = level
        isEndless = endless
        startCountdown()
    }
    
    fun retryLevel() {
        startCountdown()
    }

    private fun startCountdown() {
        gameJob?.cancel()
        _gameState.value = UltramanGameState.COUNTDOWN
        _activeBeams.value = emptyList() // Clear beams
        _score.value = 0
        
        gameJob = scope.launch {
            _countdown.value = 3
            delay(1000)
            _countdown.value = 2
            delay(1000)
            _countdown.value = 1
            delay(1000)
            
            // Start Game
            _gameState.value = UltramanGameState.PLAYING
            _timeLeft.value = if (isEndless) 0 else currentLevel.durationSec
            startLoopLogic()
        }
    }

    private suspend fun startLoopLogic() {
        val startTime = System.currentTimeMillis()
        var nextBeamTime = startTime + 1000L

        while (_gameState.value == UltramanGameState.PLAYING) {
            val now = System.currentTimeMillis()

            // Timer Logic
            if (!isEndless) {
                val paramsDuration = currentLevel.durationSec
                val elapsed = (now - startTime) / 1000
                val remaining = paramsDuration - elapsed
                _timeLeft.value = remaining.toInt()
                if (remaining <= 0) {
                    endGame(true)
                    break
                }
            } else {
                _timeLeft.value = ((now - startTime) / 1000).toInt()
            }

            // Spawner Logic
            if (now >= nextBeamTime) {
                spawnBeam()
                // Random interval 1.5s - 3s, faster in endless/later levels
                val difficultyFactor = if (isEndless) (_score.value / 10).coerceAtMost(5) else 0
                val minDelay = (1500 - difficultyFactor * 100).coerceAtLeast(800)
                val maxDelay = (3000 - difficultyFactor * 200).coerceAtLeast(1200)
                nextBeamTime = now + Random.nextLong(minDelay.toLong(), maxDelay.toLong())
            }

            // Collision Check (Missed beams)
            checkMisses(now)

            delay(50) // Tick
        }
    }

    private fun spawnBeam() {
        // 20% chance of fake (feint) if difficulty allows
        val isFake = Random.nextFloat() < 0.2f
        val newBeam = Beam(System.nanoTime(), System.currentTimeMillis(), beamTravelTimeMs, isFake)
        // Only real beams are added to tracking list effectively? 
        // No, visuals need fakes too. Logic handles them differently.
        val current = _activeBeams.value.toMutableList()
        current.add(newBeam)
        _activeBeams.value = current
    }

    private fun checkMisses(now: Long) {
        val current = _activeBeams.value.toMutableList()
        val iterator = current.iterator()
        while (iterator.hasNext()) {
            val beam = iterator.next()
            val impactTime = beam.startTime + beam.speed
            // Too Late Check
            if (now > impactTime + hitWindowMs) {
                iterator.remove()
                if (!beam.isFake) {
                    endGame(false)
                    return
                }
            }
        }
        _activeBeams.value = current
    }

    fun onPlayerAction() {
        if (_gameState.value != UltramanGameState.PLAYING) return

        scope.launch {
            _isActing.value = true
            delay(300)
            _isActing.value = false
        }

        val now = System.currentTimeMillis()
        val current = _activeBeams.value.toMutableList()
        val iterator = current.iterator()
        var hitReal = false
        var hitFake = false

        while (iterator.hasNext()) {
            val beam = iterator.next()
            val impactTime = beam.startTime + beam.speed
            
            // Check Hit Window
            if (now in (impactTime - hitWindowMs)..(impactTime + hitWindowMs)) {
                iterator.remove()
                if (beam.isFake) {
                    hitFake = true
                } else {
                    hitReal = true
                    _score.value += 1
                }
            }
        }
        
        if (hitReal) {
            _activeBeams.value = current
            // Success sound
        } else {
            // No real beam hit -> Too Early or False Alarm -> Game Over
            // (Unless we hit a fake... hitting a fake might be allowed or penalty. Let's assume hitting fake is NOT Game Over but no score)
            if (!hitFake) {
                 endGame(false)
            } else {
                 _activeBeams.value = current // Removed fake
            }
        }
    }

    private fun endGame(victory: Boolean) {
        _gameState.value = if (victory) UltramanGameState.VICTORY else UltramanGameState.GAME_OVER
        gameJob?.cancel()
    }
    
    fun reset() {
        _gameState.value = UltramanGameState.IDLE
        gameJob?.cancel()
    }
}
