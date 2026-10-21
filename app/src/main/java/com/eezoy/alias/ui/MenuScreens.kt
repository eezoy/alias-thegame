package com.eezoy.alias.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eezoy.alias.Category
import com.eezoy.alias.GameViewModel
import com.eezoy.alias.Screen

@Composable
fun MenuScreen(vm: GameViewModel) {
    var confirmReset by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    val total = remember(refresh) { vm.repo.totalUnique() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Text(
            "ALIAS",
            style = TextStyle(
                brush = Brush.linearGradient(listOf(Accent2, Accent)),
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
            ),
        )
        Text(
            "Объясняй слова, не называя их",
            color = TextMuted,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        BigButton("Играть") { vm.go(Screen.CATEGORY) }
        Spacer(Modifier.height(12.dp))
        GhostButton("✍️ Мои слова") { vm.go(Screen.MY_WORDS) }
        Spacer(Modifier.height(12.dp))
        GhostButton("Правила") { vm.go(Screen.RULES) }
        Spacer(Modifier.height(12.dp))
        GhostButton("Сбросить историю слов") { confirmReset = true }
        Spacer(Modifier.height(20.dp))
        Text("В словарях $total слов и выражений", color = TextMuted, fontSize = 13.sp)
    }

    if (confirmReset) {
        ConfirmDialog(
            title = "Сбросить историю?",
            text = "Игра запоминает выпавшие слова и не повторяет их, пока не покажет все остальные. После сброса слова снова пойдут с нуля.",
            confirm = "Сбросить",
            onConfirm = {
                vm.repo.resetAll()
                refresh++
                confirmReset = false
            },
            onDismiss = { confirmReset = false },
        )
    }
}

@Composable
fun RulesScreen(vm: GameViewModel) {
    BackHandler { vm.go(Screen.MENU) }
    Column(Modifier.fillMaxSize()) {
        Header("Правила") { vm.go(Screen.MENU) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Rule("🗣", "Один игрок объясняет слова своей команде, не называя само слово и однокоренные.")
            Rule("⬆️", "Команда отгадала — смахни карточку вверх. +1 очко.")
            Rule("⬇️", "Не получается — смахни вниз, слово пропускается. Если включён штраф, это −1 очко.")
            Rule("⏱", "Раунд идёт на время. Когда время выйдет, можно доиграть последнее слово: его может отгадать любая команда, и очко достаётся ей.")
            Rule("✏️", "После раунда можно поправить итоги, если кто-то случайно смахнул не туда.")
            Rule("🏆", "Побеждает команда, первой набравшая нужное число очков. Победитель определяется, когда все команды сделали одинаковое число ходов.")
            Rule("🔁", "Игра помнит выпавшие слова и не повторяет их, пока не покажет весь словарь.")
            Rule("✍️", "В разделе «Мои слова» можно добавить свои слова. Играть ими можно отдельно или подмешать в «Рулетку».")
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Rule(icon: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceC)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 26.sp)
        Spacer(Modifier.width(14.dp))
        Text(text, color = TextMain, fontSize = 16.sp, lineHeight = 22.sp)
    }
}

@Composable
fun CategoryScreen(vm: GameViewModel) {
    BackHandler { vm.go(Screen.MENU) }
    Column(Modifier.fillMaxSize()) {
        Header("Выбери слова") { vm.go(Screen.MENU) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Category.entries.forEach { c ->
                CategoryCard(vm, c) {
                    // Пустую категорию «Мои слова» сначала нужно наполнить.
                    if (c == Category.MINE && vm.repo.customWords().isEmpty()) vm.go(Screen.MY_WORDS)
                    else vm.selectCategory(c)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CategoryCard(vm: GameViewModel, c: Category, onClick: () -> Unit) {
    val total = remember(c) { vm.repo.total(c) }
    val fresh = remember(c) { vm.repo.fresh(c) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceC)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceHi),
            contentAlignment = Alignment.Center,
        ) {
            Text(c.emoji, fontSize = 28.sp)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(c.title, color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(c.subtitle, color = TextMuted, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                if (c == Category.MINE && total == 0) "Пока пусто — нажми, чтобы добавить"
                else "$total слов · новых: $fresh",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
