package com.piepie.brainhouse.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piepie.brainhouse.ui.theme.*
import com.piepie.brainhouse.util.SoundManager
import com.piepie.brainhouse.R

@Composable
fun CustomModeScreen(
    onExit: () -> Unit,
    onStartGame: (String, Int, Int) -> Unit, // type, param1, param2
    soundManager: SoundManager? = null
) {
    var selectedGame by remember { mutableStateOf("SCHULTE") } // SCHULTE or BLINDBOX
    var param1 by remember { mutableStateOf(3) } // Grid Size / Box Count
    
    // Limits
    val minParam = if (selectedGame == "SCHULTE") 2 else 3
    val maxParam = if (selectedGame == "SCHULTE") 6 else 12

    Box(modifier = Modifier.fillMaxSize().background(CreamBackground)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
             // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacaronBackButton(onClick = onExit)
                
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "自定义挑战", 
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Game Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GameOptionTab("舒尔特方格", selectedGame == "SCHULTE") { 
                    selectedGame = "SCHULTE"
                    param1 = 3 
                }
                GameOptionTab("盲盒记忆", selectedGame == "BLINDBOX") { 
                    selectedGame = "BLINDBOX"
                    param1 = 4 
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Large Preview Area
            // Use weight to fill available space but respect other elements
            Card(
                modifier = Modifier
                    .weight(1f) // Fill remaining space
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedGame == "SCHULTE") {
                        // Real Grid Preview
                        val gridSize = param1
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridSize),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            userScrollEnabled = false,
                            modifier = Modifier.aspectRatio(1f).fillMaxSize()
                        ) {
                            items(gridSize * gridSize) { index ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .background(MacaronBlue.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                        .border(1.dp, MacaronBlue, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = (240 / gridSize / 2.5).sp, // Dynamic font size adjustment
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    } else {
                        // Blind Box Preview
                        val boxCount = param1
                        // Arrange roughly in a grid
                        val columns = kotlin.math.sqrt(boxCount.toDouble()).toInt().coerceAtLeast(2)
                        
                        // Use a scrollable grid if it overflows, or fit?
                        // User wants to SEE it.
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            userScrollEnabled = true, // Allow scrolling if too many
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(boxCount) {
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .background(MacaronPink, RoundedCornerShape(8.dp))
                                        .shadow(2.dp, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("?", color = Color.White, fontSize = 24.sp)
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Config Controls
            Text(
                if (selectedGame == "SCHULTE") "格子数量: ${param1}x${param1}" else "盒子数量: $param1", 
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                IconButton(
                    onClick = { if (param1 > minParam) param1-- },
                    modifier = Modifier
                        .size(56.dp)
                        .background(MacaronGreen, CircleShape)
                ) {
                    Text("-", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                IconButton(
                    onClick = { if (param1 < maxParam) param1++ },
                    modifier = Modifier
                        .size(56.dp)
                        .background(WarmOrange, CircleShape)
                ) {
                    Text("+", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    soundManager?.playClick()
                    onStartGame(selectedGame, param1, 0)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(8.dp, RoundedCornerShape(32.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = MacaronBlue),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text("开始挑战", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GameOptionTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MacaronBlue else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, 
            color = if (isSelected) Color.White else TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}
