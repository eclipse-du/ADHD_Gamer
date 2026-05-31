package com.piepie.brainhouse.game.treasurehunt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
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

@Composable
fun TreasureHuntScreen(
    levelId: Int,
    customConfig: TreasureHuntConfig? = null,
    onExit: () -> Unit,
    onSessionComplete: (Int, Int) -> Unit,
    soundManager: SoundManager? = null
) {
    val engine = remember { TreasureHuntEngine() }
    val gameState by engine.gameState.collectAsState()
    val score by engine.score.collectAsState()
    val currentGroupSize by engine.currentGroupSize.collectAsState()
    val currentMaxCells by engine.currentMaxCells.collectAsState()
    val currentDistance by engine.currentDistance.collectAsState()
    val completedGroups by engine.completedMaxGroupsInDifficulty.collectAsState()
    val targetCells by engine.targetCells.collectAsState()
    val foundCells by engine.foundCells.collectAsState()
    val previewCells by engine.previewCells.collectAsState()
    val cueCell by engine.cueCell.collectAsState()
    val cueVisible by engine.cueVisible.collectAsState()
    val timeLeftSec by engine.timeLeftSec.collectAsState()
    val trialTimeLeftSec by engine.trialTimeLeftSec.collectAsState()

    var showEndDialog by remember { mutableStateOf(false) }
    var completionReported by remember { mutableStateOf(false) }
    val config = customConfig ?: TreasureHuntLevels.getLevel(levelId)

    fun restart() {
        showEndDialog = false
        completionReported = false
        engine.start(config)
    }

    LaunchedEffect(levelId, customConfig) {
        engine.start(config)
    }

    LaunchedEffect(gameState) {
        while (gameState == TreasureHuntGameState.PLAYING) {
            delay(1000)
            engine.tickSessionSecond()
        }
    }

    LaunchedEffect(gameState) {
        while (gameState == TreasureHuntGameState.PLAYING) {
            delay(1000)
            engine.tickTrialSecond()
        }
    }

    LaunchedEffect(cueCell, cueVisible, gameState) {
        if (gameState == TreasureHuntGameState.PLAYING && cueVisible) {
            delay(config.previewDurationMs)
            engine.hideCue()
        }
    }

    LaunchedEffect(gameState) {
        if (gameState == TreasureHuntGameState.FINISHED && !completionReported) {
            completionReported = true
            showEndDialog = true
            onSessionComplete(engine.currentLevelNumber(), score)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MacaronBackButton(onClick = onExit)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "寻找宝箱",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "第 ${engine.currentLevelNumber()} 关  ${currentMaxCells}格 / 距离$currentDistance",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Text(
                text = formatTreasureTime(timeLeftSec),
                color = WarmOrange,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TreasureStat(label = "积分", value = score.toString(), color = WarmOrange)
            TreasureStat(label = "本组", value = "$currentGroupSize/$currentMaxCells", color = MacaronBlue)
            TreasureStat(label = "连过", value = "$completedGroups/2", color = MacaronGreen)
            TreasureStat(label = "点击", value = "${trialTimeLeftSec}s", color = SoftYellow)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (cueVisible) "记住红色和蓝色宝箱" else "把刚才的位置点出来",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        val targets = targetCells.toSet()
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            val boardSize = min(maxWidth.value, maxHeight.value).dp
            LazyVerticalGrid(
                columns = GridCells.Fixed(config.gridSize),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                userScrollEnabled = false,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(boardSize)
                    .aspectRatio(1f)
            ) {
                items(config.gridSize * config.gridSize) { index ->
                    val cell = GridCell(index / config.gridSize, index % config.gridSize)
                    val isCue = cueVisible && cell == cueCell
                    val isFound = cell in foundCells || (cueVisible && cell in previewCells)
                    val isHiddenTarget = cell in targets
                    TreasureCell(
                        isCue = isCue,
                        isFound = isFound,
                        isHiddenTarget = isHiddenTarget,
                        enabled = gameState == TreasureHuntGameState.PLAYING && !cueVisible,
                        onClick = {
                            val result = engine.onCellClicked(cell)
                            when (result) {
                                TreasureClickResult.Correct,
                                TreasureClickResult.TrialComplete,
                                TreasureClickResult.AlreadyFound -> soundManager?.playCorrect()
                                TreasureClickResult.Wrong -> soundManager?.playWrong()
                                TreasureClickResult.Ignored -> Unit
                            }
                        }
                    )
                }
            }
        }
    }

    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text("寻宝结束！", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "闯到第 ${engine.currentLevelNumber()} 关\n难度：${currentMaxCells}格 / 距离$currentDistance\n积分：$score",
                    color = TextSecondary,
                    fontSize = 18.sp,
                    lineHeight = 28.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { restart() },
                    colors = ButtonDefaults.buttonColors(containerColor = MacaronGreen)
                ) {
                    Text("再玩一次")
                }
            },
            dismissButton = {
                Button(
                    onClick = onExit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("返回")
                }
            }
        )
    }
}

@Composable
private fun TreasureStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun TreasureCell(
    isCue: Boolean,
    isFound: Boolean,
    isHiddenTarget: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val background = when {
        isCue -> Color(0xFFE53935)
        isFound -> Color(0xFF2196F3)
        else -> Color.White
    }
    val border = when {
        isCue -> Color(0xFFB71C1C)
        isFound -> Color(0xFF0D47A1)
        isHiddenTarget -> TextSecondary.copy(alpha = 0.18f)
        else -> TextSecondary.copy(alpha = 0.12f)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .shadow(if (isCue || isFound) 5.dp else 0.dp, RoundedCornerShape(8.dp))
            .background(background, RoundedCornerShape(8.dp))
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isCue) {
            Text("宝", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatTreasureTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
