package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelNumber: Int,
    val isUnlocked: Boolean = false,
    val starsEarned: Int = 0,
    val bestScore: Int = 0,
    val completions: Int = 0
)

@Entity(tableName = "player_stats")
data class PlayerStatsEntity(
    @PrimaryKey val id: Int = 1,
    val classicBestScore: Int = 0,
    val dailyBestScore: Int = 0,
    val coins: Int = 250,
    val hammerCount: Int = 3,
    val bombCount: Int = 2,
    val rerollCount: Int = 3,
    val rotateCount: Int = 3,
    val totalLinesCleared: Int = 0,
    val totalGemsCollected: Int = 0,
    val maxComboReached: Int = 0,
    val gamesPlayed: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true
)

@Entity(tableName = "game_history")
data class GameHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modeTitle: String,
    val levelNumber: Int,
    val score: Int,
    val stars: Int,
    val maxCombo: Int,
    val linesCleared: Int,
    val timestamp: Long = System.currentTimeMillis()
)
