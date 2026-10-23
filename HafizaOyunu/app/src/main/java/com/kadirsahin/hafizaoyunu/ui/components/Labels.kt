package com.kadirsahin.hafizaoyunu.ui.components

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.kadirsahin.hafizaoyunu.R
import com.kadirsahin.hafizaoyunu.data.Leaderboard
import com.kadirsahin.hafizaoyunu.domain.Achievement
import com.kadirsahin.hafizaoyunu.domain.Booster
import com.kadirsahin.hafizaoyunu.domain.CardBack
import com.kadirsahin.hafizaoyunu.domain.CardTheme
import com.kadirsahin.hafizaoyunu.domain.GameMode
import com.kadirsahin.hafizaoyunu.domain.GameType
import com.kadirsahin.hafizaoyunu.domain.Twist

@StringRes
fun GameMode.titleRes(): Int = when (this) {
    GameMode.EASY -> R.string.board_easy
    GameMode.FOCUS -> R.string.board_focus
    GameMode.MASTER -> R.string.board_master
}

@StringRes
fun GameType.titleRes(): Int = when (this) {
    GameType.CLASSIC -> R.string.mode_classic
    GameType.CAMPAIGN -> R.string.campaign
    GameType.DAILY -> R.string.daily_challenge
    GameType.SURVIVAL -> R.string.survival
    GameType.ZEN -> R.string.mode_zen
    GameType.DUEL -> R.string.mode_duel
}

@StringRes
fun GameType.descriptionRes(): Int = when (this) {
    GameType.CLASSIC -> R.string.mode_classic_desc
    GameType.CAMPAIGN -> R.string.mode_campaign_desc
    GameType.DAILY -> R.string.mode_daily_desc
    GameType.SURVIVAL -> R.string.mode_survival_desc
    GameType.ZEN -> R.string.mode_zen_desc
    GameType.DUEL -> R.string.mode_duel_desc
}

fun GameType.glyph(): Glyph = when (this) {
    GameType.CLASSIC -> Glyph.CLOCK
    GameType.CAMPAIGN -> Glyph.FLAG
    GameType.DAILY -> Glyph.CALENDAR
    GameType.SURVIVAL -> Glyph.HEART
    GameType.ZEN -> Glyph.LEAF
    GameType.DUEL -> Glyph.USERS
}

fun GameType.gradient(): List<Color> = when (this) {
    GameType.CLASSIC -> listOf(Color(0xFF7B5CFF), Color(0xFF4A2DD6))
    GameType.CAMPAIGN -> listOf(Color(0xFFFF8A3D), Color(0xFFE5376B))
    GameType.DAILY -> listOf(Color(0xFF22C9E8), Color(0xFF2F6BFF))
    GameType.SURVIVAL -> listOf(Color(0xFFFF4F7B), Color(0xFF9B1C5C))
    GameType.ZEN -> listOf(Color(0xFF39D98A), Color(0xFF138A72))
    GameType.DUEL -> listOf(Color(0xFFFFC23D), Color(0xFFE56A1C))
}

@StringRes
fun Twist.titleRes(): Int = when (this) {
    Twist.NO_PREVIEW -> R.string.twist_no_preview
    Twist.SHUFFLE -> R.string.twist_shuffle
    Twist.ICE -> R.string.twist_ice
    Twist.GOLDEN -> R.string.twist_golden
    Twist.HOURGLASS -> R.string.twist_hourglass
}

@StringRes
fun Twist.descriptionRes(): Int = when (this) {
    Twist.NO_PREVIEW -> R.string.twist_no_preview_desc
    Twist.SHUFFLE -> R.string.twist_shuffle_desc
    Twist.ICE -> R.string.twist_ice_desc
    Twist.GOLDEN -> R.string.twist_golden_desc
    Twist.HOURGLASS -> R.string.twist_hourglass_desc
}

fun Twist.glyph(): Glyph = when (this) {
    Twist.NO_PREVIEW -> Glyph.MOON
    Twist.SHUFFLE -> Glyph.SHUFFLE
    Twist.ICE -> Glyph.SNOWFLAKE
    Twist.GOLDEN -> Glyph.CROWN
    Twist.HOURGLASS -> Glyph.HOURGLASS
}

@StringRes
fun Booster.titleRes(): Int = when (this) {
    Booster.PEEK -> R.string.booster_peek
    Booster.HINT -> R.string.booster_hint
    Booster.FREEZE -> R.string.booster_freeze
}

