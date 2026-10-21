package com.eezoy.alias

import android.content.Context

/** Категории слов. Рулетка смешивает лёгкие, средние и сложные. */
enum class Category(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val files: List<String>,
) {
    EASY("Лёгкие", "Простые слова для разогрева", "🙂", listOf("easy")),
    MEDIUM("Средние", "Уже придётся подумать", "🤔", listOf("medium")),
    HARD("Сложные", "Для тех, кто знает всё", "🧠", listOf("hard")),
    ROULETTE("Рулетка", "Лёгкие, средние и сложные вперемешку", "🎲", listOf("easy", "medium", "hard")),
    PHRASES("Словосочетания", "Объясни целое выражение", "💬", listOf("phrases")),
}

/**
 * Загружает словари из assets/words и помнит, какие слова уже выпадали.
 * Слово не повторится, пока в его словаре не закончатся все остальные.
 */
class WordRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("used_words", Context.MODE_PRIVATE)
    private val cache = mutableMapOf<String, List<String>>()

    fun words(file: String): List<String> = cache.getOrPut(file) {
        runCatching {
            context.assets.open("words/$file.txt").bufferedReader(Charsets.UTF_8).useLines { lines ->
                lines.map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .distinct()
                    .toList()
            }
        }.getOrDefault(emptyList())
    }

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

    fun total(category: Category): Int = category.files.sumOf { words(it).size }

    fun fresh(category: Category): Int = category.files.sumOf { file ->
        val used = used(file)
        words(file).count { it !in used }
    }

    fun totalUnique(): Int = listOf("easy", "medium", "hard", "phrases").sumOf { words(it).size }
}

/** Колода на одну игру: перемешанные слова, которые ещё не выпадали. */
class WordDeck(private val repo: WordRepository, private val category: Category) {
    private val queue = mutableListOf<Pair<String, String>>()
    private var position = 0

    private fun refill() {
        queue.clear()
        position = 0
        for (file in category.files) {
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
