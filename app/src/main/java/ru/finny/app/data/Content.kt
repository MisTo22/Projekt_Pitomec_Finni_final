package ru.finny.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.finny.app.R

/** Контент и справочники v2: задания из assets/json, плитки и спрайты из res. */
object Content {
    lateinit var tasks: List<Task2>
        private set
    private var loaded = false

    fun load(context: Context) {
        if (loaded) return
        val json = context.assets.open("json/tasks_v2.json").bufferedReader().use { it.readText() }
        tasks = Gson().fromJson(json, object : TypeToken<List<Task2>>() {}.type)
        loaded = true
    }

    /** Градиентные плитки-иконки (стиль макетов Group 6/7/8). */
    fun tile(name: String): Int = when (name) {
        "coin" -> R.drawable.tile_coin
        "star" -> R.drawable.tile_star
        "sytost" -> R.drawable.tile_sytost
        "joy" -> R.drawable.tile_joy
        "energy" -> R.drawable.tile_energy
        "mood" -> R.drawable.tile_mood
        "feed" -> R.drawable.tile_feed
        "play" -> R.drawable.tile_play
        "sleep" -> R.drawable.tile_sleep
        "backpack" -> R.drawable.tile_backpack
        "tasks" -> R.drawable.tile_tasks
        "arrow" -> R.drawable.tile_arrow
        "ball" -> R.drawable.tile_ball
        "garland" -> R.drawable.tile_garland
        "puzzle" -> R.drawable.tile_puzzle
        "cap" -> R.drawable.tile_cap
        "honey" -> R.drawable.tile_honey
        "berry" -> R.drawable.tile_berry
        "parent" -> R.drawable.tile_parent
        else -> R.drawable.tile_star
    }

    /** Исходные спрайты ежа: стадия x настроение + сон. */
    fun sprite(stage: Int, mood: String, sleeping: Boolean): Int = when {
        sleeping -> when (stage) {
            1 -> R.drawable.finny_s1_sleep
            2 -> R.drawable.finny_s2_sleep
            else -> R.drawable.finny_s3_sleep
        }
        stage == 1 -> when (mood) {
            "happy" -> R.drawable.finny_s1_happy
            "sad" -> R.drawable.finny_s1_sad
            else -> R.drawable.finny_s1_ok
        }
        stage == 2 -> when (mood) {
            "happy" -> R.drawable.finny_s2_happy
            "sad" -> R.drawable.finny_s2_sad
            else -> R.drawable.finny_s2_ok
        }
        else -> when (mood) {
            "happy" -> R.drawable.finny_s3_happy
            "sad" -> R.drawable.finny_s3_sad
            else -> R.drawable.finny_s3_ok
        }
    }

    val wishes = listOf(
        Wish("ball", "Мячик-попрыгун", 10, "ball"),
        Wish("garl", "Гирлянда из звёздочек", 15, "garland"),
        Wish("puz", "Игра-головоломка", 20, "puzzle"),
        Wish("cap", "Оранжевая кепка", 15, "cap"),
    )

    val shop = listOf(
        ShopItem("feed", "Корм для Финни", 10, "sytost", "food", 15, "Сытость +15"),
        ShopItem("treat", "Медовая вкусняшка", 5, "honey", "joy", 10, "Радость +10"),
        ShopItem("vita", "Ягодные витамины", 10, "berry", "energy", 15, "Энергия +15"),
    )

    /** Соответствие заданий возрастным группам (экран «Сколько тебе лет?»). */
    val ageMap = mapOf(
        "t1" to "7-9", "t2" to "7-9", "t4" to "7-9", "t5" to "7-9", "t7" to "7-9", "t9" to "7-9",
        "t3" to "10-11", "t6" to "10-11", "t8" to "10-11",
    )
}
