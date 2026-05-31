package com.piepie.brainhouse.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piepie.brainhouse.R
import com.piepie.brainhouse.ui.theme.*
import kotlin.math.absoluteValue

// Helper function to avoid import issues
fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LevelSelectSyncScreen(
    onLevelSelected: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        // Background Decor
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .size(300.dp)
                .background(Brush.radialGradient(listOf(SoftYellow.copy(alpha=0.4f), Color.Transparent)))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacaronBackButton(onClick = onBack)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "选择模式",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontSize = 28.sp
                )
                
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(48.dp)) // Balance
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Carousel
            val pagerState = rememberPagerState(pageCount = { 6 })
            
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 48.dp),
                modifier = Modifier.weight(1f)
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                
                // Parallax/Scale Effect
                val scale = lerp(
                    start = 0.85f,
                    stop = 1f,
                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                )
                val alpha = lerp(
                    start = 0.5f,
                    stop = 1f,
                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (page == 0) {
                        GameModeCardLarge(
                            title = "舒尔特方格",
                            subtitle = "Schulte Grid",
                            desc = "专注力训练\n通过寻找数字，提升注意力集中度。",
                            color = MacaronBlue,
                            iconEmoji = "🦉", // or Owl
                            onClick = { onLevelSelected("SCHULTE", 1) }
                        )
                    } else if (page == 1) {
                        GameModeCardLarge(
                            title = "盲盒记忆",
                            subtitle = "Blind Box Memory",
                            desc = "记忆力训练\n记住物品位置，锻炼瞬间记忆力。",
                            color = MacaronPink,
                            iconEmoji = "🎁", // or Cat/Box
                            onClick = { onLevelSelected("BLINDBOX", 1) }
                        )
                    } else if (page == 2) {
                        GameModeCardLarge(
                            title = "奥特曼特训",
                            subtitle = "Ultraman Agility",
                            desc = "敏捷力训练\n通过快速反应，守护光之国！",
                            color = Color(0xFFE53935), // Ultraman Red
                            iconEmoji = "🦸", // Ultraman
                            onClick = { onLevelSelected("ULTRAMAN", 1) }
                        )
                    } else if (page == 3) {
                        GameModeCardLarge(
                            title = "寻找宝箱",
                            subtitle = "Treasure Hunt",
                            desc = "专注力挑战\n记住宝箱位置，连续找到就会升级。",
                            color = Color(0xFFFFA726),
                            iconEmoji = "💎",
                            onClick = { onLevelSelected("TREASURE", 1) }
                        )
                    } else if (page == 4) {
                        GameModeCardLarge(
                            title = "找不同",
                            subtitle = "Spot Difference",
                            desc = "观察力挑战\n左右对比，找到不一样的地方。",
                            color = Color(0xFF66BB6A),
                            iconEmoji = "🔍",
                            onClick = { onLevelSelected("SPOTDIFF", 1) }
                        )
                    } else {
                        GameModeCardLarge(
                            title = "听词反应",
                            subtitle = "Word Reaction",
                            desc = "听觉专注力挑战\n听到目标词后 3 秒内按下按钮。",
                            color = Color(0xFF42A5F5),
                            iconEmoji = "🎧",
                            onClick = { onLevelSelected("WORD_REACTION", 1) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Pager Indicator
            Row(
                modifier = Modifier.height(50.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(6) { iteration ->
                    val color = if (pagerState.currentPage == iteration) TextPrimary else TextSecondary.copy(alpha=0.3f)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(10.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun GameModeCardLarge(
    title: String,
    subtitle: String,
    desc: String,
    color: Color,
    iconEmoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f) // Tall card
            .clickable(onClick = onClick)
            .shadow(16.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Area
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(Color.White.copy(alpha=0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconEmoji, fontSize = 80.sp)
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha=0.8f)
                )
            }
            
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha=0.9f),
                modifier = Modifier.padding(horizontal = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = color),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("开始 (START)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}
