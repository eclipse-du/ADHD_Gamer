package com.piepie.brainhouse.game.treasurehunt

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

data class GridCell(val row: Int, val col: Int) {
    fun chebyshevDistanceTo(other: GridCell): Int {
        return max(abs(row - other.row), abs(col - other.col))
    }
}

enum class TreasureHuntGameState {
    IDLE, PLAYING, FINISHED
}

sealed class TreasureClickResult {
    data object Correct : TreasureClickResult()
    data object TrialComplete : TreasureClickResult()
    data object Wrong : TreasureClickResult()
    data object AlreadyFound : TreasureClickResult()
    data object Ignored : TreasureClickResult()
}

class TreasureHuntEngine(private val random: Random = Random.Default) {
    private val _gameState = MutableStateFlow(TreasureHuntGameState.IDLE)
    val gameState = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _correctClicks = MutableStateFlow(0)
    val correctClicks = _correctClicks.asStateFlow()

    private val _wrongClicks = MutableStateFlow(0)
    val wrongClicks = _wrongClicks.asStateFlow()

    private val _currentGroupSize = MutableStateFlow(1)
    val currentGroupSize = _currentGroupSize.asStateFlow()

    private val _currentMaxCells = MutableStateFlow(5)
    val currentMaxCells = _currentMaxCells.asStateFlow()

    private val _currentDistance = MutableStateFlow(2)
    val currentDistance = _currentDistance.asStateFlow()

    private val _completedMaxGroupsInDifficulty = MutableStateFlow(0)
    val completedMaxGroupsInDifficulty = _completedMaxGroupsInDifficulty.asStateFlow()

    private val _targetCells = MutableStateFlow<List<GridCell>>(emptyList())
    val targetCells = _targetCells.asStateFlow()

    private val _foundCells = MutableStateFlow<Set<GridCell>>(emptySet())
    val foundCells = _foundCells.asStateFlow()

    private val _previewCells = MutableStateFlow<Set<GridCell>>(emptySet())
    val previewCells = _previewCells.asStateFlow()

    private val _cueCell = MutableStateFlow<GridCell?>(null)
    val cueCell = _cueCell.asStateFlow()

    private val _cueVisible = MutableStateFlow(false)
    val cueVisible = _cueVisible.asStateFlow()

    private val _timeLeftSec = MutableStateFlow(300)
    val timeLeftSec = _timeLeftSec.asStateFlow()

    private val _trialTimeLeftSec = MutableStateFlow(15)
    val trialTimeLeftSec = _trialTimeLeftSec.asStateFlow()

    private var config = TreasureHuntConfig(maxCells = 5, distance = 2)
    private var recentCompletedCells: Set<GridCell> = emptySet()

    fun start(config: TreasureHuntConfig) {
        this.config = config
        _score.value = 0
        _correctClicks.value = 0
        _wrongClicks.value = 0
        _currentGroupSize.value = 1
        _currentMaxCells.value = config.maxCells.coerceIn(1, config.gridSize * config.gridSize)
        _currentDistance.value = config.distance.coerceIn(1, config.gridSize - 1)
        _completedMaxGroupsInDifficulty.value = 0
        _timeLeftSec.value = config.sessionDurationSec
        recentCompletedCells = emptySet()
        _gameState.value = TreasureHuntGameState.PLAYING
        beginNextTrial()
    }

    fun hideCue() {
        if (_gameState.value != TreasureHuntGameState.PLAYING) return
        _cueVisible.value = false
        _previewCells.value = emptySet()
    }

    fun tickSessionSecond() {
        if (_gameState.value != TreasureHuntGameState.PLAYING) return
        val next = (_timeLeftSec.value - 1).coerceAtLeast(0)
        _timeLeftSec.value = next
        if (next == 0) {
            finishSession()
        }
    }

    fun tickTrialSecond() {
        if (_gameState.value != TreasureHuntGameState.PLAYING || _cueVisible.value) return
        val next = (_trialTimeLeftSec.value - 1).coerceAtLeast(0)
        _trialTimeLeftSec.value = next
        if (next == 0) {
            onTrialTimeout()
        }
    }

    fun onCellClicked(cell: GridCell): TreasureClickResult {
        if (_gameState.value != TreasureHuntGameState.PLAYING || _cueVisible.value) {
            return TreasureClickResult.Ignored
        }

        if (cell in _foundCells.value) {
            return TreasureClickResult.AlreadyFound
        }

        if (cell !in _targetCells.value) {
            registerFailure()
            return TreasureClickResult.Wrong
        }

        _foundCells.value = _foundCells.value + cell
        _correctClicks.value += 1
        _score.value += 50

        return if (_foundCells.value.containsAll(_targetCells.value)) {
            completeTrial()
            TreasureClickResult.TrialComplete
        } else {
            TreasureClickResult.Correct
        }
    }

