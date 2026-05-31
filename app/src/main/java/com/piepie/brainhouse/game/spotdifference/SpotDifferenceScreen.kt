package com.piepie.brainhouse.game.spotdifference

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piepie.brainhouse.ui.MacaronBackButton
import com.piepie.brainhouse.ui.theme.MacaronGreen
import com.piepie.brainhouse.ui.theme.TextPrimary
import com.piepie.brainhouse.ui.theme.TextSecondary
import com.piepie.brainhouse.ui.theme.WarmOrange
import com.piepie.brainhouse.util.SoundManager
import kotlinx.coroutines.delay

@Composable
fun SpotDifferenceScreen(
    levelId: Int,
    onExit: () -> Unit,
    onLevelComplete: (Int, Long) -> Unit,
    soundManager: SoundManager? = null
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val previousOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            if (previousOrientation != null) {
                activity.requestedOrientation = previousOrientation
            }
        }
    }

    val levels = remember { SpotDifferenceLevels.allLevels() }
    val deck = remember { SpotDifferenceDeck(levels) }
    var level by remember(levelId) { mutableStateOf(deck.nextLevel()) }
    val engine = remember(level.id) { SpotDifferenceEngine(level) }
    val foundSpotIds by engine.foundSpotIds.collectAsState()
    val wrongCount by engine.wrongCount.collectAsState()
    val shouldSkipImage by engine.shouldSkipImage.collectAsState()
    val isComplete by engine.isComplete.collectAsState()
    var totalScore by remember { mutableIntStateOf(0) }
    var timeLeftSec by remember { mutableIntStateOf(300) }
    var showEndDialog by remember { mutableStateOf(false) }
    var completionReported by remember { mutableStateOf(false) }

    fun advanceImage() {
        level = deck.nextLevel()
    }

    LaunchedEffect(Unit) {
        while (timeLeftSec > 0) {
            delay(1000)
            timeLeftSec -= 1
        }
        showEndDialog = true
    }

    LaunchedEffect(isComplete, shouldSkipImage) {
        if (isComplete || shouldSkipImage) {
            delay(650)
            advanceImage()
        }
    }

    LaunchedEffect(showEndDialog) {
        if (showEndDialog && !completionReported) {
            completionReported = true
            onLevelComplete(3, totalScore.toLong())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            DifferenceImage(
                imageRes = level.leftImageRes,
                level = level,
                foundSpotIds = foundSpotIds,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onTap = { x, y ->
                    totalScore += handleSpotTap(engine, x, y, soundManager)
                }
            )
            DifferenceImage(
                imageRes = level.rightImageRes,
                level = level,
                foundSpotIds = foundSpotIds,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onTap = { x, y ->
                    totalScore += handleSpotTap(engine, x, y, soundManager)
                }
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(6.dp),
            color = Color.White.copy(alpha = 0.88f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacaronBackButton(onClick = onExit)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "找不同",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${level.id}/${levels.size} ${level.title}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "积分 $totalScore",
                    color = WarmOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${foundSpotIds.size}/${level.requiredFinds}",
                    color = MacaronGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "错 $wrongCount/3",
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = formatSpotTime(timeLeftSec),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Text(
            text = "DiapixUK CC BY 4.0",
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(topStart = 8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }

    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text("找不同结束！", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "本模式共有 ${levels.size} 组图片随机出现。\n最后积分：$totalScore",
                    color = TextSecondary,
                    fontSize = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onExit,
                    colors = ButtonDefaults.buttonColors(containerColor = MacaronGreen)
                ) {
                    Text("完成")
                }
            }
        )
    }
}

@Composable
private fun DifferenceImage(
    imageRes: Int,
    level: SpotDifferenceLevel,
    foundSpotIds: Set<Int>,
    modifier: Modifier = Modifier,
    onTap: (Float, Float) -> Unit
) {
    Box(
        modifier = modifier
            .background(Color.White)
            .pointerInput(level.id) {
                detectTapGestures { offset ->
                    onTap(offset.x / size.width, offset.y / size.height)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = level.title,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            foundSpotIds.forEach { index ->
                val spot = level.spots[index]
                val center = Offset(spot.x * size.width, spot.y * size.height)
                val radius = spot.radius * minOf(size.width, size.height)
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = 0.35f),
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = Color(0xFFE53935),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

private fun handleSpotTap(
    engine: SpotDifferenceEngine,
    x: Float,
    y: Float,
    soundManager: SoundManager?
): Int {
    return when (engine.onTap(x, y)) {
        is SpotDifferenceClickResult.Correct -> {
            soundManager?.playCorrect()
            50
        }
        SpotDifferenceClickResult.AlreadyFound -> {
            soundManager?.playCorrect()
            0
        }
        SpotDifferenceClickResult.Wrong -> {
            soundManager?.playWrong()
            -50
        }
    }
}

private fun formatSpotTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
