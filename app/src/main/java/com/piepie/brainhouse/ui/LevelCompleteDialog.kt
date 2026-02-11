package com.piepie.brainhouse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.piepie.brainhouse.ui.theme.*

@Composable
fun LevelCompleteDialog(
    stars: Int,
    timeMs: Long,
    onNextLevel: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = { /* No dismiss */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "挑战成功！",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Stars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        val color = if (index < stars) WarmOrange else Color.LightGray
                        Text("★", fontSize = 48.sp, color = color)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "用时: ${timeMs / 1000}s",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onExit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("返回")
                    }
                    
                    Button(
                        onClick = onNextLevel,
                        colors = ButtonDefaults.buttonColors(containerColor = MacaronGreen)
                    ) {
                        Text("下一关")
                    }
                }
            }
        }
    }
}
