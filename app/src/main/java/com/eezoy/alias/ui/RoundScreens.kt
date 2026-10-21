package com.eezoy.alias.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eezoy.alias.GameViewModel
import com.eezoy.alias.Screen

/** Экран перед ходом: чья очередь и общий счёт. */
@Composable
fun ReadyScreen(vm: GameViewModel) {
    var confirmExit by remember { mutableStateOf(false) }
    BackHandler { confirmExit = true }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Раунд ${vm.roundNumber}",
                color = TextMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                "до ${vm.targetScore} очков",
                color = TextMuted,
                fontSize = 14.sp,
            )
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceHi)
                    .clickable { confirmExit = true },
                contentAlignment = Alignment.Center,
            ) {
                Text("✕", color = TextMuted, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Ход команды", color = TextMuted, fontSize = 18.sp)
        Text(
            vm.currentTeam?.name ?: "",
            color = TextMain,
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 44.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "${vm.category.emoji} ${vm.category.title} · ${vm.roundSeconds} сек",
            color = Accent,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(24.dp))
        Scoreboard(vm, highlight = vm.currentTeamIndex, modifier = Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        Text(
            "Передай телефон объясняющему. Смахивай вверх, если угадали, и вниз, чтобы пропустить.",
            color = TextMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))
        BigButton("Поехали!") { vm.startRound() }
    }

    if (confirmExit) {
        ConfirmDialog(
            title = "Выйти в меню?",
            text = "Счёт этой игры не сохранится.",
            confirm = "Выйти",
            onConfirm = {
                confirmExit = false
                vm.endGame()
            },
            onDismiss = { confirmExit = false },
        )
    }
}

@Composable
fun Scoreboard(vm: GameViewModel, highlight: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        vm.teams.forEachIndexed { i, team ->
            val active = i == highlight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (active) Accent2.copy(alpha = 0.22f) else SurfaceC)
                    .border(1.5.dp, if (active) Accent2 else Color.Transparent, RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    team.name,
                    color = TextMain,
                    fontSize = 18.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                Text(team.score.toString(), color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

/** Итоги раунда: можно поправить ошибки перед подсчётом. */
@Composable
fun ReviewScreen(vm: GameViewModel) {
    BackHandler { /* итоги нужно подтвердить */ }
    val points = vm.roundPoints()

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text("Итоги раунда", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text(
            "Нажми на слово, чтобы исправить результат",
            color = TextMuted,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(14.dp))
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (vm.results.isEmpty()) {
                Text("Ни одного слова за раунд 🙃", color = TextMuted, fontSize = 16.sp, modifier = Modifier.padding(8.dp))
            }
            vm.results.forEachIndexed { index, r ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceC)
                        .clickable { vm.toggleResult(index) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (r.guessed) Good else Bad),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(if (r.guessed) "✓" else "✕", color = Color.White, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(r.word, color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            if (r.isLast) {
                                Text("последнее слово", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (r.isLast && r.guessed && vm.lastWordEnabled && vm.teams.size > 1) {
                        Spacer(Modifier.height(10.dp))
                        Text("Кто отгадал?", color = TextMuted, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState())) {
                            PillRow {
                                vm.teams.forEachIndexed { ti, t ->
                                    Pill(t.name, r.lastTeam == ti) { vm.setLastWordTeam(index, ti) }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceHi)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(vm.currentTeam?.name ?: "", color = TextMain, fontSize = 17.sp, modifier = Modifier.weight(1f), maxLines = 1)
            Text(
                (if (points > 0) "+" else "") + points,
                color = if (points >= 0) Good else Bad,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
            )
        }
        Spacer(Modifier.height(12.dp))
        BigButton("Дальше") { vm.confirmRound() }
    }
}

@Composable
fun WinnerScreen(vm: GameViewModel) {
    BackHandler { vm.endGame() }
    val sorted = vm.teams.sortedByDescending { it.score }
    val winner = sorted.firstOrNull()

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Text("🏆", fontSize = 80.sp)
        Text("Победа!", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            winner?.name ?: "",
            color = TextMain,
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 44.sp,
        )
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            sorted.forEachIndexed { place, team ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (place == 0) Gold.copy(alpha = 0.18f) else SurfaceC)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${place + 1}.", color = TextMuted, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    Text(team.name, color = TextMain, fontSize = 18.sp, modifier = Modifier.weight(1f), maxLines = 1)
                    Text(team.score.toString(), color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        BigButton("Реванш") { vm.rematch() }
        Spacer(Modifier.height(10.dp))
        GhostButton("В меню") { vm.go(Screen.MENU) }
    }
}
