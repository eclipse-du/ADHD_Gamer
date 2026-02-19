package com.piepie.brainhouse.game.blindbox

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piepie.brainhouse.ui.theme.*
import com.piepie.brainhouse.util.SoundManager
import com.piepie.brainhouse.ui.MacaronBackButton
import com.piepie.brainhouse.ui.LevelCompleteDialog

@Composable
fun BlindBoxGameScreen(
    levelId: Int,
    customConfig: BlindBoxLevelConfig? = null,
    onExit: () -> Unit,
    onLevelComplete: (Int, Long) -> Unit, // stars, time
    onNextLevel: () -> Unit,
    soundManager: SoundManager? = null
) {
    val scope = rememberCoroutineScope()
    val engine = remember { BlindBoxEngine(scope) }
    
    val gameState by engine.gameState.collectAsState()
    val boxes by engine.boxes.collectAsState()
    val currentQuestion by engine.currentQuestion.collectAsState()
    
    var showCompleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(levelId, customConfig) {
        val config = customConfig ?: BlindBoxLevels.getLevel(levelId)
        engine.startLevel(config)
        soundManager?.playVoice(com.piepie.brainhouse.R.raw.audio_blindbox_intro)
    }

    LaunchedEffect(gameState) {
        if (gameState == BlindBoxState.LEVEL_COMPLETE && !showCompleteDialog) {
            val result = engine.calculateResult()
            onLevelComplete(result.stars, result.timeMs)
            soundManager?.playVoice(com.piepie.brainhouse.R.raw.audio_blindbox_victory)
            showCompleteDialog = true
        }
        if (gameState == BlindBoxState.QUESTION_TIME) {
            soundManager?.playVoice(com.piepie.brainhouse.R.raw.audio_blindbox_question)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MacaronPink.copy(alpha=0.1f))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
                    "Blind Box Level $levelId",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Grid
            // Calculate columns dynamically
            val boxCount = boxes.size
            val columns = kotlin.math.sqrt(boxCount.toDouble()).toInt().coerceAtLeast(2)
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(boxes) { box ->
                    BlindBoxItem(
                        box = box,
                        onClick = {
                            soundManager?.playClick()
                            engine.onBoxClick(box.id)
                        }
                    )
                }
            }
        }
        
        // Question Overlay
        if (gameState == BlindBoxState.QUESTION_TIME && currentQuestion != null) {
            val question = currentQuestion!!
            
            // Use a semi-transparent scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(enabled = false) {}, // Block clicks
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "刚才第 ${question.targetIndex + 1} 个盲盒里是什么颜色？",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Options
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            question.options.forEach { option ->
                                if (option is BoxContent.ColorContent) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(option.color)
                                            .clickable {
                                                soundManager?.playClick()
                                                engine.submitAnswer(option)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Maybe name text?
                                        Text(option.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showCompleteDialog) {
             LevelCompleteDialog(
                stars = engine.calculateResult().stars, // Recalculating is cheap
                timeMs = engine.calculateResult().timeMs,
                onNextLevel = {
                    showCompleteDialog = false
                    onNextLevel()
                },
                onExit = onExit
            )
        }
    }
}

@Composable
fun BlindBoxItem(
    box: BoxItem,
    onClick: () -> Unit
) {
    val isOpen = box.isOpen
    val isLocked = box.isLocked
    
    val scale by animateFloatAsState(targetValue = if (isOpen) 1.05f else 1f)
    
    val bgColor = if (isLocked) Color.LightGray.copy(alpha=0.3f) else MacaronPink
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .shadow(if (isOpen) 8.dp else 2.dp, RoundedCornerShape(16.dp))
            .background(bgColor, RoundedCornerShape(16.dp))
            .clickable(enabled = !isLocked, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isOpen) {
            val content = box.content
            if (content is BoxContent.ColorContent) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(8.dp).background(content.color, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(content.name, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Closed State
            Text(
                text = "${box.id + 1}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLocked) Color.Gray else Color.White
            )
        }
    }
}
