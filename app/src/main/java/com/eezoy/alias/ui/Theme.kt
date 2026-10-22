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
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Палитра приложения: у тёмной и светлой темы свои значения. */
@Immutable
data class Palette(
    val dark: Boolean,
    val bg: Color,
    val surface: Color,
    val surfaceHi: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val accent2: Color,
    val good: Color,
    val bad: Color,
    val gold: Color,
    val thumb: Color,
)

val DarkPalette = Palette(
    dark = true,
    bg = Color(0xFF14121F),
    surface = Color(0xFF1F1B2E),
    surfaceHi = Color(0xFF2A2540),
    text = Color(0xFFF4F1FF),
    muted = Color(0xFFA59FC2),
    accent = Color(0xFFFF5C8A),
    accent2 = Color(0xFF7C5CFF),
    good = Color(0xFF35D07F),
    bad = Color(0xFFFF5A5A),
    gold = Color(0xFFFFC94D),
    thumb = Color.White,
)

val LightPalette = Palette(
    dark = false,
    bg = Color(0xFFF5F2FC),
    surface = Color(0xFFFFFFFF),
    surfaceHi = Color(0xFFEAE5F8),
    text = Color(0xFF1D1830),
    muted = Color(0xFF6F6889),
    accent = Color(0xFFF2457A),
    accent2 = Color(0xFF6A4CFF),
    good = Color(0xFF1FB36A),
    bad = Color(0xFFE5484D),
    gold = Color(0xFFC98A00),
    thumb = Color(0xFF6A4CFF),
)

val LocalPalette = staticCompositionLocalOf { DarkPalette }

val Bg: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.bg
val SurfaceC: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.surface
val SurfaceHi: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.surfaceHi
val Accent: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.accent
val Accent2: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.accent2
val Good: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.good
val Bad: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.bad
val Gold: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.gold
val TextMain: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.text
val TextMuted: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.muted
val ThumbC: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.thumb

@Composable
fun AliasTheme(dark: Boolean = true, content: @Composable () -> Unit) {
    val p = if (dark) DarkPalette else LightPalette
    val scheme = if (dark) {
        darkColorScheme(
            primary = p.accent, secondary = p.accent2, background = p.bg, surface = p.surface,
            onPrimary = Color.White, onBackground = p.text, onSurface = p.text,
        )
    } else {
        lightColorScheme(
            primary = p.accent, secondary = p.accent2, background = p.bg, surface = p.surface,
            onPrimary = Color.White, onBackground = p.text, onSurface = p.text,
        )
    }
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

@Composable
fun BigButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textColor: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (color == Color.Unspecified) Accent else color, contentColor = textColor),
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

/**
 * Диалог выхода из идущей игры: выйти с сохранением счёта или сбросить игру.
 * Сброс подтверждается вторым шагом, чтобы счёт не стёрся случайно.
 */
@Composable
fun ExitGameDialog(
    title: String,
    text: String,
    exitLabel: String,
    onExit: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    var resetStep by remember { mutableStateOf(false) }
    if (!resetStep) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = SurfaceC,
            title = { Text(title, color = TextMain, fontWeight = FontWeight.Bold) },
            text = { Text(text, color = TextMuted) },
            confirmButton = {
                TextButton(onClick = onExit) { Text(exitLabel, color = Accent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { resetStep = true }) { Text("Сбросить", color = Bad, fontWeight = FontWeight.SemiBold) }
                    TextButton(onClick = onDismiss) { Text("Отмена", color = TextMuted) }
                }
            },
        )
    } else {
        AlertDialog(
            onDismissRequest = { resetStep = false },
            containerColor = SurfaceC,
            title = { Text("Сбросить игру?", color = TextMain, fontWeight = FontWeight.Bold) },
            text = { Text("Счёт этой игры удалится, продолжить её будет нельзя.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = onReset) { Text("Сбросить", color = Bad, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { resetStep = false }) { Text("Назад", color = TextMuted) }
            },
        )
    }
}
