package com.piepie.brainhouse.game.treasurehunt

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TreasureHuntEngineTest {
    @Test
    fun clickingAllTargetsInAnyOrderCompletesTrialAndScoresEachHit() {
        val engine = TreasureHuntEngine(random = Random(1))

        engine.start(TreasureHuntConfig(maxCells = 3, distance = 2, sessionDurationSec = 300))
        engine.beginTrialForTest(listOf(GridCell(1, 1), GridCell(1, 2)))

        engine.onCellClicked(GridCell(1, 2))
        val result = engine.onCellClicked(GridCell(1, 1))

        assertEquals(100, engine.score.value)
        assertEquals(2, engine.correctClicks.value)
        assertTrue(result is TreasureClickResult.TrialComplete)
        assertEquals(3, engine.currentGroupSize.value)
    }

    @Test
    fun wrongClickPenalizesAndResetsGroupSizeToOne() {
        val engine = TreasureHuntEngine(random = Random(2))

        engine.start(TreasureHuntConfig(maxCells = 5, distance = 2, sessionDurationSec = 300))
        engine.beginTrialForTest(listOf(GridCell(3, 3), GridCell(3, 4), GridCell(3, 5)))

        val result = engine.onCellClicked(GridCell(0, 0))

        assertEquals(-50, engine.score.value)
        assertEquals(1, engine.currentGroupSize.value)
        assertEquals(1, engine.wrongClicks.value)
        assertTrue(result is TreasureClickResult.Wrong)
    }

    @Test
    fun previousBlueCellsAreOnlyVisibleDuringTheCuePhase() {
        val engine = TreasureHuntEngine(random = Random(10))

        engine.start(TreasureHuntConfig(maxCells = 3, distance = 2, sessionDurationSec = 300))
        engine.beginTrialForTest(listOf(GridCell(2, 2)))

        engine.onCellClicked(GridCell(2, 2))

        assertTrue(engine.cueVisible.value)
        assertTrue(GridCell(2, 2) in engine.previewCells.value)
        assertTrue(engine.foundCells.value.isEmpty())
        assertTrue(GridCell(2, 2) in engine.targetCells.value)
        assertEquals(2, engine.targetCells.value.size)

        engine.hideCue()

        assertTrue(engine.previewCells.value.isEmpty())
        assertTrue(engine.foundCells.value.isEmpty())
    }

    @Test
    fun wrongClickAvoidsTheFailedTrialCellsWhenStartingOver() {
        val engine = TreasureHuntEngine(random = Random(9))

        engine.start(TreasureHuntConfig(maxCells = 3, distance = 1, gridSize = 2))
        engine.beginTrialForTest(listOf(GridCell(0, 0), GridCell(0, 1), GridCell(1, 0)))

        engine.onCellClicked(GridCell(1, 1))

        assertEquals(listOf(GridCell(1, 1)), engine.targetCells.value)
    }

    @Test
    fun failureDoesNotLowerReachedDifficulty() {
        val engine = TreasureHuntEngine(random = Random(8))

        engine.start(TreasureHuntConfig(maxCells = 5, distance = 3, sessionDurationSec = 300))
        engine.beginTrialForTest(listOf(GridCell(3, 3), GridCell(3, 4)))

        engine.onCellClicked(GridCell(0, 0))

        assertEquals(5, engine.currentMaxCells.value)
        assertEquals(3, engine.currentDistance.value)
        assertEquals(1, engine.currentGroupSize.value)
    }

    @Test
    fun timeoutCountsAsWrongAndResetsGroupSizeToOne() {
        val engine = TreasureHuntEngine(random = Random(3))

        engine.start(TreasureHuntConfig(maxCells = 5, distance = 2, sessionDurationSec = 300))
        engine.beginTrialForTest(listOf(GridCell(4, 4), GridCell(4, 5)))

        engine.onTrialTimeout()

        assertEquals(-50, engine.score.value)
        assertEquals(1, engine.currentGroupSize.value)
        assertEquals(1, engine.wrongClicks.value)
    }

    @Test
    fun generatedNewCellDoesNotRepeatCurrentTargetsOrRecentCompletedCells() {
        val engine = TreasureHuntEngine(random = Random(4))
        val existing = listOf(GridCell(4, 4), GridCell(4, 5), GridCell(5, 4))
        val recent = setOf(GridCell(5, 5), GridCell(5, 6))

        repeat(20) {
            val cell = engine.pickNextCellForTest(existing, recent, distance = 2)

            assertFalse(cell in existing)
            assertFalse(cell in recent)
        }
    }

    @Test
    fun generatedNewCellUsesDistanceLimitWhenCandidatesExist() {
        val engine = TreasureHuntEngine(random = Random(5))
        val existing = listOf(GridCell(4, 4))

        repeat(20) {
            val cell = engine.pickNextCellForTest(existing, emptySet(), distance = 2)

            assertTrue(cell.chebyshevDistanceTo(existing.first()) in 1..2)
        }
    }

    @Test
    fun generatedNewCellUsesCenterDistanceWhenThereAreMultipleExistingCells() {
        val engine = TreasureHuntEngine(random = Random(11))
        val existing = listOf(GridCell(0, 0), GridCell(0, 4), GridCell(4, 0), GridCell(4, 4))
        val center = GridCell(2, 2)

        repeat(20) {
            val cell = engine.pickNextCellForTest(existing, emptySet(), distance = 2)

            assertTrue(cell.chebyshevDistanceTo(center) in 1..2)
        }
    }

    @Test
    fun defaultGridSizeIsSixBySix() {
        assertEquals(6, TreasureHuntConfig(maxCells = 5, distance = 2).gridSize)
    }

    @Test
    fun defaultPreviewDurationIsHalfOfPreviousCueTime() {
        assertEquals(900L, TreasureHuntConfig(maxCells = 5, distance = 2).previewDurationMs)
    }

    @Test
    fun levelConfigStartsAtFiveCellsDistanceTwoAndRaisesDistanceBeforeMaxCells() {
        assertEquals(TreasureHuntConfig(maxCells = 5, distance = 2), TreasureHuntLevels.getLevel(1))
        assertEquals(TreasureHuntConfig(maxCells = 5, distance = 3), TreasureHuntLevels.getLevel(2))
        assertEquals(TreasureHuntConfig(maxCells = 6, distance = 2), TreasureHuntLevels.getLevel(3))
        assertNotEquals(TreasureHuntLevels.getLevel(1), TreasureHuntLevels.getLevel(4))
    }

    @Test
    fun twoCompletedMaxGroupsAdvanceDifficultyWithoutEndingSession() {
        val engine = TreasureHuntEngine(random = Random(6))

        engine.start(TreasureHuntConfig(maxCells = 2, distance = 2, sessionDurationSec = 300))

        engine.beginTrialForTest(listOf(GridCell(1, 1), GridCell(1, 2)))
        engine.onCellClicked(GridCell(1, 1))
        engine.onCellClicked(GridCell(1, 2))
        assertEquals(2, engine.currentDistance.value)

        engine.beginTrialForTest(listOf(GridCell(2, 1), GridCell(2, 2)))
        engine.onCellClicked(GridCell(2, 1))
        engine.onCellClicked(GridCell(2, 2))

        assertEquals(TreasureHuntGameState.PLAYING, engine.gameState.value)
        assertEquals(3, engine.currentDistance.value)
        assertEquals(2, engine.currentMaxCells.value)
        assertEquals(1, engine.currentGroupSize.value)
    }
}
