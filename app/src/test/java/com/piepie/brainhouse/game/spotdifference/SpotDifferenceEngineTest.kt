package com.piepie.brainhouse.game.spotdifference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SpotDifferenceEngineTest {
    @Test
    fun requiredFindCountsAreFiveSixAndSeven() {
        assertEquals(5, SpotDifferenceLevels.getLevel(1).requiredFinds)
        assertEquals(6, SpotDifferenceLevels.getLevel(2).requiredFinds)
        assertEquals(7, SpotDifferenceLevels.getLevel(3).requiredFinds)
    }

    @Test
    fun levelsAcceptMoreSpotsThanAreNeededToPass() {
        SpotDifferenceLevels.allLevels().forEach { level ->
            assertTrue(level.spots.size >= 10)
            assertTrue(level.requiredFinds < level.spots.size)
        }
    }

    @Test
    fun usesEnoughImagePairsForRandomPlay() {
        assertTrue(SpotDifferenceLevels.allLevels().size >= 12)
    }

    @Test
    fun shuffledDeckDoesNotRepeatImmediatelyWhenAlternativesExist() {
        val deck = SpotDifferenceDeck(
            levels = SpotDifferenceLevels.allLevels().take(4),
            seed = 7
        )

        var previous = deck.nextLevel()
        repeat(12) {
            val next = deck.nextLevel()
            assertNotEquals(previous.id, next.id)
            previous = next
        }
    }

    @Test
    fun correctTapFindsOnlyOneSpotAndCompletesAfterAllSpots() {
        val level = SpotDifferenceLevel(
            id = 1,
            title = "test",
            leftImageRes = 1,
            rightImageRes = 2,
            requiredFinds = 2,
            spots = listOf(
                DifferenceSpot(0.2f, 0.2f, 0.08f),
                DifferenceSpot(0.7f, 0.7f, 0.08f)
            )
        )
        val engine = SpotDifferenceEngine(level)

        assertEquals(SpotDifferenceClickResult.Correct(0), engine.onTap(0.21f, 0.2f))
        assertEquals(1, engine.foundSpotIds.value.size)
        assertEquals(50, engine.score.value)
        assertFalse(engine.isComplete.value)

        assertEquals(SpotDifferenceClickResult.Correct(1), engine.onTap(0.7f, 0.72f))
        assertEquals(2, engine.foundSpotIds.value.size)
        assertEquals(100, engine.score.value)
        assertTrue(engine.isComplete.value)
    }

    @Test
    fun repeatedTapOnFoundSpotDoesNotIncreaseProgress() {
        val level = SpotDifferenceLevel(
            id = 1,
            title = "test",
            leftImageRes = 1,
            rightImageRes = 2,
            requiredFinds = 1,
            spots = listOf(DifferenceSpot(0.2f, 0.2f, 0.08f))
        )
        val engine = SpotDifferenceEngine(level)

        engine.onTap(0.2f, 0.2f)
        assertEquals(SpotDifferenceClickResult.AlreadyFound, engine.onTap(0.2f, 0.2f))
        assertEquals(1, engine.foundSpotIds.value.size)
    }

    @Test
    fun wrongTapDoesNotChangeProgress() {
        val level = SpotDifferenceLevel(
            id = 1,
            title = "test",
            leftImageRes = 1,
            rightImageRes = 2,
            requiredFinds = 1,
            spots = listOf(DifferenceSpot(0.2f, 0.2f, 0.08f))
        )
        val engine = SpotDifferenceEngine(level)

        assertEquals(SpotDifferenceClickResult.Wrong, engine.onTap(0.9f, 0.9f))
        assertTrue(engine.foundSpotIds.value.isEmpty())
        assertEquals(-50, engine.score.value)
        assertEquals(1, engine.wrongCount.value)
        assertFalse(engine.isComplete.value)
    }

    @Test
    fun threeWrongTapsOnOneImageMarksImageForSkipping() {
        val level = SpotDifferenceLevel(
            id = 1,
            title = "test",
            leftImageRes = 1,
            rightImageRes = 2,
            requiredFinds = 1,
            spots = listOf(DifferenceSpot(0.2f, 0.2f, 0.08f))
        )
        val engine = SpotDifferenceEngine(level)

        engine.onTap(0.9f, 0.9f)
        engine.onTap(0.8f, 0.8f)
        engine.onTap(0.7f, 0.7f)

        assertEquals(-150, engine.score.value)
        assertEquals(3, engine.wrongCount.value)
        assertTrue(engine.shouldSkipImage.value)
    }

    @Test
    fun completesAfterRequiredFindsEvenWhenMoreAcceptedSpotsRemain() {
        val level = SpotDifferenceLevel(
            id = 1,
            title = "test",
            leftImageRes = 1,
            rightImageRes = 2,
            requiredFinds = 2,
            spots = listOf(
                DifferenceSpot(0.1f, 0.1f, 0.08f),
                DifferenceSpot(0.3f, 0.3f, 0.08f),
                DifferenceSpot(0.5f, 0.5f, 0.08f),
                DifferenceSpot(0.7f, 0.7f, 0.08f)
            )
        )
        val engine = SpotDifferenceEngine(level)

        engine.onTap(0.1f, 0.1f)
        assertFalse(engine.isComplete.value)
        engine.onTap(0.3f, 0.3f)

        assertEquals(2, engine.foundSpotIds.value.size)
        assertTrue(engine.isComplete.value)
    }
}
