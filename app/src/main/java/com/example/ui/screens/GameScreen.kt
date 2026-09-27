package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.PlayerStatsEntity
import com.example.model.BOARD_SIZE
import com.example.model.GameMode
import com.example.model.PowerUpType
import com.example.ui.components.CandidatePieceTray
import com.example.ui.components.InteractiveBlockBoard
import com.example.ui.components.drawJewelBlockCell
import com.example.ui.theme.ArcadeCellBorder
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.ArcadeSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.NeonGold
import com.example.ui.theme.VibrantMagenta
import com.example.viewmodel.GameUiState
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    uiState: GameUiState,
    stats: PlayerStatsEntity,
    onSelectCandidate: (Int) -> Unit,
    onUpdateDragHover: (Int, Int, Int) -> Unit,
    onClearDragHover: () -> Unit,
    onPlacePiece: (Int, Int, Int) -> Boolean,
    onBoardCellTapped: (Int, Int) -> Unit,
    onPowerUpClicked: (PowerUpType) -> Unit,
    onRetryGame: () -> Unit,
    onNextLevel: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    val animatedScore by animateIntAsState(
        targetValue = uiState.score,
        animationSpec = tween(durationMillis = 260),
        label = "scoreAnim"
    )

    var screenOriginInRoot by remember { mutableStateOf(Offset.Zero) }
    var boardOriginInRoot by remember { mutableStateOf(Offset.Zero) }
    var boardWidthPx by remember { mutableFloatStateOf(1f) }

    var draggingPieceIndex by remember { mutableStateOf<Int?>(null) }
    var dragPointerInRoot by remember { mutableStateOf(Offset.Zero) }

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                screenOriginInRoot = coords.positionInRoot()
            }
            .testTag("game_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Top Game Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier.testTag("game_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Orqaga",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = when (uiState.gameMode) {
                                GameMode.ADVENTURE -> "${uiState.currentLevel.levelNumber}-Bosqich: ${uiState.currentLevel.titleUz}"
                                GameMode.CLASSIC -> "Klassik Cheksiz"
                                GameMode.DAILY -> "Kunlik Oltin Sinov"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "${stats.coins} tanga",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonGold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (uiState.comboStreak >= 2) {
                        Surface(
                            color = VibrantMagenta,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "KOMBO x${uiState.comboStreak}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onRetryGame,
                        modifier = Modifier.testTag("game_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Qayta boshlash",
                            tint = ElectricCyan
                        )
                    }
                }
            }

            // 2. Score & Objectives Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ArcadeSurface)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "JORIY BALL",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$animatedScore",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = NeonGold,
                                modifier = Modifier.testTag("game_score_text")
                            )
                        }

                        if (uiState.gameMode == GameMode.CLASSIC) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "REKORD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${maxOf(stats.classicBestScore, uiState.score)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = EmeraldMint
                                )
                            }
                        } else {
                            // Adventure / Daily Star Progress Bar
                            val level = uiState.currentLevel
                            val progressFraction = (uiState.score.toFloat() / level.star3Threshold.toFloat())
                                .coerceIn(0f, 1f)
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.width(170.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Maqsad: ${level.targetScore}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                    Row {
                                        val s1 = uiState.score >= level.targetScore
                                        val s2 = uiState.score >= level.star2Threshold
                                        val s3 = uiState.score >= level.star3Threshold
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (s1) NeonGold else Color.White.copy(alpha = 0.3f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (s2) NeonGold else Color.White.copy(alpha = 0.3f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (s3) NeonGold else Color.White.copy(alpha = 0.3f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = NeonGold,
                                    trackColor = ArcadeSurfaceVariant
                                )
                            }
                        }
                    }

                    // Mission sub-goals row for Adventure & Daily modes
                    if (uiState.gameMode != GameMode.CLASSIC) {
                        val level = uiState.currentLevel
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MissionGoalChip(
                                label = "Ball",
                                current = uiState.score,
                                target = level.targetScore,
                                color = NeonGold,
                                modifier = Modifier.weight(1f)
                            )
                            if (level.targetGems > 0) {
                                MissionGoalChip(
                                    label = "Olmos",
                                    current = uiState.gemsCollectedInSession,
                                    target = level.targetGems,
                                    color = VibrantMagenta,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (level.targetIceBroken > 0) {
                                MissionGoalChip(
                                    label = "Muz",
                                    current = uiState.iceBrokenInSession,
                                    target = level.targetIceBroken,
                                    color = ElectricCyan,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (level.targetLines > 0) {
                                MissionGoalChip(
                                    label = "Qator",
                                    current = uiState.linesClearedInSession,
                                    target = level.targetLines,
                                    color = EmeraldMint,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Status / Helper Banner
            AnimatedVisibility(
                visible = uiState.statusBannerText != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = VibrantMagenta.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = uiState.statusBannerText.orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // 3. 8x8 Interactive Block Board
            InteractiveBlockBoard(
                board = uiState.board,
                candidates = uiState.candidates,
                hoverPreview = uiState.hoverPreview,
                particles = uiState.particles,
                popups = uiState.popups,
                onCellTapped = onBoardCellTapped,
                onBoardBoundsUpdated = { origin, width ->
                    boardOriginInRoot = origin
                    boardWidthPx = width.coerceAtLeast(1f)
                },
                modifier = Modifier.widthIn(max = 420.dp)
            )

            // 4. Candidate Piece Tray (Supports both Drag-and-Drop and Tap-to-Select!)
            CandidatePieceTray(
                candidates = uiState.candidates,
                selectedPieceIndex = uiState.selectedPieceIndex,
                onSelectPiece = onSelectCandidate,
                onDragStartPiece = { idx, pointerRoot ->
                    draggingPieceIndex = idx
                    dragPointerInRoot = pointerRoot
                    val piece = uiState.candidates.getOrNull(idx)
                    if (piece != null) {
                        val cellPx = boardWidthPx / BOARD_SIZE
                        val liftedY = pointerRoot.y - cellPx * 1.85f
                        val col = ((pointerRoot.x - boardOriginInRoot.x) / cellPx - piece.shape.colSpan / 2f).roundToInt()
                        val row = ((liftedY - boardOriginInRoot.y) / cellPx - piece.shape.rowSpan / 2f).roundToInt()
                        onUpdateDragHover(idx, row, col)
                    }
                },
                onDragMovePiece = { idx, pointerRoot ->
                    draggingPieceIndex = idx
                    dragPointerInRoot = pointerRoot
                    val piece = uiState.candidates.getOrNull(idx)
                    if (piece != null) {
                        val cellPx = boardWidthPx / BOARD_SIZE
                        val liftedY = pointerRoot.y - cellPx * 1.85f
                        val col = ((pointerRoot.x - boardOriginInRoot.x) / cellPx - piece.shape.colSpan / 2f).roundToInt()
                        val row = ((liftedY - boardOriginInRoot.y) / cellPx - piece.shape.rowSpan / 2f).roundToInt()
                        onUpdateDragHover(idx, row, col)
                    }
                },
                onDragEndPiece = { idx ->
                    val preview = uiState.hoverPreview
                    if (preview != null && preview.pieceIndex == idx && preview.isValid) {
                        onPlacePiece(idx, preview.anchorRow, preview.anchorCol)
                    } else {
                        onClearDragHover()
                    }
                    draggingPieceIndex = null
                },
                onDragCancelPiece = {
                    onClearDragHover()
                    draggingPieceIndex = null
                },
                modifier = Modifier.widthIn(max = 420.dp)
            )

            // 5. Power-Up Boosters Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PowerUpButton(
                    type = PowerUpType.HAMMER,
                    count = stats.hammerCount,
                    isActive = uiState.activePowerUp == PowerUpType.HAMMER,
                    onClick = { onPowerUpClicked(PowerUpType.HAMMER) },
                    modifier = Modifier.weight(1f)
                )
                PowerUpButton(
                    type = PowerUpType.BOMB,
                    count = stats.bombCount,
                    isActive = uiState.activePowerUp == PowerUpType.BOMB,
                    onClick = { onPowerUpClicked(PowerUpType.BOMB) },
                    modifier = Modifier.weight(1f)
                )
                PowerUpButton(
                    type = PowerUpType.ROTATE,
                    count = stats.rotateCount,
                    isActive = false,
                    onClick = { onPowerUpClicked(PowerUpType.ROTATE) },
                    modifier = Modifier.weight(1f)
                )
                PowerUpButton(
                    type = PowerUpType.REROLL,
                    count = stats.rerollCount,
                    isActive = false,
                    onClick = { onPowerUpClicked(PowerUpType.REROLL) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Floating Dragged Piece Overlay (follows finger lifted slightly upward)
        val activeDragIdx = draggingPieceIndex
        val draggedPiece = activeDragIdx?.let { uiState.candidates.getOrNull(it) }
        if (draggedPiece != null && !draggedPiece.isPlaced && boardWidthPx > 10f) {
            val cellPx = boardWidthPx / BOARD_SIZE
            val pieceWidthPx = draggedPiece.shape.colSpan * cellPx
            val pieceHeightPx = draggedPiece.shape.rowSpan * cellPx
            val topLeftX = dragPointerInRoot.x - screenOriginInRoot.x - pieceWidthPx / 2f
            val topLeftY = (dragPointerInRoot.y - cellPx * 1.85f) - screenOriginInRoot.y - pieceHeightPx / 2f

            val widthDp = with(density) { pieceWidthPx.toDp() }
            val heightDp = with(density) { pieceHeightPx.toDp() }

            Canvas(
                modifier = Modifier
                    .offset { IntOffset(topLeftX.roundToInt(), topLeftY.roundToInt()) }
                    .size(width = widthDp, height = heightDp)
            ) {
                for (offset in draggedPiece.shape.cells) {
                    drawJewelBlockCell(
                        topLeft = Offset(offset.col * cellPx, offset.row * cellPx),
                        cellSize = cellPx,
                        color = draggedPiece.color,
                        hasGem = (draggedPiece.gemCell == offset),
                        isBomb = (draggedPiece.bombCell == offset),
                        alpha = 0.92f
                    )
                }
            }
        }
    }

    // Victory Celebration Modal
    if (uiState.isVictory) {
        VictoryDialog(
            levelNumber = uiState.currentLevel.levelNumber,
            levelTitle = uiState.currentLevel.titleUz,
            score = uiState.score,
            stars = uiState.starsEarnedOnVictory,
            coinsWon = uiState.coinsEarnedOnVictory,
            isDaily = (uiState.gameMode == GameMode.DAILY),
            onNextLevel = onNextLevel,
            onReplay = onRetryGame,
            onBackToHome = onBackToHome
        )
    }

    // Game Over Modal
    if (uiState.isGameOver && !uiState.isVictory) {
        GameOverDialog(
            score = uiState.score,
            maxCombo = uiState.maxComboInSession,
            linesCleared = uiState.linesClearedInSession,
            canUseReroll = (stats.rerollCount > 0 || stats.coins >= PowerUpType.REROLL.coinCost),
            onUseRerollRescue = { onPowerUpClicked(PowerUpType.REROLL) },
            onRetry = onRetryGame,
            onBackToHome = onBackToHome
        )
    }
}

@Composable
private fun MissionGoalChip(
    label: String,
    current: Int,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val done = current >= target
    Surface(
        modifier = modifier,
        color = if (done) EmeraldMint.copy(alpha = 0.2f) else ArcadeSurfaceVariant,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (done) EmeraldMint else color.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (done) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldMint,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = "$label: ${current.coerceAtMost(target)}/$target",
                style = MaterialTheme.typography.labelSmall,
                color = if (done) EmeraldMint else color
            )
        }
    }
}

@Composable
private fun PowerUpButton(
    type: PowerUpType,
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (type) {
        PowerUpType.HAMMER -> Icons.Default.Build
        PowerUpType.BOMB -> Icons.Default.LocalFireDepartment
        PowerUpType.REROLL -> Icons.Default.Autorenew
        PowerUpType.ROTATE -> Icons.Default.RotateRight
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("powerup_${type.name}"),
        color = if (isActive) VibrantMagenta else ArcadeSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) NeonGold else ArcadeCellBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = type.titleUz,
                    tint = if (isActive) Color.White else NeonGold,
                    modifier = Modifier.size(18.dp)
                )
                Surface(
                    color = if (count > 0) EmeraldMint.copy(alpha = 0.22f) else ArcadeSurfaceVariant,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (count > 0) "x$count" else "${type.coinCost}T",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (count > 0) EmeraldMint else NeonGold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
            Text(
                text = type.titleUz,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
private fun VictoryDialog(
    levelNumber: Int,
    levelTitle: String,
    score: Int,
    stars: Int,
    coinsWon: Int,
    isDaily: Boolean,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onBackToHome: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, NeonGold, RoundedCornerShape(26.dp))
                .testTag("victory_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ArcadeSurface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isDaily) "KUNLIK SINOV YAKUNLANDI!" else "$levelNumber-BOSQICH ZABT ETILDI!",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonGold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = levelTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                // 3 Stars Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    for (i in 1..3) {
                        val earned = i <= stars
                        Icon(
                            imageVector = if (earned) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (earned) NeonGold else Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(if (i == 2) 52.dp else 42.dp)
                        )
                    }
                }

                Text(
                    text = "To'plangan Ball: $score",
                    style = MaterialTheme.typography.titleLarge,
                    color = ElectricCyan
                )

                Surface(
                    color = NeonGold.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold)
                ) {
                    Text(
                        text = "Mukofot: +$coinsWon tanga!",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonGold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (!isDaily) {
                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("next_level_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGold,
                            contentColor = Color(0xFF1A103C)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Keyingi Bosqich", fontWeight = FontWeight.Black)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onReplay,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Qayta", color = Color.White)
                    }
                    Button(
                        onClick = onBackToHome,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Menyu", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameOverDialog(
    score: Int,
    maxCombo: Int,
    linesCleared: Int,
    canUseReroll: Boolean,
    onUseRerollRescue: () -> Unit,
    onRetry: () -> Unit,
    onBackToHome: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, VibrantMagenta, RoundedCornerShape(26.dp))
                .testTag("game_over_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ArcadeSurface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "JOY QOLMADI!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VibrantMagenta
                )
                Text(
                    text = "Navbatdagi bloklar uchun bo'sh joy topilmadi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = ArcadeSurfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$score BALL",
                            style = MaterialTheme.typography.headlineLarge,
                            color = NeonGold
                        )
                        Text(
                            text = "Tozalangan qatorlar: $linesCleared • Maks Kombo: x$maxCombo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ElectricCyan
                        )
                    }
                }

                if (canUseReroll) {
                    Button(
                        onClick = onUseRerollRescue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("rescue_reroll_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldMint,
                            contentColor = Color(0xFF002612)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Bloklarni Yangilab Davom Etish", fontWeight = FontWeight.ExtraBold)
                    }
                }

                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("retry_game_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGold,
                        contentColor = Color(0xFF1A103C)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Qayta O'ynash", fontWeight = FontWeight.Black)
                }

                Button(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("gameover_home_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Asosiy Menyuga Qaytish", color = Color.White)
                }
            }
        }
    }
}
