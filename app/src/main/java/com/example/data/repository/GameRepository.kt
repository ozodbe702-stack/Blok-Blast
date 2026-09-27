package com.example.data.repository

import com.example.data.local.GameDao
import com.example.data.local.GameHistoryEntity
import com.example.data.local.LevelProgressEntity
import com.example.data.local.PlayerStatsEntity
import com.example.model.LevelCatalog
import com.example.model.PowerUpType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val gameDao: GameDao) {

    val levelProgressFlow: Flow<List<LevelProgressEntity>> = gameDao.observeAllLevelProgress()
    val playerStatsFlow: Flow<PlayerStatsEntity?> = gameDao.observePlayerStats()
    val recentHistoryFlow: Flow<List<GameHistoryEntity>> = gameDao.observeRecentHistory()

    suspend fun ensureInitialized() {
        val currentStats = gameDao.getPlayerStatsOnce()
        if (currentStats == null) {
            gameDao.upsertPlayerStats(PlayerStatsEntity())
        }
        val existingLevels = gameDao.observeAllLevelProgress().firstOrNull().orEmpty()
        if (existingLevels.size < LevelCatalog.levels.size) {
            val existingMap = existingLevels.associateBy { it.levelNumber }
            val seeded = LevelCatalog.levels.map { cfg ->
                existingMap[cfg.levelNumber] ?: LevelProgressEntity(
                    levelNumber = cfg.levelNumber,
                    isUnlocked = (cfg.levelNumber == 1),
                    starsEarned = 0,
                    bestScore = 0,
                    completions = 0
                )
            }
            gameDao.upsertAllLevelProgress(seeded)
        }
    }

    suspend fun getOrCreateStats(): PlayerStatsEntity {
        return gameDao.getPlayerStatsOnce() ?: PlayerStatsEntity().also {
            gameDao.upsertPlayerStats(it)
        }
    }

    suspend fun toggleSound(): Boolean {
        val stats = getOrCreateStats()
        val updated = stats.copy(soundEnabled = !stats.soundEnabled)
        gameDao.upsertPlayerStats(updated)
        return updated.soundEnabled
    }

    suspend fun toggleHaptic(): Boolean {
        val stats = getOrCreateStats()
        val updated = stats.copy(hapticEnabled = !stats.hapticEnabled)
        gameDao.upsertPlayerStats(updated)
        return updated.hapticEnabled
    }

    suspend fun consumeOrBuyPowerUp(type: PowerUpType): Boolean {
        val stats = getOrCreateStats()
        val currentCount = when (type) {
            PowerUpType.HAMMER -> stats.hammerCount
            PowerUpType.BOMB -> stats.bombCount
            PowerUpType.REROLL -> stats.rerollCount
            PowerUpType.ROTATE -> stats.rotateCount
        }
        if (currentCount > 0) {
            val updated = when (type) {
                PowerUpType.HAMMER -> stats.copy(hammerCount = stats.hammerCount - 1)
                PowerUpType.BOMB -> stats.copy(bombCount = stats.bombCount - 1)
                PowerUpType.REROLL -> stats.copy(rerollCount = stats.rerollCount - 1)
                PowerUpType.ROTATE -> stats.copy(rotateCount = stats.rotateCount - 1)
            }
            gameDao.upsertPlayerStats(updated)
            return true
        } else if (stats.coins >= type.coinCost) {
            val updated = stats.copy(coins = stats.coins - type.coinCost)
            gameDao.upsertPlayerStats(updated)
            return true
        }
        return false
    }

    suspend fun buyPowerUpPack(type: PowerUpType): Boolean {
        val stats = getOrCreateStats()
        if (stats.coins < type.coinCost) return false
        val updated = when (type) {
            PowerUpType.HAMMER -> stats.copy(coins = stats.coins - type.coinCost, hammerCount = stats.hammerCount + 1)
            PowerUpType.BOMB -> stats.copy(coins = stats.coins - type.coinCost, bombCount = stats.bombCount + 1)
            PowerUpType.REROLL -> stats.copy(coins = stats.coins - type.coinCost, rerollCount = stats.rerollCount + 1)
            PowerUpType.ROTATE -> stats.copy(coins = stats.coins - type.coinCost, rotateCount = stats.rotateCount + 1)
        }
        gameDao.upsertPlayerStats(updated)
        return true
    }

    suspend fun recordLiveStatsDelta(
        scoreCandidate: Int,
        isClassic: Boolean,
        isDaily: Boolean,
        linesClearedDelta: Int,
        gemsCollectedDelta: Int,
        comboReached: Int,
        bonusCoins: Int
    ) {
        val stats = getOrCreateStats()
        val updated = stats.copy(
            classicBestScore = if (isClassic) maxOf(stats.classicBestScore, scoreCandidate) else stats.classicBestScore,
            dailyBestScore = if (isDaily) maxOf(stats.dailyBestScore, scoreCandidate) else stats.dailyBestScore,
            coins = stats.coins + bonusCoins,
            totalLinesCleared = stats.totalLinesCleared + linesClearedDelta,
            totalGemsCollected = stats.totalGemsCollected + gemsCollectedDelta,
            maxComboReached = maxOf(stats.maxComboReached, comboReached)
        )
        gameDao.upsertPlayerStats(updated)
    }

    suspend fun recordLevelVictory(
        levelNumber: Int,
        score: Int,
        stars: Int,
        rewardCoins: Int,
        maxCombo: Int,
        linesCleared: Int
    ) {
        val current = gameDao.getLevelProgress(levelNumber) ?: LevelProgressEntity(levelNumber = levelNumber, isUnlocked = true)
        val updatedLevel = current.copy(
            isUnlocked = true,
            starsEarned = maxOf(current.starsEarned, stars),
            bestScore = maxOf(current.bestScore, score),
            completions = current.completions + 1
        )
        gameDao.upsertLevelProgress(updatedLevel)

        // Unlock next level if exists
        if (levelNumber < LevelCatalog.levels.size) {
            val next = gameDao.getLevelProgress(levelNumber + 1)
                ?: LevelProgressEntity(levelNumber = levelNumber + 1)
            if (!next.isUnlocked) {
                gameDao.upsertLevelProgress(next.copy(isUnlocked = true))
            }
        }

        val stats = getOrCreateStats()
        gameDao.upsertPlayerStats(
            stats.copy(
                coins = stats.coins + rewardCoins,
                gamesPlayed = stats.gamesPlayed + 1,
                maxComboReached = maxOf(stats.maxComboReached, maxCombo)
            )
        )

        gameDao.insertGameHistory(
            GameHistoryEntity(
                modeTitle = "${levelNumber}-Bosqich",
                levelNumber = levelNumber,
                score = score,
                stars = stars,
                maxCombo = maxCombo,
                linesCleared = linesCleared
            )
        )
    }

    suspend fun recordGameOver(
        modeTitle: String,
        levelNumber: Int,
        score: Int,
        isClassic: Boolean,
        isDaily: Boolean,
        maxCombo: Int,
        linesCleared: Int
    ) {
        val stats = getOrCreateStats()
        val participationCoins = (score / 120).coerceIn(5, 60)
        gameDao.upsertPlayerStats(
            stats.copy(
                classicBestScore = if (isClassic) maxOf(stats.classicBestScore, score) else stats.classicBestScore,
                dailyBestScore = if (isDaily) maxOf(stats.dailyBestScore, score) else stats.dailyBestScore,
                coins = stats.coins + participationCoins,
                gamesPlayed = stats.gamesPlayed + 1,
                maxComboReached = maxOf(stats.maxComboReached, maxCombo)
            )
        )
        gameDao.insertGameHistory(
            GameHistoryEntity(
                modeTitle = modeTitle,
                levelNumber = levelNumber,
                score = score,
                stars = 0,
                maxCombo = maxCombo,
                linesCleared = linesCleared
            )
        )
    }
}
