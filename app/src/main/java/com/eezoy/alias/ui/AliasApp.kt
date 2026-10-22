package com.eezoy.alias.ui

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.eezoy.alias.GameViewModel
import com.eezoy.alias.Screen
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.max

/** Переключить тему; аргумент — точка (центр иконки), откуда разливается новая тема. */
val LocalThemeSwitch = staticCompositionLocalOf<(Offset) -> Unit> { {} }

private val RevealEasing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

@Composable
fun AliasApp(vm: GameViewModel) {
    val layer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    var snapshot by remember { mutableStateOf<ImageBitmap?>(null) }
    var center by remember { mutableStateOf(Offset.Zero) }
    val progress = remember { Animatable(0f) }

    val switchTheme: (Offset) -> Unit = { origin ->
        if (snapshot == null) {
            scope.launch {
                // Снимок старой темы лежит поверх, а в нём растёт круглое «окно»,
                // через которое видно приложение уже в новой теме.
                snapshot = layer.toImageBitmap()
                center = origin
                progress.snapTo(0f)
                vm.toggleTheme()
                progress.animateTo(1f, tween(durationMillis = 750, easing = RevealEasing))
                snapshot = null
            }
        }
    }

    AliasTheme(dark = vm.darkTheme) {
        SystemBarsAppearance(dark = vm.darkTheme, background = Bg)
        CompositionLocalProvider(LocalThemeSwitch provides switchTheme) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            layer.record { this@drawWithContent.drawContent() }
                            drawLayer(layer)
                        }
                        .background(Bg)
                        .systemBarsPadding(),
                ) {
                    Crossfade(targetState = vm.screen, animationSpec = tween(220), label = "screen") { screen ->
                        when (screen) {
                            Screen.MENU -> MenuScreen(vm)
                            Screen.RULES -> RulesScreen(vm)
                            Screen.CATEGORY -> CategoryScreen(vm)
                            Screen.SETUP -> SetupScreen(vm)
                            Screen.READY -> ReadyScreen(vm)
                            Screen.PLAY -> PlayScreen(vm)
                            Screen.REVIEW -> ReviewScreen(vm)
                            Screen.WINNER -> WinnerScreen(vm)
                            Screen.MY_WORDS -> MyWordsScreen(vm)
                        }
                    }
                }

                snapshot?.let { old ->
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            // пока идёт анимация, нажатия не проходят
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) awaitPointerEvent().changes.forEach { it.consume() }
                                }
                            },
                    ) {
                        val maxRadius = hypot(
                            max(center.x, size.width - center.x),
                            max(center.y, size.height - center.y),
                        )
                        val hole = Path().apply { addOval(Rect(center, maxRadius * progress.value)) }
                        clipPath(hole, clipOp = ClipOp.Difference) { drawImage(old) }
                    }
                }
            }
        }
    }
}

/** Тёмные значки статус-бара на светлой теме и светлые — на тёмной. */
@Composable
private fun SystemBarsAppearance(dark: Boolean, background: androidx.compose.ui.graphics.Color) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        // фон окна под цвет темы, чтобы при запуске не мелькал тёмный экран
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(background.toArgb()))
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
}