@StringRes
fun Booster.descriptionRes(): Int = when (this) {
    Booster.PEEK -> R.string.booster_peek_desc
    Booster.HINT -> R.string.booster_hint_desc
    Booster.FREEZE -> R.string.booster_freeze_desc
}

fun Booster.glyph(): Glyph = when (this) {
    Booster.PEEK -> Glyph.EYE
    Booster.HINT -> Glyph.BULB
    Booster.FREEZE -> Glyph.SNOWFLAKE
}

fun Booster.gradient(): List<Color> = when (this) {
    Booster.PEEK -> listOf(Color(0xFFB06BFF), Color(0xFF6A3DE0))
    Booster.HINT -> listOf(Color(0xFFFFC23D), Color(0xFFE5861C))
    Booster.FREEZE -> listOf(Color(0xFF6FE3FF), Color(0xFF2F8BFF))
}

@StringRes
fun CardBack.titleRes(): Int = when (this) {
    CardBack.AURORA -> R.string.back_aurora
    CardBack.NEON_GRID -> R.string.back_neon_grid
    CardBack.WAVES -> R.string.back_waves
    CardBack.CONSTELLATION -> R.string.back_constellation
    CardBack.HEX -> R.string.back_hex
    CardBack.SUNBURST -> R.string.back_sunburst
    CardBack.ROYAL -> R.string.back_royal
}

@StringRes
fun CardTheme.titleRes(): Int = when (this) {
    CardTheme.CLASSIC -> R.string.card_theme_classic
    CardTheme.COSMIC -> R.string.card_theme_cosmic
    CardTheme.FOREST -> R.string.card_theme_forest
    CardTheme.NEON -> R.string.card_theme_neon
    CardTheme.EMOJI -> R.string.card_theme_emoji
}

@StringRes
fun CardTheme.descriptionRes(): Int = when (this) {
    CardTheme.CLASSIC -> R.string.card_theme_classic_description
    CardTheme.COSMIC -> R.string.card_theme_cosmic_description
    CardTheme.FOREST -> R.string.card_theme_forest_description
    CardTheme.NEON -> R.string.card_theme_neon_description
    CardTheme.EMOJI -> R.string.card_theme_emoji_description
}

@StringRes
fun Leaderboard.titleRes(): Int = when (this) {
    Leaderboard.EASY -> R.string.board_easy
    Leaderboard.FOCUS -> R.string.board_focus
    Leaderboard.MASTER -> R.string.board_master
    Leaderboard.SURVIVAL -> R.string.survival
    Leaderboard.DAILY -> R.string.lb_daily
}

@StringRes
fun chapterTitleRes(chapter: Int): Int = when (chapter) {
    1 -> R.string.chapter_1
    2 -> R.string.chapter_2
    3 -> R.string.chapter_3
    4 -> R.string.chapter_4
    else -> R.string.chapter_5
}

fun chapterGradient(chapter: Int): List<Color> = when (chapter) {
    1 -> listOf(Color(0xFF7B5CFF), Color(0xFF22C9E8))
    2 -> listOf(Color(0xFF2F6BFF), Color(0xFF8E44FF))
    3 -> listOf(Color(0xFF6FE3FF), Color(0xFF2F8BFF))
    4 -> listOf(Color(0xFF3B2A6E), Color(0xFF12082E))
    else -> listOf(Color(0xFFFFC23D), Color(0xFFE5376B))
}

@StringRes
fun rankTitleRes(level: Int): Int = when {
    level >= 30 -> R.string.rank_6
    level >= 20 -> R.string.rank_5
    level >= 15 -> R.string.rank_4
    level >= 10 -> R.string.rank_3
    level >= 5 -> R.string.rank_2
    else -> R.string.rank_1
}

