package com.piepie.brainhouse.game.schulte

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piepie.brainhouse.ui.theme.*
import com.piepie.brainhouse.util.SoundManager
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.piepie.brainhouse.ui.MacaronBackButton
import com.piepie.brainhouse.ui.LevelCompleteDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState

@Composable
fun SchulteGameScreen(
    levelId: Int,
    customConfig: SchulteLevelConfig? = null,
    onExit: () -> Unit,
    onLevelComplete: (Int, Long) -> Unit, // stars, time
    onNextLevel: () -> Unit,
    soundManager: SoundManager? = null
) {
    val scope = rememberCoroutineScope()
    val engine = remember { SchulteEngine(scope) }
    
    val gridItems by engine.gridItems.collectAsState()
    val nextNumber by engine.nextNumber.collectAsState()
    val timeElapsed by engine.timeElapsed.collectAsState()
    val gameState by engine.gameState.collectAsState()
    
    var showCompleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(levelId, customConfig) {
        val config = customConfig ?: SchulteLevels.getLevel(levelId)
        engine.startLevel(config)
        soundManager?.speak("找到数字 1")
    }

    // Effect for completion
    LaunchedEffect(gameState) {
        if (gameState == SchulteGameState.COMPLETED && !showCompleteDialog) {
            val stars = engine.calculateStars()
            onLevelComplete(stars, timeElapsed)
            soundManager?.speak("太棒了！挑战成功！")
            showCompleteDialog = true // Show dialog instead of exiting
        }
    }

    // Main Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MacaronBackButton(onClick = onExit)
            
            Text(
                "Level $levelId",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            
            Text(
                "${timeElapsed/1000}s",
                style = MaterialTheme.typography.titleLarge,
                color = TextSecondary
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Target: $nextNumber",
            style = MaterialTheme.typography.displayLarge,
            color = MacaronBlue
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Grid
        val config = engine.currentConfig ?: SchulteLevels.getLevel(levelId)
        val columns = config.gridSize
        
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width((columns * 80).dp)
            ) {
                items(gridItems) { number ->
                    SchulteCell(
                        number = number,
                        isNext = number == nextNumber,
                        isPast = number < nextNumber,
                        onClick = {
                             if (number == nextNumber) {
                                 soundManager?.playClick()
                                 engine.onNumberClicked(number)
                             }
                        }
                    )
                }
            }
        }
    }
    
    if (showCompleteDialog) {
        LevelCompleteDialog(
            stars = engine.calculateStars(),
            timeMs = timeElapsed,
            onNextLevel = {
                showCompleteDialog = false
                onNextLevel()
            },
            onExit = onExit
        )
    }
}

@Composable
fun SchulteCell(
    number: Int,
    isNext: Boolean,
    isPast: Boolean,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // Animation states
    val shakeOffset = remember { androidx.compose.animation.core.Animatable(0f) }
    var isError by remember { mutableStateOf(false) }
    
    val cellColor by animateColorAsState(
        targetValue = if (isError) Color(0xFFFFCDD2) else Color.White,
        animationSpec = tween(200)
    )
    
    // Scale for correct click (or general touch)
    val scale by animateFloatAsState(targetValue = if (isPast) 0.9f else 1f, animationSpec = tween(300))
    
    val borderColor = if (isNext) MacaronBlue else TextSecondary.copy(alpha=0.2f)
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .offset(x = shakeOffset.value.dp)
            .background(cellColor, RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !isPast) {
                if (isNext) {
                    onClick()
                } else {
                    // Wrong click animation
                    scope.launch {
                        // Flash Red
                        launch {
                            isError = true
                            delay(200)
                            isError = false
                        }
                        // Shake
                        launch {
                            for (i in 0..2) {
                                shakeOffset.animateTo(10f, tween(50))
                                shakeOffset.animateTo(-10f, tween(50))
                            }
                            shakeOffset.animateTo(0f, tween(50))
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
