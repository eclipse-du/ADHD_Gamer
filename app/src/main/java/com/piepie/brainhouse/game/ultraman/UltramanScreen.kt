package com.piepie.brainhouse.game.ultraman

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piepie.brainhouse.R
import androidx.compose.ui.draw.rotate
import com.piepie.brainhouse.ui.theme.MacaronBlue
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import com.piepie.brainhouse.ui.theme.SoftYellow

@Composable
fun UltramanScreen(
    levelId: Int,
    isEndless: Boolean,
    viewModel: UltramanViewModel,
    onBack: () -> Unit,
    onNextLevel: () -> Unit,
    onUnlockHonor: (Int) -> Unit,
    soundManager: com.piepie.brainhouse.util.SoundManager? = null
) {
    val scope = rememberCoroutineScope()
    val engine = remember { UltramanGameEngine(scope) }
    
    val gameState by engine.gameState.collectAsState()
    val timeLeft by engine.timeLeft.collectAsState()
    val score by engine.score.collectAsState()
    val beams by engine.activeBeams.collectAsState()
    val isActing by engine.isActing.collectAsState()
    
    // Frame Loop for Smooth Animation (60fps)
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(16) // Approx 60fps
        }
    }
    
    // Level Config
    val level = UltramanLevel.values().find { it.id == levelId } ?: UltramanLevel.LEVEL_1

    // Asset Mapping
    val bgRes = R.drawable.ultraman_bg_graveyard
    val playerRes = when (level) {
        UltramanLevel.LEVEL_1 -> R.drawable.char_sd_belial
        UltramanLevel.LEVEL_2 -> R.drawable.char_sd_zero
        UltramanLevel.LEVEL_3 -> R.drawable.char_sd_ace
    }
    val enemyRes = when (level) {
        UltramanLevel.LEVEL_1 -> R.drawable.char_sd_zero
        UltramanLevel.LEVEL_2 -> R.drawable.char_sd_belial
        UltramanLevel.LEVEL_3 -> R.drawable.char_sd_belial
    }
    val beamRes = when (level) {
        UltramanLevel.LEVEL_1 -> R.drawable.effect_beam_zero
        UltramanLevel.LEVEL_2 -> R.drawable.effect_beam_belial
        UltramanLevel.LEVEL_3 -> R.drawable.effect_beam_belial
    }

    LaunchedEffect(Unit) {
        engine.startGame(level, isEndless)
    }

    // Game Over / Victory Handling
    LaunchedEffect(gameState) {
        if (gameState == UltramanGameState.VICTORY) {
            val finalScore = if (isEndless) score else timeLeft // Or 60
            viewModel.completeLevel(levelId, finalScore)
            
            // Unlock Honor
            if (!isEndless) {
                // L1 -> 22, L2 -> 23, L3 -> 24
                onUnlockHonor(21 + levelId)
            } else {
                if (score >= 30) onUnlockHonor(25) // Endless
            }
            
            // TTS Victory
            if (!isEndless) {
                 when (levelId) {
                     1 -> soundManager?.playVoice(R.raw.audio_ultraman_l1_win)
                     2 -> soundManager?.playVoice(R.raw.audio_ultraman_l2_win)
                     3 -> soundManager?.playVoice(R.raw.audio_ultraman_l3_win)
                     else -> soundManager?.playVoice(R.raw.audio_ultraman_l3_win) 
                 }
            } else {
                 soundManager?.playVoice(R.raw.audio_ultraman_endless_win)
            }
        } else if (gameState == UltramanGameState.GAME_OVER) {
             // TTS Failure
             when (levelId) {
                 1 -> soundManager?.playVoice(R.raw.audio_ultraman_l1_lose)
                 2 -> soundManager?.playVoice(R.raw.audio_ultraman_l2_lose)
                 3 -> soundManager?.playVoice(R.raw.audio_ultraman_l3_lose)
                 else -> soundManager?.playVoice(R.raw.audio_ultraman_l1_lose)
             }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = gameState == UltramanGameState.PLAYING) {
                engine.onPlayerAction()
            }
    ) {
                // Background
        Image(
            painter = painterResource(bgRes),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Spotlight Overlay
        val hitZoneBias = 0.6f
        val density = LocalDensity.current.density // Get density explicitly if needed, but DrawScope has it
        
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.99f }) {
            drawRect(Color.Black.copy(alpha = 0.7f))
            
            // DrawScope has 'size' property
            val centerOffset = size.height / 2
            
            // 400.dp to Px can be done via 'hitZoneBias * 400.dp.toPx()' if extension works.
            // If not, we can manually convert: 400 * density.
            // Or use 'toPx()' function of DrawScope? No, DrawScope extends Density.
            // The extension 'fun Dp.toPx(): Float' is what we need.
            // If it's missing, use 'this.toPx(400.dp)' not available?
            // Use roundToPx? No.
            
            // Workaround: Calculate offset in dp outside or use manual calc?
            // Let's rely on standard extension. If it failed, maybe naming collision.
            // Let's try:
            val offsetPx = hitZoneBias * 400.dp.toPx() 
            val hitY = centerOffset + offsetPx
            val hitX = size.width / 2
            val radiusPx = 60.dp.toPx()
            
            drawCircle(
                color = Color.Transparent,
                radius = radiusPx, 
                center = androidx.compose.ui.geometry.Offset(hitX, hitY),
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )
            // Draw Ring
            drawCircle(
                color = Color.Green.copy(alpha = 0.5f),
                radius = radiusPx,
                center = androidx.compose.ui.geometry.Offset(hitX, hitY),
                style = Stroke(width = 3.dp.toPx())
            )
        }
        
        // HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = SoftYellow)) {
                Text("返回")
            }
            
            Text(
                text = if (isEndless) "Score: $score" else "Time: ${timeLeft}s",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Game Area
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Enemy (Top Center)
            Image(
                painter = painterResource(enemyRes),
                contentDescription = "Enemy",
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
            )

            // Player (Bottom Center)
            val actionScale by animateFloatAsState(if (isActing) 1.2f else 1.0f)
            val actionAlpha by animateFloatAsState(if (isActing && level == UltramanLevel.LEVEL_2) 0.5f else 1.0f) // Shield visual
            val actionOffset by animateFloatAsState(if (isActing && level == UltramanLevel.LEVEL_3) -100f else 0f) // Jump visual

            // Position Player Inside Spotlight
            // Spotlight is at hitZoneBias (0.6).
            // Player should be aligned there.
            // Previous: Align BottomCenter + padding 100.
            // Let's align Player to the Hit Zone precisely.
            
            // Position Player Inside Spotlight
            // Spotlight is at hitZoneBias (0.6).
            // Player size reduced to 110.dp (User request)
            
             Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (0.6f * 400).dp) // Match spotlight
                    .size(110.dp) // Smaller Player
             ) {
                 Image(
                    painter = painterResource(playerRes),
                    contentDescription = "Player",
                    modifier = Modifier.fillMaxSize()
                        .offset(y = actionOffset.dp)
                        .scale(actionScale)
                        .alpha(actionAlpha)
                 )
             }


            // Beams
            // Use 'now' from Frame Loop for smooth interpolation
            
            beams.forEach { beam ->
                val progress = (now - beam.startTime).toFloat() / beam.speed.toFloat()
                if (progress in 0f..1.1f) {
                     // Vertical Path: Top to Bottom
                     
                     // Bias: -0.6 (Top) to 0.6 (Bottom Target)
                     // hitZone is at 0.6 bias.
                     // Beam starts at -0.8 (-0.6 was top center, slightly above for spawn).
                     val startBias = -0.8f
                     val endBias = 0.6f // Target Center
                     
                     val yBias = startBias + (endBias - startBias) * progress
                     
                     Image(
                        painter = painterResource(beamRes),
                        contentDescription = "Beam",
                        modifier = Modifier
                            .size(40.dp, 100.dp) // Vertical Beam shape
                            .align(Alignment.Center) 
                            .offset(y = (yBias * 400).dp) // 400dp scaler
                     )
                }
            }
        }

    // Countdown Overlay
    if (gameState == UltramanGameState.COUNTDOWN) {
        val countdownValue by engine.countdown.collectAsState()
        val scale by animateFloatAsState(
            targetValue = if (countdownValue > 0) 1.5f else 0.5f,
            animationSpec = tween(500),
            label = "countdown_scale"
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(enabled = false) {}, // Block interaction
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$countdownValue",
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Yellow,
                modifier = Modifier.scale(scale)
            )
        }
    }

    // Overlay: Game Over / Victory
    if (gameState == UltramanGameState.GAME_OVER || gameState == UltramanGameState.VICTORY) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(if (gameState == UltramanGameState.VICTORY) "挑战成功！" else "挑战失败") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val promptText = if (gameState == UltramanGameState.VICTORY) {
                        if (isEndless) "光之纽带，超越极限！新的纪录！" 
                        else when (levelId) {
                             1 -> "光之力量觉醒！黑暗已被驱散！"
                             2 -> "这种速度... 甚至超越了赛罗！"
                             3 -> "热忱之心不可磨灭！你就是新的光之巨人！"
                             else -> "挑战成功！"
                        }
                    } else {
                        when (levelId) {
                             1 -> "你还差两万年呢！"
                             2 -> "本大爷是最强的！黑暗即将来临！"
                             3 -> "不要放弃！再次燃烧你的热忱之心！"
                             else -> "特训失败！"
                        }
                    }
                    
                    Text(promptText, style = MaterialTheme.typography.bodyLarge)
                    
                    if (gameState == UltramanGameState.VICTORY && !isEndless) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("解锁对应奥特曼合影！")
                        val rewardRes = when (levelId) {
                            1 -> R.drawable.photo_pike_gen_1
                            2 -> R.drawable.photo_pike_gen_2
                            3 -> R.drawable.photo_pike_gen_3
                            else -> R.drawable.user_child_photo
                        }
                        Image(
                            painter = painterResource(rewardRes),
                            contentDescription = "Reward",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(250.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .border(5.dp, Color.Green, androidx.compose.foundation.shape.CircleShape) // Standard Green
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { 
                    if (gameState == UltramanGameState.VICTORY && !isEndless) onNextLevel() else engine.retryLevel()
                }) {
                    Text(if (gameState == UltramanGameState.VICTORY) "继续" else "重试")
                }
            },
            dismissButton = {
                Button(onClick = onBack) {
                    Text("退出")
                }
            }
        )
    }
    }
}


