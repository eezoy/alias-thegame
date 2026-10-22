package com.eezoy.alias

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Screen { MENU, CATEGORY, SETUP, READY, PLAY, REVIEW, WINNER, RULES, MY_WORDS }

class Team(name: String, score: Int = 0) {
    var name by mutableStateOf(name)
    var score by mutableIntStateOf(score)
}

/** Итог одного слова в раунде. lastTeam — кто отгадал последнее (общее) слово. */
data class WordResult(
    val word: String,
    val guessed: Boolean,
    val isLast: Boolean = false,
    val lastTeam: Int = -1,
)

private val FUNNY_TEAM_NAMES = listOf(
    "Котики", "Пельмени", "Душнилы", "Сигмы", "Кринжули", "Зумеры", "Бумеры",
    "Шаурмисты", "Ленивцы", "Гении", "Тапочки", "Енотики", "Вайбики", "Скуфы",
    "Альтушки", "Чебуреки", "Пингвины", "Мемологи",
)

/** Краткие сведения о незаконченной игре для главного меню. */
data class SavedGameInfo(
    val category: Category,
    val teams: List<Pair<String, Int>>,
    val round: Int,
    val nextTeam: String,
    val target: Int,
)

class GameViewModel(app: Application) : AndroidViewModel(app) {
    val repo = WordRepository(app)
    private val prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val savePrefs = app.getSharedPreferences("saved_game", Context.MODE_PRIVATE)

    // ---- Тема ----
    var darkTheme by mutableStateOf(prefs.getBoolean("darkTheme", true))
        private set

    fun toggleTheme() {
        darkTheme = !darkTheme
        prefs.edit().putBoolean("darkTheme", darkTheme).apply()
    }

    /** Незаконченная игра, если есть. */
    var savedGame by mutableStateOf<SavedGameInfo?>(null)
        private set

    var screen by mutableStateOf(Screen.MENU)
        private set

    // ---- Настройки ----
    var category by mutableStateOf(Category.ROULETTE)
        private set
    var roundSeconds by mutableIntStateOf(prefs.getInt("roundSeconds", 60))
    var targetScore by mutableIntStateOf(prefs.getInt("targetScore", 50))
    var skipPenalty by mutableStateOf(prefs.getBoolean("skipPenalty", true))
    var lastWordEnabled by mutableStateOf(prefs.getBoolean("lastWord", true))

    val teams = mutableStateListOf<Team>()

    // ---- Состояние игры ----
    var turnIndex by mutableIntStateOf(0)
        private set
    val currentTeamIndex: Int get() = if (teams.isEmpty()) 0 else turnIndex % teams.size
    val currentTeam: Team? get() = teams.getOrNull(currentTeamIndex)
    val roundNumber: Int get() = turnIndex / teams.size.coerceAtLeast(1) + 1

    var currentWord by mutableStateOf<String?>(null)
        private set
    var remainingMs by mutableLongStateOf(0L)
        private set
    var paused by mutableStateOf(false)
        private set
    var lastWordPhase by mutableStateOf(false)
        private set

    /** Раунд открыт, но таймер ещё не запущен: ждём кнопку «Старт». */
    var waitingStart by mutableStateOf(false)
        private set
    val results = mutableStateListOf<WordResult>()

