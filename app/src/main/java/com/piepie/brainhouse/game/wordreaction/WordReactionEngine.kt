package com.piepie.brainhouse.game.wordreaction

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import kotlin.random.Random

data class WordCue(
    val text: String,
    val startMs: Long
)

data class WordReactionStory(
    val title: String,
    val targetWord: String,
    val segments: List<String>
)

data class WordReactionConfig(
    val targetWord: String = "喝茶",
    val durationSec: Int = 180,
    val cueIntervalMs: Long = 1_200L,
    val responseWindowMs: Long = 3_000L
)

enum class WordReactionPressResult {
    Correct,
    TooEarly,
    TooLate
}

class WordReactionEngine(
    val targetWord: String,
    private val responseWindowMs: Long
) {
    private val _score = mutableIntStateOf(0)
    val score: State<Int> = _score

    private val _correctCount = mutableIntStateOf(0)
    val correctCount: State<Int> = _correctCount

    private val _wrongCount = mutableIntStateOf(0)
    val wrongCount: State<Int> = _wrongCount

    private var activeTargetStartMs: Long? = null

    fun onWordSpoken(text: String, spokenAtMs: Long) {
        if (text.contains(targetWord)) {
            activeTargetStartMs = spokenAtMs
        }
    }

    fun onButtonPressed(pressedAtMs: Long): WordReactionPressResult {
        val targetStart = activeTargetStartMs
        val result = when {
            targetStart == null -> WordReactionPressResult.TooEarly
            pressedAtMs in targetStart..(targetStart + responseWindowMs) -> WordReactionPressResult.Correct
            else -> WordReactionPressResult.TooLate
        }

        if (result == WordReactionPressResult.Correct) {
            _score.intValue += 50
            _correctCount.intValue += 1
        } else {
            _score.intValue -= 50
            _wrongCount.intValue += 1
        }
        activeTargetStartMs = null

        return result
    }

    fun hasOpenTargetWindow(nowMs: Long): Boolean {
        val targetStart = activeTargetStartMs ?: return false
        return nowMs in targetStart..(targetStart + responseWindowMs)
    }
}

object WordReactionStoryBank {
    val allStories = listOf(
        createStory("小茶馆", "喝茶", "小米", "茶馆", "茶杯", "爷爷"),
        createStory("运动会", "跑步", "乐乐", "操场", "红旗", "老师"),
        createStory("小画展", "画画", "朵朵", "教室", "彩笔", "同桌"),
        createStory("干净小手", "洗手", "安安", "水池", "毛巾", "妈妈"),
        createStory("图书角", "读书", "豆豆", "书屋", "书签", "姐姐"),
        createStory("音乐会", "拍手", "晨晨", "舞台", "小鼓", "爸爸"),
        createStory("菜园雨后", "浇水", "果果", "菜园", "水壶", "奶奶"),
        createStory("牙齿卫士", "刷牙", "天天", "浴室", "牙杯", "医生"),
        createStory("彩绳比赛", "跳绳", "圆圆", "院子", "彩绳", "朋友"),
        createStory("礼貌拜访", "敲门", "贝贝", "走廊", "门铃", "阿姨")
    )

    fun randomStory(random: Random = Random.Default): WordReactionStory {
        return allStories[random.nextInt(allStories.size)]
    }

    private fun createStory(
        title: String,
        targetWord: String,
        child: String,
        place: String,
        prop: String,
        helper: String
    ): WordReactionStory {
        return WordReactionStory(
            title = title,
            targetWord = targetWord,
            segments = listOf(
                "早上阳光亮",
                "$child 醒来了",
                "$helper 微笑说",
                "今天去$place",
                "$child 点点头",
                "$child $targetWord",
                "$prop 放桌上",
                "小风吹窗帘",
                "路上很安静",
                "小鸟飞过去",
                "$helper 牵着手",
                "他们慢慢走",
                "来到$place 里",
                "大家排好队",
                "$helper $targetWord",
                "$child 认真听",
                "不要乱按哦",
                "铃声轻轻响",
                "故事继续讲",
                "$prop 闪闪亮",
                "$child 又看见",
                "朋友挥挥手",
                "朋友也$targetWord",
                "大家笑起来",
                "白云慢慢飘",
                "老师点点头",
                "脚步轻轻走",
                "$child 坐下来",
                "这时$child $targetWord",
                "心里很开心",
                "小猫路过了",
                "小狗坐好了",
                "$helper 收好$prop",
                "大家准备回家",
                "回家前再$targetWord",
                "今天真快乐"
            )
        )
    }
}

object WordReactionScriptFactory {
    fun create(
        config: WordReactionConfig,
        story: WordReactionStory = WordReactionStoryBank.allStories.first()
    ): List<WordCue> {
        val totalCues = ((config.durationSec * 1_000L) / config.cueIntervalMs).toInt()
        return List(totalCues) { index ->
            WordCue(
                text = story.segments[index % story.segments.size],
                startMs = index * config.cueIntervalMs
            )
        }
    }
}
