package com.eezoy.alias.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eezoy.alias.GameViewModel
import com.eezoy.alias.Screen

@Composable
fun AliasApp(vm: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
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
}
