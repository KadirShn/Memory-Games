package com.kadirsahin.hafizaoyunu.domain

enum class GameMode(
    val storageId: String,
    val rows: Int,
    val columns: Int,
    val durationSeconds: Int,
) {
    EASY("4x4", rows = 4, columns = 4, durationSeconds = 75),
    FOCUS("4x5", rows = 5, columns = 4, durationSeconds = 110),
    MASTER("6x6", rows = 6, columns = 6, durationSeconds = 180),
    ;

    val cardCount: Int get() = rows * columns
    val pairCount: Int get() = cardCount / 2

    companion object {
        fun fromStorageId(value: String): GameMode? = entries.firstOrNull { it.storageId == value }
    }
}
