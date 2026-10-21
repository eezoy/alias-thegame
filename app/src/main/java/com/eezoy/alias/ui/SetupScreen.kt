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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eezoy.alias.GameViewModel
import com.eezoy.alias.Screen


@Composable
fun SetupScreen(vm: GameViewModel) {
    BackHandler { vm.go(Screen.CATEGORY) }
    Column(Modifier.fillMaxSize().imePadding()) {
        Header("${vm.category.emoji} ${vm.category.title}") { vm.go(Screen.CATEGORY) }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionTitle("Команды")
            vm.teams.forEachIndexed { index, team ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = team.name,
                        onValueChange = { if (it.length <= 24) team.name = it },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done,
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent2,
                            unfocusedBorderColor = SurfaceHi,
                            focusedContainerColor = SurfaceC,
                            unfocusedContainerColor = SurfaceC,
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            cursorColor = Accent,
                        ),
                    )
                    if (vm.teams.size > 2) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SurfaceHi)
                                .clickable { vm.removeTeam(index) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("✕", color = Bad, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (vm.teams.size < 6) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceC)
                        .clickable { vm.addTeam() }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+ Добавить команду", color = Accent2, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
            ValueSlider(
                title = "Время раунда",
                value = vm.roundSeconds,
                range = 15..120,
                step = 5,
                format = { "$it сек" },
            ) { vm.roundSeconds = it }
            ValueSlider(
                title = "Очков до победы",
                value = vm.targetScore,
                range = 20..100,
                step = 5,
                format = { "$it" },
            ) { vm.targetScore = it }

            Spacer(Modifier.height(4.dp))
            ToggleRow(
                title = "Штраф за пропуск",
                subtitle = "Каждое пропущенное слово отнимает 1 очко",
                checked = vm.skipPenalty,
            ) { vm.skipPenalty = it }
            ToggleRow(
                title = "Последнее общее слово",
                subtitle = "Последнее слово, которое доигрывают после конца времени, может отгадать любая команда",
                checked = vm.lastWordEnabled,
            ) { vm.lastWordEnabled = it }
            Spacer(Modifier.height(12.dp))
        }
        Box(Modifier.padding(20.dp)) {
            BigButton("Начать игру") { vm.startGame() }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = TextMuted,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
    )
}

@Composable
fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceC)
            .clickable { onChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextMuted, fontSize = 13.sp)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Accent2,
                uncheckedTrackColor = SurfaceHi,
                uncheckedThumbColor = TextMuted,
                uncheckedBorderColor = SurfaceHi,
            ),
        )
    }
}

/** Ползунок с подписью и текущим значением, шаг задаётся явно. */
@Composable
private fun ValueSlider(
    title: String,
    value: Int,
    range: IntRange,
    step: Int,
    format: (Int) -> String,
    onChange: (Int) -> Unit,
) {
    val snapped = (((value.coerceIn(range.first, range.last) - range.first + step / 2) / step) * step + range.first)
        .coerceIn(range.first, range.last)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceC)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(format(snapped), color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        Slider(
            value = snapped.toFloat(),
            onValueChange = { v ->
                val n = (((v - range.first) / step).let { kotlin.math.round(it) }.toInt() * step + range.first)
                    .coerceIn(range.first, range.last)
                if (n != snapped) onChange(n)
            },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first) / step - 1,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Accent2,
                inactiveTrackColor = SurfaceHi,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
        )
        Row {
            Text(format(range.first), color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text(format(range.last), color = TextMuted, fontSize = 12.sp)
        }
    }
}
