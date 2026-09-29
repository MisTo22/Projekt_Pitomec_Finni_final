package ru.finny.app.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.finny.app.audio.SoundManager
import ru.finny.app.data.Content
import ru.finny.app.data.GameState2
import ru.finny.app.data.SaveStore
import kotlin.random.Random

/**
 * ViewModel игры v2 (точь-в-точь finni_v2.html) + новая экономика:
 *  - старт с 25 монет — хватает на 2–3 покупки заботы, но не на «всё сразу»;
 *  - каждое утро (пробуждение) карманные 5–10 монет: база 5 + случайные 0–3
 *    + бонус +2, если все шкалы ≥ 50 (забота вознаграждается);
 *  - основной доход — задания (+15 впервые, +2 повтор), уровень +20;
 *  - траты: забота 5–10, желания 10–20 — монеты снова имеют цену.
 */
class GameViewModel(app: Application) : AndroidViewModel(app) {

    data class Particle(val id: Int, val kind: String, val x: Float, val y: Float)
    data class AnimEvent(val token: Int, val name: String)

    private val _state = MutableStateFlow(SaveStore.load(app) ?: GameState2())
    val state: StateFlow<GameState2> = _state.asStateFlow()

    private val _phase = MutableStateFlow(if (SaveStore.load(app)?.age == null) "entry" else "app")
    val phase: StateFlow<String> = _phase.asStateFlow()

    private val _bubble = MutableStateFlow<String?>(null)
    val bubble: StateFlow<String?> = _bubble.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    private val _anim = MutableStateFlow(AnimEvent(0, ""))
    val anim: StateFlow<AnimEvent> = _anim.asStateFlow()

    private val _tab = MutableStateFlow("home")
    val tab: StateFlow<String> = _tab.asStateFlow()

    private val _seg = MutableStateFlow("wish")
    val seg: StateFlow<String> = _seg.asStateFlow()

    private val _openTask = MutableStateFlow<String?>(null)
    val openTask: StateFlow<String?> = _openTask.asStateFlow()

    private val _fb = MutableStateFlow<Pair<Boolean, String>?>(null)
    val fb: StateFlow<Pair<Boolean, String>?> = _fb.asStateFlow()

    private var pid = 0
    private var token = 0
    private var idleCd = 8
    private var petLast = 0L

    init {
        Content.load(app)
        SoundManager.init(app)
        if (_phase.value == "app") _tab.value = "home"
        viewModelScope.launch { idleLoop() }
    }

    private val s: GameState2 get() = _state.value
    private fun commit(next: GameState2) {
        _state.value = next
        SaveStore.save(getApplication(), next)
    }

    fun say(text: String, ms: Long = 3400) {
        _bubble.value = text
        viewModelScope.launch {
            delay(ms)
            _bubble.value = null
        }
    }

    // ---------- вход и возраст ----------
    fun goAge() { _phase.value = "age" }
    fun pickAge(a: String) {
        commit(s.copy(age = a))
        _phase.value = "app"
        say(
            if (a == "7-8") "Привет! Я Финни! Мне тоже немного лет — будем играть вместе 💜"
            else "Привет! Я Финни! Ты уже большой — задания будут посерьёзнее ⭐",
            4600
        )
    }

    // ---------- настроение / стадия ----------
    fun mood(): String {
        val m = minOf(s.food, s.joy, s.energy)
        return if (m >= 65) "happy" else if (m >= 35) "ok" else "sad"
    }
    private fun stage(): Int = if (s.level <= 3) 1 else if (s.level <= 5) 2 else 3
    fun spriteRes(): Int = Content.sprite(stage(), mood(), s.sleeping)

    // ---------- частицы и анимации ----------
    private fun spawn(kind: List<String>, n: Int) {
        val list = _particles.value.toMutableList()
        repeat(n) {
            list.add(Particle(pid++, kind.random(), Random.nextFloat(), Random.nextFloat()))
        }
        _particles.value = list
    }
    fun removeParticle(id: Int) {
        _particles.value = _particles.value.filterNot { it.id == id }
    }
    private fun anim(name: String) {
        token++
        _anim.value = AnimEvent(token, name)
    }

