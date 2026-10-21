package com.eezoy.alias

import android.content.Context

/** Категории слов. Рулетка смешивает лёгкие, средние, сложные и чёрный юмор (и, по желанию, свои слова). */
enum class Category(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val files: List<String>,
) {
    EASY("Лёгкие", "Простые слова для разогрева", "🙂", listOf("easy")),
    MEDIUM("Средние", "Уже придётся подумать", "🤔", listOf("medium")),
    HARD("Сложные", "Для тех, кто знает всё", "🧠", listOf("hard")),
    ROULETTE("Рулетка", "Лёгкие, средние, сложные и чёрный юмор вперемешку", "🎲", listOf("easy", "medium", "hard", "dark")),
    PHRASES("Словосочетания", "Объясни целое выражение", "💬", listOf("phrases")),
    DARK("Чёрный юмор", "Смерть, ад, хорроры и офисная безнадёга", "🖤", listOf("dark")),
    MINE("Мои слова", "Слова, которые вы добавили сами", "✍️", listOf(CUSTOM)),
}

const val CUSTOM = "custom"

/** Встроенные словари и их названия для подсказок. */
val BUILT_IN = linkedMapOf(
    "easy" to "Лёгкие",
    "medium" to "Средние",
    "hard" to "Сложные",
    "phrases" to "Словосочетания",
    "dark" to "Чёрный юмор",
)

fun normalizeWord(w: String) = w.trim().lowercase().replace('ё', 'е').replace(Regex("\\s+"), " ")

/** Итог добавления своих слов. */
data class AddResult(
    val added: List<String>,
    val alreadyMine: List<String>,
    val inDictionary: List<Pair<String, String>>,
)

/**
 * Загружает словари из assets/words, хранит свои слова пользователя
 * и помнит, какие слова уже выпадали. Слово не повторится, пока в его
 * словаре не закончатся все остальные.
 */
class WordRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("used_words", Context.MODE_PRIVATE)
    private val customPrefs = context.getSharedPreferences("custom_words", Context.MODE_PRIVATE)
    private val cache = mutableMapOf<String, List<String>>()
    private val normCache = mutableMapOf<String, Set<String>>()

    fun words(file: String): List<String> {
        if (file == CUSTOM) return customWords()
        return cache.getOrPut(file) {
            runCatching {
                context.assets.open("words/$file.txt").bufferedReader(Charsets.UTF_8).useLines { lines ->
                    lines.map { it.trim() }
                        .filter { it.isNotEmpty() && !it.startsWith("#") }
                        .distinct()
                        .toList()
                }
            }.getOrDefault(emptyList())
        }
    }

    // ---- Свои слова ----

    fun customWords(): List<String> =
        (customPrefs.getString("list", "") ?: "")
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    private fun saveCustom(list: List<String>) {
        customPrefs.edit().putString("list", list.joinToString("\n")).apply()
    }

    var mixCustomIntoRoulette: Boolean
        get() = customPrefs.getBoolean("mix", true)
        set(value) {
            customPrefs.edit().putBoolean("mix", value).apply()
        }

    /** В каком встроенном словаре уже есть слово (название категории) или null. */
    fun builtInCategoryOf(word: String): String? {
        val n = normalizeWord(word)
        for ((file, title) in BUILT_IN) {
            val set = normCache.getOrPut(file) { words(file).map(::normalizeWord).toHashSet() }
            if (n in set) return title
        }
        return null
    }

    /** Добавляет слова. Можно передать несколько через запятую, точку с запятой или с новой строки. */
    fun addCustom(raw: String): AddResult {
        val parts = raw.split('\n', ',', ';')
            .map { it.trim().replace(Regex("\\s+"), " ") }
            .filter { it.isNotEmpty() && it.length <= 60 }
        val current = customWords().toMutableList()
        val mine = current.map(::normalizeWord).toHashSet()
        val added = mutableListOf<String>()
        val already = mutableListOf<String>()
        val inDict = mutableListOf<Pair<String, String>>()
        for (p in parts) {
            val n = normalizeWord(p)
            when {
                n in mine -> already.add(p)
                builtInCategoryOf(p) != null -> inDict.add(p to builtInCategoryOf(p)!!)
                else -> {
                    current.add(p)
                    mine.add(n)
                    added.add(p)
                }
            }
        }
        if (added.isNotEmpty()) saveCustom(current)
        return AddResult(added, already, inDict)
    }

    fun removeCustom(word: String) {
        saveCustom(customWords().filter { it != word })
    }

    fun clearCustom() {
        saveCustom(emptyList())
        reset(CUSTOM)
    }

    // ---- Какие файлы участвуют в категории ----

    fun filesFor(category: Category): List<String> =
        if (category == Category.ROULETTE && mixCustomIntoRoulette) category.files + CUSTOM else category.files

    // ---- История выпавших слов ----

    fun used(file: String): Set<String> = prefs.getStringSet(file, emptySet()) ?: emptySet()

    fun markUsed(file: String, word: String) {
        val updated = HashSet(used(file))
        updated.add(word)
        prefs.edit().putStringSet(file, updated).apply()
    }

    fun reset(file: String) {
        prefs.edit().remove(file).apply()
    }

    fun resetAll() {
        prefs.edit().clear().apply()
    }

    fun total(category: Category): Int = filesFor(category).sumOf { words(it).size }

    fun fresh(category: Category): Int = filesFor(category).sumOf { file ->
        val used = used(file)
        words(file).count { it !in used }
    }

    fun totalUnique(): Int = BUILT_IN.keys.sumOf { words(it).size } + customWords().size
}

/** Колода на одну игру: перемешанные слова, которые ещё не выпадали. */
class WordDeck(private val repo: WordRepository, private val category: Category) {
    private val queue = mutableListOf<Pair<String, String>>()
    private var position = 0

    private fun refill() {
        queue.clear()
        position = 0
        for (file in repo.filesFor(category)) {
            val all = repo.words(file)
            var used = repo.used(file)
            if (all.isNotEmpty() && all.all { it in used }) {
                repo.reset(file)
                used = emptySet()
            }
            all.filter { it !in used }.forEach { queue.add(file to it) }
        }
        queue.shuffle()
    }

    fun next(): String {
        if (position >= queue.size) refill()
        if (queue.isEmpty()) return "Слова закончились"
        val (file, word) = queue[position++]
        repo.markUsed(file, word)
        return word
    }
}
