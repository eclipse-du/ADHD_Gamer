package com.piepie.brainhouse.game.wordreaction

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class WordReactionEngineTest {
    @Test
    fun targetPressWithinThreeSecondsAddsScoreOnce() {
        val engine = WordReactionEngine(targetWord = "喝茶", responseWindowMs = 3_000L)

        engine.onWordSpoken("爷爷正在喝茶", spokenAtMs = 10_000L)
        val result = engine.onButtonPressed(pressedAtMs = 12_500L)

        assertEquals(WordReactionPressResult.Correct, result)
        assertEquals(50, engine.score.value)
        assertEquals(1, engine.correctCount.value)
        assertEquals(0, engine.wrongCount.value)
        assertFalse(engine.hasOpenTargetWindow(nowMs = 12_600L))
    }

    @Test
    fun pressBeforeTargetWordDeductsScore() {
        val engine = WordReactionEngine(targetWord = "喝茶", responseWindowMs = 3_000L)

        val result = engine.onButtonPressed(pressedAtMs = 2_000L)

        assertEquals(WordReactionPressResult.TooEarly, result)
        assertEquals(-50, engine.score.value)
        assertEquals(1, engine.wrongCount.value)
    }

    @Test
    fun pressAfterThreeSecondWindowDeductsScore() {
        val engine = WordReactionEngine(targetWord = "喝茶", responseWindowMs = 3_000L)

        engine.onWordSpoken("妈妈开始喝茶", spokenAtMs = 5_000L)
        val result = engine.onButtonPressed(pressedAtMs = 8_001L)

        assertEquals(WordReactionPressResult.TooLate, result)
        assertEquals(-50, engine.score.value)
        assertEquals(1, engine.wrongCount.value)
    }

    @Test
    fun repeatedPressInSameTargetWindowDeductsAfterFirstHit() {
        val engine = WordReactionEngine(targetWord = "喝茶", responseWindowMs = 3_000L)

        engine.onWordSpoken("大家一起喝茶", spokenAtMs = 4_000L)
        assertEquals(WordReactionPressResult.Correct, engine.onButtonPressed(pressedAtMs = 4_800L))
        assertEquals(WordReactionPressResult.TooEarly, engine.onButtonPressed(pressedAtMs = 5_000L))

        assertEquals(0, engine.score.value)
        assertEquals(1, engine.correctCount.value)
        assertEquals(1, engine.wrongCount.value)
    }

    @Test
    fun storyBankHasTenTextsWithTenCoreKeywords() {
        val stories = WordReactionStoryBank.allStories
        val keywords = stories.map { it.targetWord }

        assertEquals(10, stories.size)
        assertEquals(10, keywords.toSet().size)
        assertTrue(stories.all { story ->
            story.segments.count { it.contains(story.targetWord) } >= 5
        })
    }

    @Test
    fun randomStoryCanPickDifferentTexts() {
        val first = WordReactionStoryBank.randomStory(Random(1))
        val second = WordReactionStoryBank.randomStory(Random(2))

        assertNotEquals(first.title, second.title)
    }

    @Test
    fun generatedStoryRunsThreeMinutesAtFastCadence() {
        val story = WordReactionStoryBank.allStories.first()
        val config = WordReactionConfig(targetWord = story.targetWord, durationSec = 180, cueIntervalMs = 1_200L)

        val script = WordReactionScriptFactory.create(config, story)
        val targetCount = script.count { it.text.contains(story.targetWord) }

        assertEquals(150, script.size)
        assertTrue(targetCount in 20..28)
        assertEquals(0L, script.first().startMs)
        assertEquals(178_800L, script.last().startMs)
    }

    @Test
    fun storyKeepsTargetWordSpreadAcrossTheWholeSession() {
        val story = WordReactionStoryBank.allStories.first()
        val script = WordReactionScriptFactory.create(WordReactionConfig(targetWord = story.targetWord), story)
        val targetTimes = script.filter { it.text.contains(story.targetWord) }.map { it.startMs }

        assertTrue(targetTimes.first() < 20_000L)
        assertTrue(targetTimes.last() > 160_000L)
        assertTrue(targetTimes.zipWithNext().all { (a, b) -> b - a >= 6_000L })
    }
}
