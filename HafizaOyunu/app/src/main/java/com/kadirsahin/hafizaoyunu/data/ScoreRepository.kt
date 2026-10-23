package com.kadirsahin.hafizaoyunu.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Leaderboards shown in the app. Classic keys match the values stored by every earlier release. */
enum class Leaderboard(val key: String) {
    EASY("4x4"),
    FOCUS("4x5"),
    MASTER("6x6"),
    SURVIVAL("survival"),
    DAILY("daily"),
}

data class ScoreEntry(
    val id: Long,
    val board: Leaderboard,
    val points: Int,
    val durationSeconds: Int,
    val moves: Int,
    val createdAt: Long,
)

class ScoreRepository(context: Context) {
    private val database = LegacyCompatibleDatabase(context.applicationContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _scores = MutableStateFlow<List<ScoreEntry>>(emptyList())
    val scores: StateFlow<List<ScoreEntry>> = _scores.asStateFlow()

    init {
        scope.launch { refresh() }
    }

    suspend fun record(board: Leaderboard, points: Int, durationSeconds: Int, moves: Int) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(COLUMN_POINTS, points)
            put(COLUMN_DURATION, durationSeconds)
            put(COLUMN_MODE, board.key)
            put(COLUMN_MOVES, moves)
            put(COLUMN_CREATED, System.currentTimeMillis())
        }
        check(database.writableDatabase.insert(TABLE_SCORE, null, values) != -1L) {
            "Score could not be saved"
        }
        refresh()
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        database.writableDatabase.delete(TABLE_SCORE, null, null)
        refresh()
    }

    private suspend fun refresh() = withContext(Dispatchers.IO) {
        val result = buildList {
            database.readableDatabase.query(
                TABLE_SCORE,
                arrayOf(COLUMN_ID, COLUMN_POINTS, COLUMN_DURATION, COLUMN_MODE, COLUMN_MOVES, COLUMN_CREATED),
                null,
                null,
                null,
                null,
                "$COLUMN_POINTS DESC, $COLUMN_DURATION ASC, $COLUMN_ID DESC",
                MAX_STORED_RESULTS.toString(),
            ).use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID)
                val pointsIndex = cursor.getColumnIndexOrThrow(COLUMN_POINTS)
                val durationIndex = cursor.getColumnIndexOrThrow(COLUMN_DURATION)
                val modeIndex = cursor.getColumnIndexOrThrow(COLUMN_MODE)
                val movesIndex = cursor.getColumnIndexOrThrow(COLUMN_MOVES)
                val createdIndex = cursor.getColumnIndexOrThrow(COLUMN_CREATED)
                while (cursor.moveToNext()) {
                    val key = cursor.getString(modeIndex)
                    val board = Leaderboard.entries.firstOrNull { it.key == key } ?: continue
                    add(
                        ScoreEntry(
                            id = cursor.getLong(idIndex),
                            board = board,
                            points = cursor.getInt(pointsIndex),
                            durationSeconds = cursor.getInt(durationIndex),
                            moves = cursor.getInt(movesIndex),
                            createdAt = cursor.getLong(createdIndex),
                        ),
                    )
                }
            }
        }
        _scores.value = result
    }
}

private class LegacyCompatibleDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_SCORE (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_POINTS INTEGER NOT NULL,
                $COLUMN_DURATION INTEGER NOT NULL,
                $COLUMN_MODE TEXT NOT NULL,
                $COLUMN_MOVES INTEGER NOT NULL DEFAULT 0,
                $COLUMN_CREATED INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Version 1 scores (all releases up to 2.0) keep their rows; the new columns default to zero.
            db.execSQL("ALTER TABLE $TABLE_SCORE ADD COLUMN $COLUMN_MOVES INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE $TABLE_SCORE ADD COLUMN $COLUMN_CREATED INTEGER NOT NULL DEFAULT 0")
        }
    }
}

private const val DATABASE_NAME = "HafizaOyunu2"
private const val DATABASE_VERSION = 2
private const val TABLE_SCORE = "Skor"
private const val COLUMN_ID = "id4"
private const val COLUMN_POINTS = "puan4"
private const val COLUMN_DURATION = "sure4"
private const val COLUMN_MODE = "tur"
private const val COLUMN_MOVES = "hamle"
private const val COLUMN_CREATED = "tarih"
private const val MAX_STORED_RESULTS = 600
