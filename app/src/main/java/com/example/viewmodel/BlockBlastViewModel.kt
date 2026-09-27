package com.example.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.GameHistoryEntity
import com.example.data.local.LevelProgressEntity
import com.example.data.local.PlayerStatsEntity
import com.example.data.repository.GameRepository
import com.example.model.AppScreen
import com.example.model.BOARD_SIZE
import com.example.model.BlastParticle
import com.example.model.BlockColor
import com.example.model.BlockShape
import com.example.model.CandidatePiece
import com.example.model.CellContent
import com.example.model.FloatingPopup
import com.example.model.GameMode
import com.example.model.GridPos
import com.example.model.LevelCatalog
import com.example.model.LevelConfig
import com.example.model.PowerUpType
import com.example.model.ShapeCatalog
import com.example.ui.theme.CyanLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.NeonGold
import com.example.ui.theme.VibrantMagenta
import com.example.util.SoundEffectManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class HoverPreview(
    val pieceIndex: Int,
    val anchorRow: Int,
    val anchorCol: Int,
    val isValid: Boolean,
    val cellsToFill: Set<GridPos>,
    val rowsToClear: Set<Int>,
    val colsToClear: Set<Int>
)

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val gameMode: GameMode = GameMode.ADVENTURE,
    val currentLevel: LevelConfig = LevelCatalog.levels.first(),
    val board: List<List<CellContent?>> = List(BOARD_SIZE) { List(BOARD_SIZE) { null } },
    val candidates: List<CandidatePiece> = emptyList(),
    val selectedPieceIndex: Int? = null,
    val activePowerUp: PowerUpType? = null,
    val hoverPreview: HoverPreview? = null,
    val score: Int = 0,
    val comboStreak: Int = 0,
    val maxComboInSession: Int = 0,
    val linesClearedInSession: Int = 0,
    val gemsCollectedInSession: Int = 0,
    val iceBrokenInSession: Int = 0,
    val isVictory: Boolean = false,
    val starsEarnedOnVictory: Int = 0,
    val coinsEarnedOnVictory: Int = 0,
    val isGameOver: Boolean = false,
    val particles: List<BlastParticle> = emptyList(),
    val popups: List<FloatingPopup> = emptyList(),
    val statusBannerText: String? = null
)