    // ---------- действия ----------
    fun feed() {
        if (s.sleeping) { say("Тсс… Финни спит до утра 💤"); return }
        if (s.coins < 10) { say("Не хватает монеток! Выполни задание ⭐"); return }
        SoundManager.playCoin()
        commit(s.copy(coins = s.coins - 10, food = clamp(s.food + 15), prog = s.prog + 4))
        anim("munch"); spawn(listOf("sytost", "coin"), 5)
        say(listOf("Ням-ням! Спасибо!", "Хрум-хрум… вкусно!", "Обед по расписанию!").random())
        levelCheck()
    }

    fun play() {
        if (s.sleeping) { say("Тсс… Финни спит до утра 💤"); return }
        if (s.energy < 10) { say("Финни устал… уложи его поспать 😴"); return }
        SoundManager.playPet()
        commit(s.copy(joy = clamp(s.joy + 15), energy = clamp(s.energy - 10), prog = s.prog + 4))
        anim("jump"); spawn(listOf("wishes", "ball", "star"), 6)
        say(listOf("Ура! Мячик-попрыгун!", "Прыг-скок! Ещё разок!", "Весело-весело!").random())
        levelCheck()
    }

    fun sleepToggle() {
        SoundManager.playClick()
        if (!s.sleeping) {
            commit(s.copy(sleeping = true))
            spawn(listOf("sleep"), 4)
            say("Спокойной ночи… Финни видит сны про звёзды 💤", 4200)
        } else {
            wake()
        }
    }

    /** Утро: новый день, карманные 5–10 (база 5 + 0–3 случайно + 2 за заботу), лёгкий износ шкал. */
    private fun wake() {
        val cared = s.food >= 50 && s.joy >= 50 && s.energy >= 50
        val allowance = 5 + Random.nextInt(0, 4) + if (cared) 2 else 0
        commit(
            s.copy(
                sleeping = false,
                day = s.day + 1,
                coins = s.coins + allowance,
                food = clamp(s.food - 12),
                joy = clamp(s.joy - 10),
                energy = clamp(s.energy + 40),
                prog = s.prog + 3,
            )
        )
        spawn(listOf("energy", "star"), 6)
        say(
            "Доброе утро! Карманные +$allowance 🪙" + if (cared) " (бонус за заботу!)" else "",
            4200
        )
        levelCheck()
    }

    fun tapPet(zone: String) {
        if (s.sleeping) { say("Финни посапывает во сне… не буди 💤"); return }
        SoundManager.playPet()
        commit(s.copy(prog = s.prog + 1))
        when (zone) {
            "belly" -> { anim("wiggle"); spawn(listOf("wishes", "radost"), 5); say("Хи-хи! Пузо щекотное!") }
            "paws" -> { anim("jump"); spawn(listOf("star"), 4); say("Ой! Лапки щекочутся!") }
            else -> {
                anim("jump"); spawn(listOf("wishes", "coin", "star"), 5)
                say(listOf("Привет-привет!", "Погладки — лучшие подарки!", "Ты мой любимый человек!", "Пойдём играть? ✨").random())
            }
        }
        levelCheck()
    }

    fun petMove() {
        if (s.sleeping) return
        val now = System.currentTimeMillis()
        if (now - petLast < 150) return
        petLast = now
        SoundManager.playPet()
        spawn(listOf("wishes"), 1)
    }

    // ---------- желания и покупки ----------
    fun buyWish(id: String) {
        val w = Content.wishes.find { it.id == id } ?: return
        if (s.bought.contains(id)) return
        if (s.coins < w.p) { say("Не хватает ${w.p - s.coins} монеток… Заработай в заданиях!"); return }
        SoundManager.playCoin()
        commit(s.copy(coins = s.coins - w.p, bought = s.bought + id, prog = s.prog + 6))
        spawn(listOf("coin", "star", "wishes"), 7)
        say("Ура! «${w.n}» теперь у Финни! 🎉", 4000)
        levelCheck()
    }

    fun buyShop(id: String) {
        val it = Content.shop.find { it.id == id } ?: return
        if (s.coins < it.p) { say("Не хватает монеток! Выполни задание ⭐"); return }
        SoundManager.playCoin()
        val st = s.copy(coins = s.coins - it.p, prog = s.prog + 3)
        commit(
            when (it.eff) {
                "food" -> st.copy(food = clamp(st.food + it.d))
                "joy" -> st.copy(joy = clamp(st.joy + it.d))
                else -> st.copy(energy = clamp(st.energy + it.d))
            }
        )
        anim("munch"); spawn(listOf(it.ic, "coin"), 5)
        say("${it.txt}! Финни доволен.")
        levelCheck()
    }

