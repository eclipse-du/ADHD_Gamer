package com.piepie.brainhouse.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piepie.brainhouse.data.Honor
import com.piepie.brainhouse.ui.theme.*
import com.piepie.brainhouse.util.SoundManager

@Composable
fun HallOfFameScreen(
    onExit: () -> Unit,
    viewModel: GameDataViewModel = viewModel(),
    soundManager: SoundManager? = null
) {
    val honors by viewModel.allHonors.collectAsState()
    var selectedHonor by remember { mutableStateOf<Honor?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(CreamBackground)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacaronBackButton(onClick = onExit)
                
                Text(
                    "荣誉墙", 
                    style = MaterialTheme.typography.titleLarge, 
                    color = TextPrimary,
                    fontSize = 28.sp
                )
                
                // Reset Button
                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmOrange)
                ) {
                    Text("重置", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sticker Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(honors) { honor ->
                    HonorItem(honor) {
                        soundManager?.playClick()
                        if (honor.isUnlocked) {
                             selectedHonor = honor
                             soundManager?.speak(honor.name)
                        } else {
                             // Show locked hint?
                             // soundManager?.playError()
                        }
                    }
                }
            }
        }
        
        // Detail Dialog
        if (selectedHonor != null) {
            HonorDetailDialog(honor = selectedHonor!!) {
                selectedHonor = null
            }
        }
        
        // Reset Confirmation Dialog
        if (showResetDialog) {
             AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("重置所有荣誉？") },
                text = { Text("确定要清空所有记录和勋章吗？此操作无法撤销。") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetHonors()
                            showResetDialog = false
                            soundManager?.speak("记录已清空")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("确定重置")
                    }
                },
                dismissButton = {
                    Button(onClick = { showResetDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
fun HonorItem(honor: Honor, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.aspectRatio(1f).clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (honor.isUnlocked) {
                 Image(
                     painter = painterResource(id = honor.iconResId),
                     contentDescription = honor.name,
                     modifier = Modifier.padding(12.dp).fillMaxSize(),
                     contentScale = ContentScale.Fit
                 )
            } else {
                 // Locked Silhouette
                 Image(
                     painter = painterResource(id = honor.iconResId),
                     contentDescription = "Locked",
                     modifier = Modifier.padding(12.dp).fillMaxSize().alpha(0.2f),
                     contentScale = ContentScale.Fit,
                     colorFilter = ColorFilter.tint(Color.Black)
                 )
                 Text("?", fontSize = 40.sp, color = Color.Gray.copy(alpha=0.5f), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HonorDetailDialog(honor: Honor, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = MacaronBlue)) { 
                Text("关闭", color = Color.White) 
            }
        }, // Removed title param usage here if it causes issues, but title is standard
        title = {
            Text(honor.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 24.sp)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = honor.iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(160.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(honor.description, fontSize = 18.sp, color = TextSecondary, textAlign = TextAlign.Center)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
