package ru.finny.app.data

/** Задание финграмотности v2 (assets/json/tasks_v2.json). */
data class Task2(
    val id: String,
    val th: String,
    val t: String,
    val s: String,
    val q: String,
    val o: List<String>,
    val c: Int,
    val e: String,
    val h: String,
)

/** Желание из списка мечт. */
data class Wish(val id: String, val n: String, val p: Int, val ic: String)

/** Товар заботы из сегмента «Покупки». */
data class ShopItem(val id: String, val n: String, val p: Int, val ic: String, val eff: String, val d: Int, val txt: String)

/**
 * Локальный профиль (гостевой, офлайн).
 * Экономика v2: старт с малого капитала (25 монет — хватает на поддержку шкал),
 * основной доход — задания (+15) и дневные карманные 5–10 при пробуждении.
 */
data class GameState2(
    val coins: Int = 25,
    val day: Int = 1,
    val food: Int = 82,
    val joy: Int = 74,
    val energy: Int = 61,
    val level: Int = 1,
    val prog: Int = 0,
    val saved: Int = 0,
    val savedTarget: Int = 180,
    val age: String? = null,
    val solved: List<String> = emptyList(),
    val bought: List<String> = emptyList(),
    val sleeping: Boolean = false,
)