@StringRes
fun Achievement.titleRes(): Int = when (this) {
    Achievement.FIRST_WIN -> R.string.ach_first_win
    Achievement.PERFECT -> R.string.ach_perfect
    Achievement.COMBO_5 -> R.string.ach_combo_5
    Achievement.COMBO_8 -> R.string.ach_combo_8
    Achievement.SPEEDSTER -> R.string.ach_speedster
    Achievement.MASTER -> R.string.ach_master
    Achievement.PURIST -> R.string.ach_purist
    Achievement.CAMPAIGN_10 -> R.string.ach_campaign_10
    Achievement.CAMPAIGN_ALL -> R.string.ach_campaign_all
    Achievement.STARS_90 -> R.string.ach_stars_90
    Achievement.SURVIVOR_5 -> R.string.ach_survivor_5
    Achievement.SURVIVOR_10 -> R.string.ach_survivor_10
    Achievement.DAILY_FIRST -> R.string.ach_daily_first
    Achievement.STREAK_7 -> R.string.ach_streak_7
    Achievement.DUELIST -> R.string.ach_duelist
    Achievement.ZEN_10 -> R.string.ach_zen_10
    Achievement.GAMES_50 -> R.string.ach_games_50
    Achievement.GAMES_200 -> R.string.ach_games_200
    Achievement.MATCHES_1000 -> R.string.ach_matches_1000
    Achievement.LEVEL_10 -> R.string.ach_level_10
    Achievement.ICE_50 -> R.string.ach_ice_50
    Achievement.GOLDEN_25 -> R.string.ach_golden_25
    Achievement.NIGHT_OWL -> R.string.ach_night_owl
}

@StringRes
fun Achievement.descriptionRes(): Int = when (this) {
    Achievement.FIRST_WIN -> R.string.ach_first_win_desc
    Achievement.PERFECT -> R.string.ach_perfect_desc
    Achievement.COMBO_5 -> R.string.ach_combo_5_desc
    Achievement.COMBO_8 -> R.string.ach_combo_8_desc
    Achievement.SPEEDSTER -> R.string.ach_speedster_desc
    Achievement.MASTER -> R.string.ach_master_desc
    Achievement.PURIST -> R.string.ach_purist_desc
    Achievement.CAMPAIGN_10 -> R.string.ach_campaign_10_desc
    Achievement.CAMPAIGN_ALL -> R.string.ach_campaign_all_desc
    Achievement.STARS_90 -> R.string.ach_stars_90_desc
    Achievement.SURVIVOR_5 -> R.string.ach_survivor_5_desc
    Achievement.SURVIVOR_10 -> R.string.ach_survivor_10_desc
    Achievement.DAILY_FIRST -> R.string.ach_daily_first_desc
    Achievement.STREAK_7 -> R.string.ach_streak_7_desc
    Achievement.DUELIST -> R.string.ach_duelist_desc
    Achievement.ZEN_10 -> R.string.ach_zen_10_desc
    Achievement.GAMES_50 -> R.string.ach_games_50_desc
    Achievement.GAMES_200 -> R.string.ach_games_200_desc
    Achievement.MATCHES_1000 -> R.string.ach_matches_1000_desc
    Achievement.LEVEL_10 -> R.string.ach_level_10_desc
    Achievement.ICE_50 -> R.string.ach_ice_50_desc
    Achievement.GOLDEN_25 -> R.string.ach_golden_25_desc
    Achievement.NIGHT_OWL -> R.string.ach_night_owl_desc
}

fun Achievement.glyph(): Glyph = when (this) {
    Achievement.FIRST_WIN -> Glyph.TROPHY
    Achievement.PERFECT -> Glyph.SPARK
    Achievement.COMBO_5, Achievement.COMBO_8 -> Glyph.FLAME
    Achievement.SPEEDSTER -> Glyph.BOLT
    Achievement.MASTER, Achievement.PURIST -> Glyph.CROWN
    Achievement.CAMPAIGN_10, Achievement.CAMPAIGN_ALL -> Glyph.FLAG
    Achievement.STARS_90 -> Glyph.STAR
    Achievement.SURVIVOR_5, Achievement.SURVIVOR_10 -> Glyph.HEART
    Achievement.DAILY_FIRST, Achievement.STREAK_7 -> Glyph.CALENDAR
    Achievement.DUELIST -> Glyph.USERS
    Achievement.ZEN_10 -> Glyph.LEAF
    Achievement.GAMES_50, Achievement.GAMES_200 -> Glyph.CARDS
    Achievement.MATCHES_1000 -> Glyph.CHECK
    Achievement.LEVEL_10 -> Glyph.MEDAL
    Achievement.ICE_50 -> Glyph.SNOWFLAKE
    Achievement.GOLDEN_25 -> Glyph.COIN
    Achievement.NIGHT_OWL -> Glyph.MOON
}
