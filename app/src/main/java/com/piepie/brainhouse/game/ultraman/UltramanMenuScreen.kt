package com.piepie.brainhouse.game.ultraman

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piepie.brainhouse.R
import com.piepie.brainhouse.ui.MacaronBackButton
import com.piepie.brainhouse.ui.theme.MacaronBlue
import com.piepie.brainhouse.ui.theme.MacaronPink
import com.piepie.brainhouse.ui.theme.SoftYellow

@Composable
fun UltramanMenuScreen(
    viewModel: UltramanViewModel = viewModel(),
    onBack: () -> Unit,
    onStartLevel: (Int, Boolean) -> Unit // levelId, isEndless
) {
    val progress by viewModel.progress.collectAsState()
    var isEndless by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)) // Space Dark Blue
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacaronBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "奥特曼特训",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Mode Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White.copy(alpha=0.1f), RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Text("模式: ", color = Color.White)
                Switch(checked = isEndless, onCheckedChange = { isEndless = it })
                Text(if (isEndless) "无尽挑战" else "60秒特训", color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // Level Cards
            LevelCard(
                levelId = 1,
                title = "贝利亚·逆袭",
                description = "用双腿夹住赛罗的光线！",
                isUnlocked = progress.level1Unlocked,
                imageRes = R.drawable.char_sd_belial,
                onClick = { onStartLevel(1, isEndless) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LevelCard(
                levelId = 2,
                title = "赛罗·守护",
                description = "用盾牌挡住贝利亚的攻击！",
                isUnlocked = progress.level2Unlocked,
                imageRes = R.drawable.char_sd_zero,
                onClick = { onStartLevel(2, isEndless) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            LevelCard(
                levelId = 3,
                title = "艾斯·闪避",
                description = "跳跃躲避致命光线！",
                isUnlocked = progress.level3Unlocked,
                imageRes = R.drawable.char_sd_ace,
                onClick = { onStartLevel(3, isEndless) }
            )
        }
    }
}

@Composable
fun LevelCard(
    levelId: Int,
    title: String,
    description: String,
    isUnlocked: Boolean,
    imageRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .alpha(if (isUnlocked) 1f else 0.5f)
            .clickable(enabled = isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isUnlocked) MacaronBlue else Color.Gray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "No.$levelId $title",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isUnlocked) description else "完成上一关解锁",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
