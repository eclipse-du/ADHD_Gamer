package com.piepie.brainhouse.game.wordreaction

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piepie.brainhouse.ui.MacaronBackButton
import com.piepie.brainhouse.ui.theme.CreamBackground
import com.piepie.brainhouse.ui.theme.MacaronBlue
import com.piepie.brainhouse.ui.theme.MacaronGreen
import com.piepie.brainhouse.ui.theme.SoftYellow
import com.piepie.brainhouse.ui.theme.TextPrimary
import com.piepie.brainhouse.ui.theme.TextSecondary
import com.piepie.brainhouse.ui.theme.WarmOrange
import com.piepie.brainhouse.util.SoundManager
import kotlinx.coroutines.delay

private enum class WordReactionUiState {
    Ready,
    Playing,
    Finished
}

@Composable
fun WordReactionScreen(
    levelId: Int,
    onExit: () -> Unit,
    onSessionComplete: (score: Int, correct: Int) -> Unit,
    soundManager: SoundManager? = null
) {
    val story = remember(levelId) { WordReactionStoryBank.randomStory() }
    val config = remember(story) { WordReactionConfig(targetWord = story.targetWord) }
    val script = remember(config, story) { WordReactionScriptFactory.create(config, story) }
    val engine = remember(config) {
        WordReactionEngine(
            targetWord = config.targetWord,
            responseWindowMs = config.responseWindowMs
        )
    }
    val preloadTexts = remember {
        WordReactionStoryBank.allStories.flatMap { it.segments }.distinct()
    }

    var uiState by remember { mutableStateOf(WordReactionUiState.Ready) }
    var feedback by remember { mutableStateOf("正在准备声音") }
    var remainingSec by remember { mutableIntStateOf(config.durationSec) }
    var startUptimeMs by remember { mutableLongStateOf(0L) }
    var completedReported by remember { mutableStateOf(false) }
    var speechReady by remember { mutableStateOf(soundManager == null) }
    var speechPrepared by remember { mutableIntStateOf(0) }
    var speechTotal by remember { mutableIntStateOf(preloadTexts.size) }

    LaunchedEffect(soundManager) {
        val manager = soundManager
        if (manager == null) {
            feedback = "本轮故事：${story.title}"
            return@LaunchedEffect
        }
        manager.prepareSpeechCache(
            texts = preloadTexts,
            onProgress = { prepared, total ->
                speechPrepared = prepared
                speechTotal = total
                feedback = "正在准备声音 $prepared/$total"
            },
            onComplete = {
                speechReady = true
                feedback = "本轮故事：${story.title}"
            }
        )
    }

    LaunchedEffect(uiState) {
        if (uiState != WordReactionUiState.Playing) return@LaunchedEffect

        startUptimeMs = SystemClock.elapsedRealtime()
        feedback = "仔细听，等“${config.targetWord}”出现"

        script.forEach { cue ->
            val targetTime = startUptimeMs + cue.startMs
            val waitMs = targetTime - SystemClock.elapsedRealtime()
            if (waitMs > 0) delay(waitMs)

            val elapsedMs = SystemClock.elapsedRealtime() - startUptimeMs
            remainingSec = (config.durationSec - (elapsedMs / 1_000L).toInt()).coerceAtLeast(0)
            engine.onWordSpoken(cue.text, elapsedMs)
            soundManager?.playSpeech(cue.text)
        }

        val endTime = startUptimeMs + config.durationSec * 1_000L
        val waitToEnd = endTime - SystemClock.elapsedRealtime()
        if (waitToEnd > 0) delay(waitToEnd)
        remainingSec = 0
        uiState = WordReactionUiState.Finished
    }

    LaunchedEffect(uiState, completedReported) {
        if (uiState == WordReactionUiState.Finished && !completedReported) {
            completedReported = true
            soundManager?.playCorrect()
            onSessionComplete(engine.score.value, engine.correctCount.value)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(260.dp)
                .background(
                    Brush.radialGradient(
                        listOf(SoftYellow.copy(alpha = 0.45f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacaronBackButton(onClick = onExit)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "听词反应",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatPill("时间", formatSeconds(remainingSec), MacaronBlue, Modifier.weight(1f))
                StatPill("积分", "${engine.score.value}", WarmOrange, Modifier.weight(1f))
                StatPill("命中", "${engine.correctCount.value}", MacaronGreen, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "听到“${config.targetWord}”后 3 秒内按按钮",
                color = TextPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = feedback,
                color = TextSecondary,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            if (uiState == WordReactionUiState.Ready) {
                Button(
                    enabled = speechReady,
                    onClick = {
                        soundManager?.playClick()
                        uiState = WordReactionUiState.Playing
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .shadow(10.dp, RoundedCornerShape(46.dp)),
                    shape = RoundedCornerShape(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MacaronBlue,
                        disabledContainerColor = TextSecondary.copy(alpha = 0.25f)
                    )
                ) {
                    Text(
                        if (speechReady) "开始听" else "准备声音 $speechPrepared/$speechTotal",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    enabled = uiState == WordReactionUiState.Playing,
                    onClick = {
                        val pressedAt = SystemClock.elapsedRealtime() - startUptimeMs
                        when (engine.onButtonPressed(pressedAt)) {
                            WordReactionPressResult.Correct -> {
                                soundManager?.playCorrect()
                                feedback = "答对啦 +50"
                            }
                            WordReactionPressResult.TooEarly -> {
                                soundManager?.playWrong()
                                feedback = "太早啦 -50"
                            }
                            WordReactionPressResult.TooLate -> {
                                soundManager?.playWrong()
                                feedback = "太晚啦 -50"
                            }
                        }
                    },
                    modifier = Modifier
                        .size(260.dp)
                        .shadow(16.dp, CircleShape),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmOrange,
                        disabledContainerColor = TextSecondary.copy(alpha = 0.25f)
                    )
                ) {
                    Text("按！", fontSize = 54.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        if (uiState == WordReactionUiState.Finished) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("听词反应完成") },
                text = {
                    Text(
                        "最后积分：${engine.score.value}\n听到“${config.targetWord}”并按对：${engine.correctCount.value} 次\n按错：${engine.wrongCount.value} 次"
                    )
                },
                confirmButton = {
                    TextButton(onClick = onExit) {
                        Text("回到大厅")
                    }
                }
            )
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, color = Color.White.copy(alpha = 0.82f), fontSize = 13.sp)
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatSeconds(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "%d:%02d".format(min, sec)
}
