package com.eezoy.alias.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eezoy.alias.GameViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun PlayScreen(vm: GameViewModel) {
    var confirmExit by remember { mutableStateOf(false) }

    // Экран не гаснет во время раунда.
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    BackHandler {
        if (vm.waitingStart) vm.cancelStart() else if (!vm.paused) vm.togglePause()
    }

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val threshold = with(density) { 100.dp.toPx() }
    val flyDistance = with(density) { 900.dp.toPx() }
    val offset = remember { Animatable(0f) }
    var busy by remember { mutableStateOf(false) }
    val answer by rememberUpdatedState<(Boolean) -> Unit> { vm.answer(it) }

    fun fling(up: Boolean) {
        if (busy || vm.paused || vm.currentWord == null) return
        busy = true
        scope.launch {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            offset.animateTo(if (up) -flyDistance else flyDistance, tween(170))
            answer(up)
            offset.snapTo(0f)
            busy = false
        }
    }

    val word = vm.currentWord
    val seconds = ((vm.remainingMs + 999) / 1000).toInt()
    val guessed = vm.results.count { it.guessed && !it.isLast }
    val skipped = vm.results.count { !it.guessed && !it.isLast }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
        // Верхняя панель: команда, таймер, пауза.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(vm.currentTeam?.name ?: "", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("✓ $guessed   ✕ $skipped", color = TextMuted, fontSize = 14.sp)
            }
            Text(
                if (vm.lastWordPhase) "0" else seconds.toString(),
                color = when {
                    vm.lastWordPhase -> Bad
                    seconds <= 10 -> Bad
                    else -> TextMain
                },
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
            )
            if (!vm.waitingStart) {
                Spacer(Modifier.width(14.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SurfaceHi)
                        .clickable { vm.togglePause() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("❚❚", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        if (vm.lastWordPhase) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Gold.copy(alpha = 0.15f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (vm.lastWordEnabled) "⏰ Время вышло! Последнее слово — отгадать может любая команда"
                    else "⏰ Время вышло! Доиграйте последнее слово",
                    color = Gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
        } else if (vm.waitingStart) {
            Text(
                "Слово появится после старта",
                color = TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            Text(
                "↑ угадано",
                color = Good.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        // Карточка со словом.
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (vm.waitingStart) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(SurfaceC)
                        .clickable { vm.begin() }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🤫", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Объясняет ${vm.currentTeam?.name ?: ""}",
                            color = TextMain,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Возьми телефон так, чтобы экран видел только ты, и жми «Старт»",
                            color = TextMuted,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else if (word != null) {
                val appear = remember(word) { Animatable(0.85f) }
                LaunchedEffect(word) {
                    appear.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                val progress = (offset.value / threshold).coerceIn(-1f, 1f)
                val tint = when {
                    progress < 0 -> lerp(Accent2, Good, -progress)
                    progress > 0 -> lerp(Accent2, Bad, progress)
                    else -> Accent2
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .graphicsLayer {
                            translationY = offset.value
                            val s = appear.value * (1f - abs(progress) * 0.04f)
                            scaleX = s
                            scaleY = s
                            alpha = (1f - abs(offset.value) / flyDistance).coerceIn(0f, 1f)
                        }
                        .clip(RoundedCornerShape(32.dp))
                        .background(Brush.linearGradient(listOf(tint, lerp(tint, Accent, 0.55f))))
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    when {
                                        offset.value < -threshold -> fling(true)
                                        offset.value > threshold -> fling(false)
                                        else -> scope.launch { offset.animateTo(0f, spring()) }
                                    }
                                },
                                onDragCancel = { scope.launch { offset.animateTo(0f, spring()) } },
                                onVerticalDrag = { change, amount ->
                                    if (!busy && !vm.paused) {
                                        change.consume()
                                        scope.launch { offset.snapTo(offset.value + amount) }
                                    }
                                },
                            )
                        }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (vm.paused) {
                        Text("Пауза", color = Color.White.copy(alpha = 0.8f), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            word,
                            color = Color.White,
                            fontSize = wordFontSize(word),
                            lineHeight = wordFontSize(word) * 1.15f,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        if (!vm.lastWordPhase && !vm.waitingStart) {
            Text(
                "↓ пропустить",
                color = Bad.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        Spacer(Modifier.height(10.dp))

        // Запасные кнопки, если неудобно смахивать.
        if (vm.waitingStart) {
            BigButton("Старт", color = Good) { vm.begin() }
        } else Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BigButton(if (vm.lastWordPhase) "Не отгадали" else "Пропуск", Modifier.weight(1f), color = SurfaceHi, textColor = TextMain) { fling(false) }
            BigButton(if (vm.lastWordPhase) "Отгадали" else "Угадано", Modifier.weight(1f), color = Good) { fling(true) }
        }
    }

    if (vm.paused) {
        PauseOverlay(
            onResume = { vm.togglePause() },
            onExit = { confirmExit = true },
        )
    }
    if (confirmExit) {
        ExitGameDialog(
            title = "Выйти в меню?",
            text = "Счёт сохранится: игру можно будет продолжить из главного меню. Текущий раунд не засчитается.",
            exitLabel = "Выйти",
            onExit = {
                confirmExit = false
                vm.endGame()
            },
            onReset = {
                confirmExit = false
                vm.abandonGame()
            },
            onDismiss = { confirmExit = false },
        )
    }
}

private fun wordFontSize(word: String) = when {
    word.length <= 8 -> 44.sp
    word.length <= 13 -> 36.sp
    word.length <= 20 -> 30.sp
    else -> 26.sp
}

@Composable
private fun PauseOverlay(onResume: () -> Unit, onExit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg.copy(alpha = 0.92f))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Пауза", color = TextMain, fontSize = 40.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text("Слово скрыто, таймер остановлен", color = TextMuted, fontSize = 15.sp)
            Spacer(Modifier.height(32.dp))
            BigButton("Продолжить", onClick = onResume)
            Spacer(Modifier.height(12.dp))
            GhostButton("Выйти в меню", onClick = onExit)
        }
    }
}