    private var deck: WordDeck? = null
    private var timerJob: Job? = null
    private val tone: ToneGenerator? = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }.getOrNull()

    init {
        val saved = prefs.getString("teams", null)
            ?.split("\n")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
        val names = if (saved != null && saved.size >= 2) saved else FUNNY_TEAM_NAMES.shuffled().take(2)
        names.forEach { teams.add(Team(it)) }
        savedGame = readSave()
    }

    // ---- Сохранение незаконченной игры ----
    private fun writeSave() {
        savePrefs.edit()
            .putBoolean("active", true)
            .putString("category", category.name)
            .putInt("roundSeconds", roundSeconds)
            .putInt("targetScore", targetScore)
            .putBoolean("skipPenalty", skipPenalty)
            .putBoolean("lastWord", lastWordEnabled)
            .putInt("turnIndex", turnIndex)
            .putString("teams", teams.joinToString("\n") { "${it.score}\t${it.name.replace("\n", " ").replace("\t", " ")}" })
            .apply()
        savedGame = readSave()
    }

    private fun clearSave() {
        savePrefs.edit().clear().apply()
        savedGame = null
    }

    private fun readTeams(): List<Pair<String, Int>> =
        (savePrefs.getString("teams", "") ?: "").split("\n").mapNotNull { line ->
            val tab = line.indexOf('\t')
            if (tab <= 0) null else (line.substring(tab + 1) to (line.substring(0, tab).toIntOrNull() ?: 0))
        }

    private fun readSave(): SavedGameInfo? {
        if (!savePrefs.getBoolean("active", false)) return null
        val t = readTeams()
        if (t.size < 2) return null
        val cat = runCatching { Category.valueOf(savePrefs.getString("category", "") ?: "") }.getOrNull() ?: return null
        val turn = savePrefs.getInt("turnIndex", 0)
        return SavedGameInfo(cat, t, turn / t.size + 1, t[turn % t.size].first, savePrefs.getInt("targetScore", 50))
    }

    /** Продолжить незаконченную игру с того хода, на котором она остановилась. */
    fun continueGame() {
        val t = readTeams()
        if (savedGame == null || t.size < 2) return
        category = savedGame!!.category
        roundSeconds = savePrefs.getInt("roundSeconds", roundSeconds)
        targetScore = savePrefs.getInt("targetScore", targetScore)
        skipPenalty = savePrefs.getBoolean("skipPenalty", skipPenalty)
        lastWordEnabled = savePrefs.getBoolean("lastWord", lastWordEnabled)
        teams.clear()
        t.forEach { (name, score) -> teams.add(Team(name, score)) }
        turnIndex = savePrefs.getInt("turnIndex", 0)
        deck = WordDeck(repo, category)
        results.clear()
        screen = Screen.READY
    }

    // ---- Навигация ----
    fun go(target: Screen) {
        screen = target
    }

    fun selectCategory(c: Category) {
        category = c
        screen = Screen.SETUP
    }

    // ---- Команды ----
    fun addTeam() {
        if (teams.size >= 6) return
        val taken = teams.map { it.name }.toSet()
        val name = FUNNY_TEAM_NAMES.shuffled().firstOrNull { it !in taken } ?: "Команда ${teams.size + 1}"
        teams.add(Team(name))
    }

    fun removeTeam(index: Int) {
        if (teams.size > 2) teams.removeAt(index)
    }

    // ---- Ход игры ----
    fun startGame() {
        teams.forEachIndexed { i, t ->
            t.name = t.name.trim().ifEmpty { "Команда ${i + 1}" }
            t.score = 0
        }
        prefs.edit()
            .putString("teams", teams.joinToString("\n") { it.name })
            .putInt("roundSeconds", roundSeconds)
            .putInt("targetScore", targetScore)
            .putBoolean("skipPenalty", skipPenalty)
            .putBoolean("lastWord", lastWordEnabled)
            .apply()
        turnIndex = 0
        deck = WordDeck(repo, category)
        writeSave()
        screen = Screen.READY
    }

    fun rematch() {
        teams.forEach { it.score = 0 }
        turnIndex = 0
        deck = WordDeck(repo, category)
        writeSave()
        screen = Screen.READY
    }

    fun startRound() {
        val d = deck ?: WordDeck(repo, category).also { deck = it }
        results.clear()
        lastWordPhase = false
        paused = false
        remainingMs = roundSeconds * 1000L
        currentWord = null
        waitingStart = true
        screen = Screen.PLAY
    }

    /** Кнопка «Старт»: показываем первое слово и запускаем таймер. */
    fun begin() {
        if (!waitingStart) return
        val d = deck ?: WordDeck(repo, category).also { deck = it }
        waitingStart = false
        paused = false
        currentWord = d.next()
        beep(ToneGenerator.TONE_PROP_ACK, 150)
        startTimer()
    }

    /** Назад со стартового экрана раунда — обратно к экрану хода. */
    fun cancelStart() {
        if (!waitingStart) return
        waitingStart = false
        screen = Screen.READY
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var last = SystemClock.elapsedRealtime()
            var lastBeep = -1L
            while (remainingMs > 0) {
                delay(100)
                val now = SystemClock.elapsedRealtime()
                if (!paused) {
                    remainingMs = (remainingMs - (now - last)).coerceAtLeast(0)
                    val sec = (remainingMs + 999) / 1000
                    if (sec in 1..5 && sec != lastBeep) {
                        lastBeep = sec
                        beep(ToneGenerator.TONE_PROP_BEEP, 120)
                    }
                }
                last = now
            }
            onTimeUp()
        }
    }

    private fun onTimeUp() {
        beep(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600)
        // Последнее слово доигрывается без ограничения времени,
        // пока его не угадают или не пропустят.
        if (currentWord != null) {
            lastWordPhase = true
        } else {
            currentWord = null
            screen = Screen.REVIEW
        }
    }

    private fun beep(type: Int, ms: Int) {
        runCatching { tone?.startTone(type, ms) }
    }

    fun togglePause() {
        if (waitingStart) return
        paused = !paused
    }

    /** Ответ на текущее слово: true — угадано (свайп вверх), false — пропуск (вниз). */
    fun answer(guessed: Boolean) {
        if (paused) return
        val word = currentWord ?: return
        if (lastWordPhase) {
            results.add(WordResult(word, guessed, isLast = true, lastTeam = if (guessed) currentTeamIndex else -1))
            lastWordPhase = false
            currentWord = null
            screen = Screen.REVIEW
        } else {
            results.add(WordResult(word, guessed))
            currentWord = deck?.next()
        }
    }

    // ---- Итоги раунда ----
    fun toggleResult(index: Int) {
        val r = results.getOrNull(index) ?: return
        val nowGuessed = !r.guessed
        results[index] = r.copy(
            guessed = nowGuessed,
            lastTeam = if (r.isLast) (if (nowGuessed) currentTeamIndex else -1) else r.lastTeam,
        )
    }

    fun setLastWordTeam(index: Int, team: Int) {
        val r = results.getOrNull(index) ?: return
        results[index] = r.copy(lastTeam = team)
    }

    /** Очки текущей команды за раунд. */
    fun roundPoints(): Int {
        val me = currentTeamIndex
        var points = 0
        for (r in results) {
            if (r.isLast) {
                if (r.guessed && r.lastTeam == me) points++
            } else {
                if (r.guessed) points++ else if (skipPenalty) points--
            }
        }
        return points
    }

    fun confirmRound() {
        val me = currentTeamIndex
        teams.getOrNull(me)?.let { it.score += roundPoints() }
        results.firstOrNull { it.isLast && it.guessed && it.lastTeam != me && it.lastTeam >= 0 }
            ?.let { r -> teams.getOrNull(r.lastTeam)?.let { it.score += 1 } }
        turnIndex++

        // Победителя проверяем, только когда все команды сыграли одинаковое число ходов.
        if (turnIndex % teams.size == 0) {
            val max = teams.maxOf { it.score }
            if (max >= targetScore && teams.count { it.score == max } == 1) {
                clearSave()
                screen = Screen.WINNER
                return
            }
        }
        writeSave()
        screen = Screen.READY
    }

    fun endGame() {
        timerJob?.cancel()
        paused = false
        currentWord = null
        waitingStart = false
        lastWordPhase = false
        screen = Screen.MENU
    }

    override fun onCleared() {
        timerJob?.cancel()
        tone?.release()
    }
}
