package com.eezoy.alias.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Кнопка смены темы: в тёмной теме — месяц, в светлой — солнце.
 * Нажатие разливает новую тему кругом из центра кнопки.
 */
@Composable
fun ThemeToggle(dark: Boolean, modifier: Modifier = Modifier) {
    val switch = LocalThemeSwitch.current
    var center by remember { mutableStateOf(Offset.Zero) }
    val turn by animateFloatAsState(
        targetValue = if (dark) 0f else 180f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "turn",
    )
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(SurfaceC)
            .onGloballyPositioned { center = it.boundsInRoot().center }
            .clickable { switch(center) }
            .semantics { contentDescription = if (dark) "Включить светлую тему" else "Включить тёмную тему" },
        contentAlignment = Alignment.Center,
    ) {
        val color = TextMain
        Crossfade(targetState = dark, animationSpec = tween(350), label = "icon") { isDark ->
            Canvas(
                Modifier
                    .size(24.dp)
                    .rotate(turn)
                    .scale(if (isDark) 1f else 1.05f),
            ) {
                if (isDark) drawMoon(color) else drawSun(color)
            }
        }
    }
}

/** Месяц: круг, из которого вырезан смещённый круг. */
private fun DrawScope.drawMoon(color: Color) {
    val r = size.minDimension * 0.42f
    val c = Offset(size.width / 2f, size.height / 2f)
    val body = Path().apply { addOval(Rect(c, r)) }
    val bite = Path().apply { addOval(Rect(Offset(c.x + r * 0.55f, c.y - r * 0.45f), r * 0.82f)) }
    val crescent = Path().apply { op(body, bite, PathOperation.Difference) }
    drawPath(crescent, color)
}

/** Солнце: круг и восемь лучей. */
private fun DrawScope.drawSun(color: Color) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val s = size.minDimension
    drawCircle(color, radius = s * 0.2f, center = c)
    val stroke = s * 0.085f
    for (i in 0 until 8) {
        val a = Math.toRadians(i * 45.0)
        val from = Offset(c.x + (s * 0.32f) * cos(a).toFloat(), c.y + (s * 0.32f) * sin(a).toFloat())
        val to = Offset(c.x + (s * 0.46f) * cos(a).toFloat(), c.y + (s * 0.46f) * sin(a).toFloat())
        drawLine(color, from, to, strokeWidth = stroke, cap = StrokeCap.Round)
    }
}
