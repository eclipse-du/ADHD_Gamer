package com.piepie.brainhouse.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piepie.brainhouse.ui.theme.*
import com.piepie.brainhouse.util.SoundManager
import kotlin.math.roundToInt

@Composable
fun MainMenuScreen(
    onNavigateToGameSelect: () -> Unit,
    onNavigateToCustom: () -> Unit,
    onNavigateToHonors: () -> Unit,
    soundManager: SoundManager? = null
) {
    // Dynamic Background Animation
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val offsetAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        // Decorative Blobs
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = (-50).dp)
                .size(200.dp)
                .background(Brush.radialGradient(listOf(MacaronBlue.copy(alpha=0.6f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = 50.dp)
                .size(300.dp)
                .background(Brush.radialGradient(listOf(MacaronPink.copy(alpha=0.5f), Color.Transparent)))
        )
        
        // Floating Shapes
        FloatingShape(
            color = SoftYellow, 
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 40.dp).offset(y = offsetAnim.dp)
        )
        FloatingShape(
            color = MacaronGreen, 
            modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 120.dp, start = 40.dp).offset(y = -offsetAnim.dp)
        )

        // Main Content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title Area
            Box(contentAlignment = Alignment.Center) {
                // Shadow Text
                Text(
                    text = "派派脑力屋",
                    style = MaterialTheme.typography.displayLarge.copy(
                        shadow = Shadow(
                            color = MacaronPink,
                            offset = Offset(4f, 4f),
                            blurRadius = 8f
                        )
                    ),
                    fontSize = 48.sp,
                    color = TextPrimary
                )
            }
            Text(
                text = "Pike Brain House",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Menu Items
            MenuButton(
                text = "开启脑力冒险", // Start Adventure
                icon = "🚀",
                gradient = Brush.linearGradient(listOf(MacaronBlue, MacaronGreen)),
                onClick = {
                    soundManager?.playClick()
                    onNavigateToGameSelect()
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            MenuButton(
                text = "自由训练场", // Custom Training
                icon = "🛠️",
                gradient = Brush.linearGradient(listOf(WarmOrange, SoftYellow)),
                onClick = {
                    soundManager?.playClick()
                    onNavigateToCustom()
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            MenuButton(
                text = "派派荣誉墙", // Hall of Fame
                icon = "🏆",
                gradient = Brush.linearGradient(listOf(MacaronPink, Color(0xFFFF99CC))),
                onClick = {
                    soundManager?.playClick()
                    onNavigateToHonors()
                }
            )
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(90.dp)
            .scale(scale)
            .clickable { 
                isPressed = true
                onClick()
            }
            .shadow(
                elevation = if (isPressed) 4.dp else 12.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color.Gray // Simplified spot color or extract from gradient?
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon Bubble
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(24.dp))

                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FloatingShape(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .rotate(45f)
            .background(color.copy(alpha=0.6f), RoundedCornerShape(8.dp))
    )
}
