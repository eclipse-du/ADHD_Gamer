package com.piepie.brainhouse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piepie.brainhouse.ui.theme.TextPrimary

@Composable
fun MacaronBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .shadow(4.dp, CircleShape)
            .background(Color.White, CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "←", 
                fontSize = 28.sp, 
                fontWeight = FontWeight.Bold, 
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}