    // ---------- задания ----------
    fun show(tab: String) {
        SoundManager.playClick()
        _tab.value = tab
        if (tab == "tasks" && _openTask.value == null) renderList()
    }
    fun setSeg(seg: String) {
        SoundManager.playClick()
        if (seg == "task") { show("tasks"); return }
        _seg.value = seg
    }
    private fun renderList() { _fb.value = null }
    fun openTask(id: String?) {
        SoundManager.playClick()
        _openTask.value = id
        _fb.value = null
    }
    fun answer(i: Int) {
        val t = Content.tasks.find { it.id == _openTask.value } ?: return
        if (i == t.c) {
            SoundManager.playSuccess()
            val first = !s.solved.contains(t.id)
            val reward = if (first) 15 else 2
            commit(
                s.copy(
                    solved = if (first) s.solved + t.id else s.solved,
                    coins = s.coins + reward,
                    prog = s.prog + 10,
                )
            )
            _fb.value = true to (t.e + " Награда: +$reward 🪙" + if (first) "" else " (тренировка)")
            spawn(listOf("coin", "star"), 6)
            levelCheck()
        } else {
            SoundManager.playClick()
            _fb.value = false to t.h
        }
    }

    /** Внесение всех текущих монеток в общую цель (копилку). */
    fun depositToGoal() {
        val currentCoins = s.coins
        val remainingGoal = s.savedTarget - s.saved
        if (remainingGoal <= 0) {
            say("Ура! Мечта «Рюкзак исследователя» уже осуществлена! 🎉")
            return
        }
        if (currentCoins <= 0) {
            say("У тебя пока нет монеток. Выполняй задания! ⭐")
            return
        }
        val transfer = minOf(currentCoins, remainingGoal)
        val nextSaved = s.saved + transfer
        val nextCoins = s.coins - transfer
        val isCompleted = nextSaved >= s.savedTarget

        commit(
            s.copy(
                coins = nextCoins,
                saved = nextSaved,
                prog = s.prog + transfer * 2
            )
        )
        SoundManager.playCoin()
        spawn(listOf("coin", "star"), 8)

        if (isCompleted) {
            say("УРААА! Ты накопил монеты на Рюкзак исследователя! 🎉🎉🎉", 4500)
        } else {
            say("Отложили +$transfer 🪙 в копилочку! Осталось ${s.savedTarget - nextSaved} монеток 🎒", 3800)
        }
        levelCheck()
    }

    /** Сброс прогресса игры до начальных настроек. */
    fun resetGame() {
        SaveStore.clear(getApplication())
        _state.value = GameState2()
        _phase.value = "entry"
        _tab.value = "home"
        _openTask.value = null
        _fb.value = null
        say("Прогресс сброшен к начальным настройкам ✨", 4000)
        SoundManager.playSuccess()
    }
    /** Задания, отсортированные по возрасту ребёнка (как в HTML). */
    fun sortedTasks(): List<ru.finny.app.data.Task2> {
        val band = if (s.age == "9-11") "10-11" else "7-9"
        return Content.tasks.sortedBy { if (Content.ageMap[it.id] == band) 0 else 1 }
    }

    // ---------- уровень ----------
    private fun levelCheck() {
        var st = _state.value
        while (st.prog >= 100) {
            st = st.copy(prog = st.prog - 100, level = st.level + 1, coins = st.coins + 20)
            spawn(listOf("star", "coin"), 8)
            say("Уровень ${st.level}! Финни растёт благодаря твоей заботе 🌟", 4200)
        }
        if (st != _state.value) commit(st)
    }

    // ---------- idle ----------
    private suspend fun idleLoop() {
        while (true) {
            delay(1000)
            if (s.sleeping || _tab.value != "home") continue
            idleCd--
            if (idleCd > 0) continue
            idleCd = 8 + Random.nextInt(5)
            if (Random.nextFloat() < .5) {
                say(listOf("Пойдём играть? ✨", "Погладь меня!", "Я проголодался…", "Копим на рюкзак вместе!").random(), 2600)
            } else {
                anim(listOf("look", "yawn", "wiggle").random())
            }
        }
    }

    private fun clamp(v: Int) = v.coerceIn(0, 100)
}
