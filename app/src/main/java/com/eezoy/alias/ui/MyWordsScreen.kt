package com.eezoy.alias.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eezoy.alias.AddResult
import com.eezoy.alias.Category
import com.eezoy.alias.GameViewModel
import com.eezoy.alias.Screen

@Composable
fun MyWordsScreen(vm: GameViewModel) {
    BackHandler { vm.go(Screen.MENU) }
    val repo = vm.repo
    val words = remember { mutableStateListOf<String>().apply { addAll(repo.customWords().reversed()) } }
    var input by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<Pair<String, Color>?>(null) }
    var mix by remember { mutableStateOf(repo.mixCustomIntoRoulette) }
    var confirmClear by remember { mutableStateOf(false) }

    fun reload() {
        words.clear()
        words.addAll(repo.customWords().reversed())
    }

    fun submit() {
        if (input.isBlank()) return
        val r = repo.addCustom(input)
        input = ""
        reload()
        message = describe(r)
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        Header("✍️ Мои слова") { vm.go(Screen.MENU) }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceC)
                        .padding(16.dp),
                ) {
                    Text(
                        "Впиши слово или сразу несколько через запятую. Можно вставить готовый список, где каждое слово с новой строки.",
                        color = TextMuted,
                        fontSize = 14.sp,
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("например: кот Борис, наш подъезд", color = TextMuted) },
                        minLines = 1,
                        maxLines = 5,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent2,
                            unfocusedBorderColor = SurfaceHi,
                            focusedContainerColor = Bg,
                            unfocusedContainerColor = Bg,
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            cursorColor = Accent,
                        ),
                    )
                    Spacer(Modifier.height(10.dp))
                    BigButton("Добавить", color = Accent2, enabled = input.isNotBlank()) { submit() }
                    message?.let { (text, color) ->
                        Spacer(Modifier.height(10.dp))
                        Text(text, color = color, fontSize = 14.sp, lineHeight = 19.sp)
                    }
                }
            }
            item {
                ToggleRow(
                    title = "Подмешивать в «Рулетку»",
                    subtitle = "Твои слова будут попадаться среди остальных",
                    checked = mix,
                ) {
                    mix = it
                    repo.mixCustomIntoRoulette = it
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (words.isEmpty()) "Пока ни одного слова" else "Добавлено: ${words.size}",
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    if (words.isNotEmpty()) {
                        Text(
                            "Удалить все",
                            color = Bad,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { confirmClear = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
            items(words, key = { it }) { word ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceC)
                        .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(word, color = TextMain, fontSize = 17.sp, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                repo.removeCustom(word)
                                reload()
                                message = null
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✕", color = TextMuted, fontSize = 16.sp)
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
        if (words.isNotEmpty()) {
            Box(Modifier.padding(20.dp)) {
                BigButton("Играть моими словами") { vm.selectCategory(Category.MINE) }
            }
        }
    }

    if (confirmClear) {
        ConfirmDialog(
            title = "Удалить все свои слова?",
            text = "Список очистится полностью. Встроенные словари останутся.",
            confirm = "Удалить",
            onConfirm = {
                repo.clearCustom()
                reload()
                message = null
                confirmClear = false
            },
            onDismiss = { confirmClear = false },
        )
    }
}

private fun describe(r: AddResult): Pair<String, Color> {
    val lines = mutableListOf<String>()
    if (r.added.isNotEmpty()) lines.add("✓ Добавлено: ${r.added.size}")
    if (r.alreadyMine.isNotEmpty()) lines.add("Уже есть в твоих словах: ${r.alreadyMine.joinToString(", ")}")
    r.inDictionary.forEach { (w, cat) -> lines.add("«$w» уже есть в категории «$cat»") }
    if (lines.isEmpty()) lines.add("Нечего добавлять")
    val color = if (r.added.isNotEmpty() && r.alreadyMine.isEmpty() && r.inDictionary.isEmpty()) Good else Gold
    return lines.joinToString("\n") to color
}
