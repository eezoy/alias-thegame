package com.eezoy.alias.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Bg = Color(0xFF14121F)
val SurfaceC = Color(0xFF1F1B2E)
val SurfaceHi = Color(0xFF2A2540)
val Accent = Color(0xFFFF5C8A)
val Accent2 = Color(0xFF7C5CFF)
val Good = Color(0xFF35D07F)
val Bad = Color(0xFFFF5A5A)
val Gold = Color(0xFFFFC94D)
val TextMain = Color(0xFFF4F1FF)
val TextMuted = Color(0xFFA59FC2)

@Composable
fun AliasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            secondary = Accent2,
            background = Bg,
            surface = SurfaceC,
            onPrimary = Color.White,
            onBackground = TextMain,
            onSurface = TextMain,
        ),
        content = content,
    )
}

@Composable
fun BigButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Accent,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
    ) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GhostButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMain),
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Шапка экрана с кнопкой «назад». */
@Composable
fun Header(title: String, onBack: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Text("←", fontSize = 26.sp, color = TextMain)
            }
            Spacer(Modifier.width(4.dp))
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
    }
}

/** Небольшая выбираемая плашка (время раунда, очки до победы). */
@Composable
fun Pill(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Accent2 else SurfaceHi)
            .border(1.dp, if (selected) Accent2 else SurfaceHi, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text,
            color = if (selected) Color.White else TextMuted,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp,
        )
    }
}

@Composable
fun PillRow(content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirm: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceC,
        title = { Text(title, color = TextMain, fontWeight = FontWeight.Bold) },
        text = { Text(text, color = TextMuted) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirm, color = Accent, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = TextMuted) }
        },
    )
}