class BlockBlastViewModel(
    private val repository: GameRepository,
    private val soundManager: SoundEffectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val levelProgressList: StateFlow<List<LevelProgressEntity>> =
        repository.levelProgressFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playerStats: StateFlow<PlayerStatsEntity> =
        repository.playerStatsFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = PlayerStatsEntity()
            ).let { flow ->
                val mapped = MutableStateFlow(PlayerStatsEntity())
                viewModelScope.launch {
                    flow.collect { stats ->
                        if (stats != null) mapped.value = stats
                    }
                }
                mapped.asStateFlow()
            }

    val recentHistory: StateFlow<List<GameHistoryEntity>> =
        repository.recentHistoryFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var nextUid = 1L

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            startLevel(1)
            _uiState.update { it.copy(currentScreen = AppScreen.HOME) }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen, activePowerUp = null, hoverPreview = null) }
    }

    fun startClassicMode() {
        val emptyBoard = List(BOARD_SIZE) { List<CellContent?>(BOARD_SIZE) { null } }
        val initialCandidates = generateCandidateBatch(
            board = emptyBoard,
            mode = GameMode.CLASSIC,
            level = null
        )
        _uiState.update {
            GameUiState(
                currentScreen = AppScreen.GAME,
                gameMode = GameMode.CLASSIC,
                currentLevel = LevelCatalog.levels.first(),
                board = emptyBoard,
                candidates = initialCandidates,
                selectedPieceIndex = 0
            )
        }
    }

    fun startDailyChallenge() {
        val dailyLevel = LevelConfig(
            levelNumber = 99,
            titleUz = "Kunlik Oltin Sinov",
            subtitleUz = "Maxsus kunlik maydonda 1800 ball va 8 ta javohir to'plang!",
            difficultyLabel = "Kunlik",
            targetScore = 1800,
            targetGems = 8,
            targetIceBroken = 6,
            targetLines = 7,
            star2Threshold = 2400,
            star3Threshold = 3200,
            rewardCoins = 140,
            accentColor = BlockColor.AMBER,
            boardBuilder = {
                val g = Array(BOARD_SIZE) { arrayOfNulls<CellContent>(BOARD_SIZE) }
                for (i in 1..6) {
                    if (i != 3 && i != 4) {
                        g[i][i] = CellContent(BlockColor.AMBER, hasGem = true, iceLayers = 1)
                        g[i][7 - i] = CellContent(BlockColor.CYAN, hasGem = true)
                    }
                }
                g[3][3] = CellContent(BlockColor.RUBY, isBomb = true)
                g[4][4] = CellContent(BlockColor.RUBY, isBomb = true)
                g.map { it.toList() }
            }
        )
        val board = dailyLevel.boardBuilder()
        val initialCandidates = generateCandidateBatch(board, GameMode.DAILY, dailyLevel)
        _uiState.update {
            GameUiState(
                currentScreen = AppScreen.GAME,
                gameMode = GameMode.DAILY,
                currentLevel = dailyLevel,
                board = board,
                candidates = initialCandidates,
                selectedPieceIndex = 0
            )
        }
    }

    fun startLevel(levelNumber: Int) {
        val config = LevelCatalog.getLevel(levelNumber)
        val board = config.boardBuilder()
        val initialCandidates = generateCandidateBatch(board, GameMode.ADVENTURE, config)
        _uiState.update {
            GameUiState(
                currentScreen = AppScreen.GAME,
                gameMode = GameMode.ADVENTURE,
                currentLevel = config,
                board = board,
                candidates = initialCandidates,
                selectedPieceIndex = 0
            )
        }
    }

    fun retryCurrentGame() {
        val state = _uiState.value
        when (state.gameMode) {
            GameMode.ADVENTURE -> startLevel(state.currentLevel.levelNumber)
            GameMode.CLASSIC -> startClassicMode()
            GameMode.DAILY -> startDailyChallenge()
        }
    }

    fun startNextLevel() {
        val currentNum = _uiState.value.currentLevel.levelNumber
        val nextNum = if (currentNum < LevelCatalog.levels.size) currentNum + 1 else 1
        startLevel(nextNum)
    }

    fun selectCandidatePiece(index: Int) {
        val state = _uiState.value
        val piece = state.candidates.getOrNull(index) ?: return
        if (piece.isPlaced) return
        val stats = playerStats.value
        soundManager.playPickup(stats.soundEnabled, stats.hapticEnabled)
        _uiState.update {
            it.copy(
                selectedPieceIndex = index,
                activePowerUp = null,
                hoverPreview = null
            )
        }
    }

    fun updateDragHover(pieceIndex: Int, anchorRow: Int, anchorCol: Int) {
        val state = _uiState.value
        val piece = state.candidates.getOrNull(pieceIndex) ?: return
        if (piece.isPlaced) return

        if (anchorRow !in -2..BOARD_SIZE || anchorCol !in -2..BOARD_SIZE) {
            if (state.hoverPreview != null) {
                _uiState.update { it.copy(hoverPreview = null) }
            }
            return
        }

        val valid = canPlacePiece(state.board, piece.shape, anchorRow, anchorCol)
        val cellsToFill = if (valid) {
            piece.shape.cells.map { GridPos(anchorRow + it.row, anchorCol + it.col) }.toSet()
        } else {
            emptySet()
        }

        val (rowsToClear, colsToClear) = if (valid) {
            computePreviewLines(state.board, piece, anchorRow, anchorCol)
        } else {
            emptySet<Int>() to emptySet<Int>()
        }

        val preview = HoverPreview(
            pieceIndex = pieceIndex,
            anchorRow = anchorRow,
            anchorCol = anchorCol,
            isValid = valid,
            cellsToFill = cellsToFill,
            rowsToClear = rowsToClear,
            colsToClear = colsToClear
        )
        _uiState.update {
            it.copy(
                selectedPieceIndex = pieceIndex,
                activePowerUp = null,
                hoverPreview = preview
            )
        }
    }

    fun clearDragHover() {
        _uiState.update { it.copy(hoverPreview = null) }
    }

    fun onBoardCellTapped(row: Int, col: Int) {
        val state = _uiState.value
        if (state.isVictory || state.isGameOver) return

        // Handle active targeted power-up (Hammer or Bomb)
        val activePower = state.activePowerUp
        if (activePower == PowerUpType.HAMMER || activePower == PowerUpType.BOMB) {
            executeBoardPowerUp(activePower, row, col)
            return
        }

        // Otherwise place currently selected piece at (row, col) (or centered if needed)
        val selectedIdx = state.selectedPieceIndex ?: state.candidates.indexOfFirst { !it.isPlaced }.takeIf { it >= 0 } ?: return
        val piece = state.candidates.getOrNull(selectedIdx) ?: return
        if (piece.isPlaced) return

        // Try direct anchor at (row, col), or smart offset if player tapped center of where shape fits
        val targetAnchor = findBestTapAnchor(state.board, piece.shape, row, col)
        if (targetAnchor != null) {
            placePiece(selectedIdx, targetAnchor.row, targetAnchor.col)
        } else {
            showBanner("Bu joyga sig'maydi!")
        }
    }

    private fun findBestTapAnchor(
        board: List<List<CellContent?>>,
        shape: BlockShape,
        tapRow: Int,
        tapCol: Int
    ): GridPos? {
        if (canPlacePiece(board, shape, tapRow, tapCol)) {
            return GridPos(tapRow, tapCol)
        }
        // Check if centering the shape on (tapRow, tapCol) is valid
        val centerRowAnchor = tapRow - shape.rowSpan / 2
        val centerColAnchor = tapCol - shape.colSpan / 2
        if (canPlacePiece(board, shape, centerRowAnchor, centerColAnchor)) {
            return GridPos(centerRowAnchor, centerColAnchor)
        }
        return null
    }

    fun placePiece(pieceIndex: Int, anchorRow: Int, anchorCol: Int): Boolean {
        val state = _uiState.value
        if (state.isVictory || state.isGameOver) return false
        val piece = state.candidates.getOrNull(pieceIndex) ?: return false
        if (piece.isPlaced) return false

        if (!canPlacePiece(state.board, piece.shape, anchorRow, anchorCol)) {
            _uiState.update { it.copy(hoverPreview = null) }
            return false
        }

        val stats = playerStats.value
        val mutableBoard = state.board.map { it.toMutableList() }.toMutableList()

        // 1. Place cells onto the board
        for (offset in piece.shape.cells) {
            val r = anchorRow + offset.row
            val c = anchorCol + offset.col
            mutableBoard[r][c] = CellContent(
                color = piece.color,
                hasGem = (piece.gemCell == offset),
                iceLayers = 0,
                isBomb = (piece.bombCell == offset)
            )
        }

        val placementScore = piece.shape.cells.size * 12

        // 2. Check for completed rows & columns
        val fullRows = (0 until BOARD_SIZE).filter { r ->
            (0 until BOARD_SIZE).all { c -> mutableBoard[r][c] != null }
        }
        val fullCols = (0 until BOARD_SIZE).filter { c ->
            (0 until BOARD_SIZE).all { r -> mutableBoard[r][c] != null }
        }

        val totalLinesCleared = fullRows.size + fullCols.size
        var newCombo = if (totalLinesCleared > 0) state.comboStreak + 1 else 0
        var gemsDelta = 0
        var iceDelta = 0
        var bombExplosions = 0
        val spawnedParticles = mutableListOf<BlastParticle>()
        val spawnedPopups = mutableListOf<FloatingPopup>()

        if (totalLinesCleared > 0) {
            val cellsToHit = mutableSetOf<GridPos>()
            for (r in fullRows) {
                for (c in 0 until BOARD_SIZE) cellsToHit.add(GridPos(r, c))
            }
            for (c in fullCols) {
                for (r in 0 until BOARD_SIZE) cellsToHit.add(GridPos(r, c))
            }

            // Check if any cell in cellsToHit is a bomb ( triggers 3x3 chain blast!)
            val bombQueue = ArrayDeque<GridPos>()
            for (pos in cellsToHit) {
                if (mutableBoard[pos.row][pos.col]?.isBomb == true) {
                    bombQueue.add(pos)
                }
            }
            val visitedBombs = bombQueue.toMutableSet()
            while (bombQueue.isNotEmpty()) {
                val bPos = bombQueue.removeFirst()
                bombExplosions++
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        val nr = bPos.row + dr
                        val nc = bPos.col + dc
                        if (nr in 0 until BOARD_SIZE && nc in 0 until BOARD_SIZE) {
                            val np = GridPos(nr, nc)
                            cellsToHit.add(np)
                            if (mutableBoard[nr][nc]?.isBomb == true && visitedBombs.add(np)) {
                                bombQueue.add(np)
                            }
                        }
                    }
                }
            }

            // Apply damage / clear to all hit cells
            for (pos in cellsToHit) {
                val cell = mutableBoard[pos.row][pos.col] ?: continue
                spawnCellParticles(spawnedParticles, pos.row, pos.col, cell.color.baseColor)
                if (cell.iceLayers >= 2) {
                    // Thick ice cracks down to 1 layer
                    mutableBoard[pos.row][pos.col] = cell.copy(iceLayers = 1)
                    iceDelta++
                } else {
                    if (cell.iceLayers == 1) {
                        iceDelta++
                    }
                    if (cell.hasGem) {
                        gemsDelta++
                    }
                    mutableBoard[pos.row][pos.col] = null
                }
            }

            soundManager.playLineClear(totalLinesCleared, newCombo, stats.soundEnabled, stats.hapticEnabled)
        } else {
            soundManager.playPlace(stats.soundEnabled, stats.hapticEnabled)
        }

        // Calculate line clear + combo + gem + ice + clean board bonus score
        val lineBasePoints = when (totalLinesCleared) {
            0 -> 0
            1 -> 120
            2 -> 300
            3 -> 550
            4 -> 900
            else -> 1300
        }
        val comboMultiplier = if (newCombo >= 2) newCombo else 1
        val linePoints = lineBasePoints * comboMultiplier
        val specialPoints = gemsDelta * 60 + iceDelta * 45 + bombExplosions * 100

        val isBoardCompletelyEmpty = mutableBoard.all { row -> row.all { it == null } }
        val cleanSweepBonus = if (totalLinesCleared > 0 && isBoardCompletelyEmpty) 500 else 0

        val totalTurnPoints = placementScore + linePoints + specialPoints + cleanSweepBonus
        val updatedScore = state.score + totalTurnPoints
        val updatedMaxCombo = maxOf(state.maxComboInSession, newCombo)
        val updatedLines = state.linesClearedInSession + totalLinesCleared
        val updatedGems = state.gemsCollectedInSession + gemsDelta
        val updatedIce = state.iceBrokenInSession + iceDelta

        // Create exciting popup messages
        if (totalLinesCleared > 0) {
            val headline = when {
                cleanSweepBonus > 0 -> "TOZA DOSKA! +$totalTurnPoints"
                totalLinesCleared >= 4 -> "AFSONAVIY BLAST! +$totalTurnPoints"
                totalLinesCleared == 3 -> "MEGA UCHLIK! +$totalTurnPoints"
                totalLinesCleared == 2 -> "QO'SHALOQ ZARBA! +$totalTurnPoints"
                newCombo >= 3 -> "KOMBO x$newCombo! +$totalTurnPoints"
                else -> "AJOYIB! +$totalTurnPoints"
            }
            val sub = buildList {
                if (newCombo >= 2) add("Kombo x$newCombo")
                if (gemsDelta > 0) add("+$gemsDelta Olmos")
                if (iceDelta > 0) add("+$iceDelta Muz eridi")
                if (bombExplosions > 0) add("Bomba Portladi!")
            }.joinToString(" • ")

            spawnedPopups.add(
                FloatingPopup(
                    id = nextUid++,
                    text = headline,
                    subtitle = sub,
                    color = if (newCombo >= 2 || totalLinesCleared >= 2) NeonGold else CyanLight,
                    row = anchorRow.toFloat().coerceIn(1.5f, 5.5f),
                    col = 3.5f
                )
            )
        }

        // Update candidate pieces list
        val updatedCandidates = state.candidates.mapIndexed { idx, cand ->
            if (idx == pieceIndex) cand.copy(isPlaced = true) else cand
        }

        val finalBoard = mutableBoard.map { it.toList() }
        val allPlaced = updatedCandidates.all { it.isPlaced }
        val nextCandidates = if (allPlaced) {
            generateCandidateBatch(finalBoard, state.gameMode, state.currentLevel)
        } else {
            updatedCandidates
        }
        val nextSelectedIdx = nextCandidates.indexOfFirst { !it.isPlaced }.takeIf { it >= 0 }

        // Persist live stats & coins earned from combos
        val comboCoinBonus = if (newCombo >= 2) newCombo * 3 else 0
        viewModelScope.launch {
            repository.recordLiveStatsDelta(
                scoreCandidate = updatedScore,
                isClassic = (state.gameMode == GameMode.CLASSIC),
                isDaily = (state.gameMode == GameMode.DAILY),
                linesClearedDelta = totalLinesCleared,
                gemsCollectedDelta = gemsDelta,
                comboReached = newCombo,
                bonusCoins = comboCoinBonus
            )
        }

        // Check Victory in Adventure or Daily mode
        val level = state.currentLevel
        val isLevelMode = state.gameMode == GameMode.ADVENTURE || state.gameMode == GameMode.DAILY
        val victoryAchieved = isLevelMode &&
            updatedScore >= level.targetScore &&
            updatedGems >= level.targetGems &&
            updatedIce >= level.targetIceBroken &&
            updatedLines >= level.targetLines

        if (victoryAchieved) {
            val stars = when {
                updatedScore >= level.star3Threshold -> 3
                updatedScore >= level.star2Threshold -> 2
                else -> 1
            }
            val coinReward = level.rewardCoins + (stars - 1) * 25
            soundManager.playVictory(stats.soundEnabled, stats.hapticEnabled)
            viewModelScope.launch {
                if (state.gameMode == GameMode.ADVENTURE) {
                    repository.recordLevelVictory(
                        levelNumber = level.levelNumber,
                        score = updatedScore,
                        stars = stars,
                        rewardCoins = coinReward,
                        maxCombo = updatedMaxCombo,
                        linesCleared = updatedLines
                    )
                } else {
                    repository.recordLiveStatsDelta(
                        scoreCandidate = updatedScore,
                        isClassic = false,
                        isDaily = true,
                        linesClearedDelta = 0,
                        gemsCollectedDelta = 0,
                        comboReached = updatedMaxCombo,
                        bonusCoins = coinReward
                    )
                }
            }
            _uiState.update {
                it.copy(
                    board = finalBoard,
                    candidates = nextCandidates,
                    selectedPieceIndex = nextSelectedIdx,
                    hoverPreview = null,
                    score = updatedScore,
                    comboStreak = newCombo,
                    maxComboInSession = updatedMaxCombo,
                    linesClearedInSession = updatedLines,
                    gemsCollectedInSession = updatedGems,
                    iceBrokenInSession = updatedIce,
                    isVictory = true,
                    starsEarnedOnVictory = stars,
                    coinsEarnedOnVictory = coinReward,
                    particles = it.particles + spawnedParticles,
                    popups = it.popups + spawnedPopups
                )
            }
            scheduleEffectCleanup()
            return true
        }

        // Check if any remaining candidate piece can fit on the board
        val hasValidMoves = hasAnyValidMove(finalBoard, nextCandidates)
        if (!hasValidMoves) {
            soundManager.playGameOver(stats.soundEnabled, stats.hapticEnabled)
            viewModelScope.launch {
                repository.recordGameOver(
                    modeTitle = state.gameMode.titleUz,
                    levelNumber = level.levelNumber,
                    score = updatedScore,
                    isClassic = (state.gameMode == GameMode.CLASSIC),
                    isDaily = (state.gameMode == GameMode.DAILY),
                    maxCombo = updatedMaxCombo,
                    linesCleared = updatedLines
                )
            }
        }

        _uiState.update {
            it.copy(
                board = finalBoard,
                candidates = nextCandidates,
                selectedPieceIndex = nextSelectedIdx,
                hoverPreview = null,
                score = updatedScore,
                comboStreak = newCombo,
                maxComboInSession = updatedMaxCombo,
                linesClearedInSession = updatedLines,
                gemsCollectedInSession = updatedGems,
                iceBrokenInSession = updatedIce,
                isGameOver = !hasValidMoves,
                particles = it.particles + spawnedParticles,
                popups = it.popups + spawnedPopups
            )
        }
        scheduleEffectCleanup()
        return true
    }

    fun onPowerUpClicked(type: PowerUpType) {
        val state = _uiState.value
        if (state.isVictory) return

        // If game over, allow using power-up to rescue the board!
        when (type) {
            PowerUpType.HAMMER, PowerUpType.BOMB -> {
                val nextActive = if (state.activePowerUp == type) null else type
                val prompt = when (nextActive) {
                    PowerUpType.HAMMER -> "Bolg'a: Sindirish uchun doskadagi blokni bosing!"
                    PowerUpType.BOMB -> "Bomba 3x3: Portlatish uchun doskadagi joyni bosing!"
                    else -> null
                }
                _uiState.update {
                    it.copy(
                        activePowerUp = nextActive,
                        statusBannerText = prompt,
                        isGameOver = false
                    )
                }
            }
            PowerUpType.REROLL -> {
                viewModelScope.launch {
                    val ok = repository.consumeOrBuyPowerUp(PowerUpType.REROLL)
                    if (!ok) {
                        showBanner("Tangalar yetarli emas!")
                        return@launch
                    }
                    val stats = playerStats.value
                    soundManager.playPowerUp(stats.soundEnabled, stats.hapticEnabled)
                    val fresh = generateCandidateBatch(state.board, state.gameMode, state.currentLevel)
                    _uiState.update {
                        it.copy(
                            candidates = fresh,
                            selectedPieceIndex = 0,
                            activePowerUp = null,
                            isGameOver = !hasAnyValidMove(state.board, fresh),
                            statusBannerText = "3 ta yangi blok yaratildi!"
                        )
                    }
                }
            }
            PowerUpType.ROTATE -> {
                viewModelScope.launch {
                    val ok = repository.consumeOrBuyPowerUp(PowerUpType.ROTATE)
                    if (!ok) {
                        showBanner("Tangalar yetarli emas!")
                        return@launch
                    }
                    val stats = playerStats.value
                    soundManager.playPowerUp(stats.soundEnabled, stats.hapticEnabled)
                    val rotated = state.candidates.map { cand ->
                        if (cand.isPlaced) cand else cand.copy(
                            shape = cand.shape.rotate90(),
                            gemCell = null,
                            bombCell = null
                        )
                    }
                    _uiState.update {
                        it.copy(
                            candidates = rotated,
                            activePowerUp = null,
                            isGameOver = !hasAnyValidMove(state.board, rotated),
                            statusBannerText = "Bloklar 90° ga burildi!"
                        )
                    }
                }
            }
        }
    }

    private fun executeBoardPowerUp(type: PowerUpType, centerRow: Int, centerCol: Int) {
        val state = _uiState.value
        val targetRange = if (type == PowerUpType.HAMMER) 0..0 else -1..1
        val affectedPositions = mutableListOf<GridPos>()
        for (dr in targetRange) {
            for (dc in targetRange) {
                val r = centerRow + dr
                val c = centerCol + dc
                if (r in 0 until BOARD_SIZE && c in 0 until BOARD_SIZE && state.board[r][c] != null) {
                    affectedPositions.add(GridPos(r, c))
                }
            }
        }

        if (affectedPositions.isEmpty()) {
            showBanner("Bu yerda blok yo'q!")
            return
        }

        viewModelScope.launch {
            val ok = repository.consumeOrBuyPowerUp(type)
            if (!ok) {
                showBanner("Tangalar yetarli emas!")
                _uiState.update { it.copy(activePowerUp = null) }
                return@launch
            }

            val stats = playerStats.value
            soundManager.playPowerUp(stats.soundEnabled, stats.hapticEnabled)

            val mutableBoard = state.board.map { it.toMutableList() }.toMutableList()
            val particles = mutableListOf<BlastParticle>()
            var gemsFound = 0
            var iceFound = 0

            for (pos in affectedPositions) {
                val cell = mutableBoard[pos.row][pos.col] ?: continue
                spawnCellParticles(particles, pos.row, pos.col, cell.color.baseColor)
                if (cell.hasGem) gemsFound++
                if (cell.iceLayers > 0) iceFound++
                mutableBoard[pos.row][pos.col] = null
            }

            val bonusScore = affectedPositions.size * 40 + gemsFound * 60 + iceFound * 45
            val newBoard = mutableBoard.map { it.toList() }
            val newScore = state.score + bonusScore
            val newGems = state.gemsCollectedInSession + gemsFound
            val newIce = state.iceBrokenInSession + iceFound

            val popup = FloatingPopup(
                id = nextUid++,
                text = if (type == PowerUpType.BOMB) "BOMBA PORTLADI! +$bonusScore" else "SINDIRILDI! +$bonusScore",
                color = VibrantMagenta,
                row = centerRow.toFloat().coerceIn(1.5f, 5.5f),
                col = centerCol.toFloat().coerceIn(1.5f, 5.5f)
            )

            _uiState.update {
                it.copy(
                    board = newBoard,
                    score = newScore,
                    gemsCollectedInSession = newGems,
                    iceBrokenInSession = newIce,
                    activePowerUp = null,
                    isGameOver = !hasAnyValidMove(newBoard, it.candidates),
                    statusBannerText = null,
                    particles = it.particles + particles,
                    popups = it.popups + popup
                )
            }
            scheduleEffectCleanup()
        }
    }

    fun buyBoosterInShop(type: PowerUpType) {
        viewModelScope.launch {
            val success = repository.buyPowerUpPack(type)
            if (success) {
                val stats = playerStats.value
                soundManager.playPowerUp(stats.soundEnabled, stats.hapticEnabled)
                showBanner("${type.titleUz} xarid qilindi (+1)!")
            } else {
                showBanner("Tangalar yetarli emas!")
            }
        }
    }

    fun toggleSoundSetting() {
        viewModelScope.launch { repository.toggleSound() }
    }

    fun toggleHapticSetting() {
        viewModelScope.launch { repository.toggleHaptic() }
    }

    private fun showBanner(message: String) {
        _uiState.update { it.copy(statusBannerText = message) }
        viewModelScope.launch {
            delay(2200)
            _uiState.update {
                if (it.statusBannerText == message) it.copy(statusBannerText = null) else it
            }
        }
    }

    private fun scheduleEffectCleanup() {
        viewModelScope.launch {
            delay(950)
            _uiState.update {
                it.copy(
                    particles = emptyList(),
                    popups = emptyList()
                )
            }
        }
    }

    private fun spawnCellParticles(
        out: MutableList<BlastParticle>,
        row: Int,
        col: Int,
        color: Color
    ) {
        repeat(4) { i ->
            val angle = (i * 90 + Random.nextInt(-25, 25)) * (Math.PI / 180.0)
            out.add(
                BlastParticle(
                    id = nextUid++,
                    row = row + 0.5f,
                    col = col + 0.5f,
                    vx = kotlin.math.cos(angle).toFloat() * Random.nextFloat() * 1.8f,
                    vy = kotlin.math.sin(angle).toFloat() * Random.nextFloat() * 1.8f,
                    color = if (i % 2 == 0) color else EmeraldLight,
                    sizeDp = Random.nextInt(6, 12).toFloat()
                )
            )
        }
    }

    fun canPlacePiece(
        board: List<List<CellContent?>>,
        shape: BlockShape,
        anchorRow: Int,
        anchorCol: Int
    ): Boolean {
        for (cell in shape.cells) {
            val r = anchorRow + cell.row
            val c = anchorCol + cell.col
            if (r !in 0 until BOARD_SIZE || c !in 0 until BOARD_SIZE) return false
            if (board[r][c] != null) return false
        }
        return true
    }

    private fun computePreviewLines(
        board: List<List<CellContent?>>,
        piece: CandidatePiece,
        anchorRow: Int,
        anchorCol: Int
    ): Pair<Set<Int>, Set<Int>> {
        val placedSet = piece.shape.cells.map { GridPos(anchorRow + it.row, anchorCol + it.col) }.toSet()
        val rowsToClear = (0 until BOARD_SIZE).filter { r ->
            (0 until BOARD_SIZE).all { c -> board[r][c] != null || GridPos(r, c) in placedSet }
        }.toSet()
        val colsToClear = (0 until BOARD_SIZE).filter { c ->
            (0 until BOARD_SIZE).all { r -> board[r][c] != null || GridPos(r, c) in placedSet }
        }.toSet()
        return rowsToClear to colsToClear
    }

    private fun hasAnyValidMove(
        board: List<List<CellContent?>>,
        candidates: List<CandidatePiece>
    ): Boolean {
        val active = candidates.filter { !it.isPlaced }
        if (active.isEmpty()) return true
        for (cand in active) {
            if (canShapeFitAnywhere(board, cand.shape)) return true
        }
        return false
    }

    private fun canShapeFitAnywhere(board: List<List<CellContent?>>, shape: BlockShape): Boolean {
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                if (canPlacePiece(board, shape, r, c)) return true
            }
        }
        return false
    }

    private fun generateCandidateBatch(
        board: List<List<CellContent?>>,
        mode: GameMode,
        level: LevelConfig?
    ): List<CandidatePiece> {
        val colors = BlockColor.playableColors.shuffled().take(3)
        val occupiedCount = board.sumOf { row -> row.count { it != null } }
        val levelNum = level?.levelNumber ?: 5

        // Pool of shapes weighted by board fullness and level difficulty
        val shapes = (0 until 3).map { index ->
            if (index == 0) {
                // Guarantee at least the first piece fits on the current board
                val fittingPool = (ShapeCatalog.easyShapes + ShapeCatalog.mediumShapes)
                    .filter { canShapeFitAnywhere(board, it) }
                fittingPool.randomOrNull() ?: ShapeCatalog.easyShapes.first()
            } else {
                val roll = Random.nextInt(100)
                when {
                    occupiedCount > 40 -> ShapeCatalog.easyShapes.random()
                    levelNum <= 3 -> if (roll < 65) ShapeCatalog.easyShapes.random() else ShapeCatalog.mediumShapes.random()
                    levelNum <= 8 -> when {
                        roll < 40 -> ShapeCatalog.easyShapes.random()
                        roll < 80 -> ShapeCatalog.mediumShapes.random()
                        else -> ShapeCatalog.hardShapes.random()
                    }
                    else -> when {
                        roll < 30 -> ShapeCatalog.easyShapes.random()
                        roll < 70 -> ShapeCatalog.mediumShapes.random()
                        else -> ShapeCatalog.hardShapes.random()
                    }
                }
            }
        }

        val needsGems = (level?.targetGems ?: 0) > 0 || mode != GameMode.ADVENTURE

        return shapes.mapIndexed { idx, shape ->
            val includeGem = needsGems && (idx == 0 || Random.nextInt(100) < 35)
            val gemPos = if (includeGem) shape.cells.random() else null
            val includeBomb = !includeGem && Random.nextInt(100) < 16
            val bombPos = if (includeBomb) shape.cells.random() else null

            CandidatePiece(
                uid = nextUid++,
                shape = shape,
                color = colors[idx % colors.size],
                gemCell = gemPos,
                bombCell = bombPos,
                isPlaced = false
            )
        }
    }

    class Factory(
        private val repository: GameRepository,
        private val soundManager: SoundEffectManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BlockBlastViewModel(repository, soundManager) as T
        }
    }
}