    fun onTrialTimeout() {
        if (_gameState.value != TreasureHuntGameState.PLAYING) return
        registerFailure()
    }

    fun finishSession() {
        _gameState.value = TreasureHuntGameState.FINISHED
        _cueVisible.value = false
        _previewCells.value = emptySet()
    }

    fun currentLevelNumber(): Int {
        return TreasureHuntLevels.levelNumber(_currentMaxCells.value, _currentDistance.value)
    }

    private fun completeTrial() {
        if (_currentGroupSize.value >= _currentMaxCells.value) {
            recentCompletedCells = _targetCells.value.toSet()
            _completedMaxGroupsInDifficulty.value += 1

            if (_completedMaxGroupsInDifficulty.value >= 2) {
                advanceDifficulty()
            }

            _currentGroupSize.value = 1
            beginNextTrial()
        } else {
            _currentGroupSize.value += 1
            beginNextTrial()
        }
    }

    private fun registerFailure() {
        _wrongClicks.value += 1
        _score.value -= 50
        recentCompletedCells = _targetCells.value.toSet()
        _completedMaxGroupsInDifficulty.value = 0
        _currentGroupSize.value = 1
        beginNextTrial()
    }

    private fun advanceDifficulty() {
        val (nextMaxCells, nextDistance) = TreasureHuntLevels.nextDifficulty(
            maxCells = _currentMaxCells.value,
            distance = _currentDistance.value
        )
        _currentMaxCells.value = nextMaxCells.coerceAtMost(config.gridSize * config.gridSize)
        _currentDistance.value = nextDistance.coerceIn(1, config.gridSize - 1)
        _completedMaxGroupsInDifficulty.value = 0
    }

    private fun beginNextTrial() {
        val oldTargets = _targetCells.value
        val retained = if (_currentGroupSize.value <= 1 || oldTargets.isEmpty()) {
            emptyList()
        } else {
            oldTargets.take(_currentGroupSize.value - 1)
        }
        val nextTargets = if (retained.isEmpty()) {
            listOf(pickNextCell(existing = emptyList(), recent = recentCompletedCells))
        } else {
            retained + pickNextCell(existing = retained, recent = recentCompletedCells)
        }

        _targetCells.value = nextTargets
        _foundCells.value = emptySet()
        _previewCells.value = retained.toSet()
        _cueCell.value = nextTargets.lastOrNull()
        _cueVisible.value = true
        _trialTimeLeftSec.value = config.trialDurationSec
    }

    private fun pickNextCell(existing: List<GridCell>, recent: Set<GridCell>): GridCell {
        return pickNextCellForTest(existing, recent, _currentDistance.value)
    }

    fun beginTrialForTest(cells: List<GridCell>) {
        _targetCells.value = cells
        _foundCells.value = emptySet()
        _previewCells.value = emptySet()
        _currentGroupSize.value = cells.size.coerceAtLeast(1)
        _cueCell.value = cells.lastOrNull()
        _cueVisible.value = false
        _trialTimeLeftSec.value = config.trialDurationSec
    }

    fun pickNextCellForTest(
        existing: List<GridCell>,
        recent: Set<GridCell>,
        distance: Int
    ): GridCell {
        val allCells = (0 until config.gridSize).flatMap { row ->
            (0 until config.gridSize).map { col -> GridCell(row, col) }
        }
        val blocked = existing.toSet() + recent

        if (existing.isEmpty()) {
            return (allCells - blocked).randomOrNull(random)
                ?: (allCells - existing.toSet()).random(random)
        }

        val center = existing.centerCell()
        val nearCandidates = allCells.filter { cell ->
            cell !in blocked && cell.chebyshevDistanceTo(center) in 1..distance
        }
        if (nearCandidates.isNotEmpty()) return nearCandidates.random(random)

        val outsideCandidates = allCells.filter { cell ->
            cell !in blocked && cell.chebyshevDistanceTo(center) > distance
        }
        if (outsideCandidates.isNotEmpty()) return outsideCandidates.random(random)

        return (allCells - existing.toSet()).random(random)
    }

    private fun List<GridCell>.centerCell(): GridCell {
        val row = map { it.row }.average().roundToInt().coerceIn(0, config.gridSize - 1)
        val col = map { it.col }.average().roundToInt().coerceIn(0, config.gridSize - 1)
        return GridCell(row, col)
    }
}
