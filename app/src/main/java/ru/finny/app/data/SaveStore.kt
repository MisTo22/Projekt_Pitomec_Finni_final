package ru.finny.app.data

import android.content.Context
import com.google.gson.Gson

/** Локальное сохранение профиля v2 (SharedPreferences + Gson, только офлайн). */
object SaveStore {
    private const val PREF = "finny_save_v2"
    private const val KEY = "game_state_v2"
    private val gson = Gson()

    fun save(context: Context, state: GameState2) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, gson.toJson(state)).apply()
    }

    fun load(context: Context): GameState2? = try {
        val json = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, null)
        if (json == null) null else gson.fromJson(json, GameState2::class.java)
    } catch (e: Exception) {
        null
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
