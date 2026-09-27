package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BOARD_SIZE
import com.example.model.BlastParticle
import com.example.model.BlockColor
import com.example.model.CandidatePiece
import com.example.model.CellContent
import com.example.model.FloatingPopup
import com.example.model.GridPos
import com.example.ui.theme.ArcadeBoardBg
import com.example.ui.theme.ArcadeCellBorder
import com.example.ui.theme.ArcadeCellEmpty
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.IceBorder
import com.example.ui.theme.IceOverlay
import com.example.ui.theme.NeonGold
import com.example.viewmodel.HoverPreview

fun DrawScope.drawJewelBlockCell(
    topLeft: Offset,
    cellSize: Float,
    color: BlockColor,
    hasGem: Boolean = false,
    iceLayers: Int = 0,
    isBomb: Boolean = false,
    alpha: Float = 1f,
    highlightClear: Boolean = false
) {
    val pad = cellSize * 0.05f
    val x = topLeft.x + pad
    val y = topLeft.y + pad
    val w = (cellSize - pad * 2).coerceAtLeast(2f)
    val corner = CornerRadius(w * 0.22f, w * 0.22f)

    // 1. Deep 3D bottom-right bevel base
    drawRoundRect(
        color = color.darkColor.copy(alpha = alpha),
        topLeft = Offset(x, y),
        size = Size(w, w),
        cornerRadius = corner
    )

    // 2. Main vibrant gradient body
    val bodyInset = w * 0.07f
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                color.lightColor.copy(alpha = alpha),
                color.baseColor.copy(alpha = alpha),
                color.darkColor.copy(alpha = alpha)
            ),
            start = Offset(x, y),
            end = Offset(x + w, y + w)
        ),
        topLeft = Offset(x + bodyInset * 0.4f, y + bodyInset * 0.4f),
        size = Size(w - bodyInset * 1.2f, w - bodyInset * 1.2f),
        cornerRadius = corner
    )

    // 3. Inner jewel facet & top-left specular shine
    val facetInset = w * 0.20f
    drawRoundRect(
        color = color.baseColor.copy(alpha = alpha),
        topLeft = Offset(x + facetInset, y + facetInset),
        size = Size(w - facetInset * 2f, w - facetInset * 2f),
        cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
    )

    // Specular glossy shine bar at top-left
    drawRoundRect(
        color = Color.White.copy(alpha = 0.42f * alpha),
        topLeft = Offset(x + w * 0.14f, y + w * 0.11f),
        size = Size(w * 0.48f, w * 0.13f),
        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
    )

    // 4. Ice layer overlay
    if (iceLayers > 0) {
        drawRoundRect(
            color = IceOverlay.copy(alpha = if (iceLayers >= 2) 0.78f * alpha else 0.52f * alpha),
            topLeft = Offset(x, y),
            size = Size(w, w),
            cornerRadius = corner
        )
        drawRoundRect(
            color = IceBorder.copy(alpha = 0.9f * alpha),
            topLeft = Offset(x + 1.5f, y + 1.5f),
            size = Size(w - 3f, w - 3f),
            cornerRadius = corner,
            style = Stroke(width = w * 0.07f)
        )
        // Frost crack lines
        drawLine(
            color = Color.White.copy(alpha = 0.85f * alpha),
            start = Offset(x + w * 0.2f, y + w * 0.25f),
            end = Offset(x + w * 0.55f, y + w * 0.5f),
            strokeWidth = w * 0.05f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.85f * alpha),
            start = Offset(x + w * 0.55f, y + w * 0.5f),
            end = Offset(x + w * 0.8f, y + w * 0.35f),
            strokeWidth = w * 0.05f
        )
        if (iceLayers >= 2) {
            drawLine(
                color = Color.White.copy(alpha = 0.9f * alpha),
                start = Offset(x + w * 0.3f, y + w * 0.78f),
                end = Offset(x + w * 0.72f, y + w * 0.52f),
                strokeWidth = w * 0.06f
            )
        }
    }

    // 5. Sparkling Gem diamond overlay
    if (hasGem) {
        val cx = x + w * 0.5f
        val cy = y + w * 0.5f
        val r = w * 0.28f
        val diamondPath = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r, cy)
            lineTo(cx, cy + r)
            lineTo(cx - r, cy)
            close()
        }
        drawPath(
            path = diamondPath,
            brush = Brush.radialGradient(
                colors = listOf(Color.White, NeonGold, Color(0xFFFF6F00)),
                center = Offset(cx, cy),
                radius = r * 1.2f
            )
        )
        drawPath(
            path = diamondPath,
            color = Color.White.copy(alpha = 0.95f * alpha),
            style = Stroke(width = w * 0.05f)
        )
    }

    // 6. Bomb indicator overlay
    if (isBomb) {
        val cx = x + w * 0.5f
        val cy = y + w * 0.54f
        val radius = w * 0.24f
        drawCircle(
            color = Color(0xFF1C1C24).copy(alpha = alpha),
            radius = radius,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFFFF5252).copy(alpha = alpha),
            radius = radius * 0.55f,
            center = Offset(cx, cy)
        )
        // Spark at top of bomb
        drawCircle(
            color = NeonGold.copy(alpha = alpha),
            radius = radius * 0.38f,
            center = Offset(cx + radius * 0.5f, cy - radius * 0.75f)
        )
    }

    // 7. Golden pulse outline when part of a row/column about to clear
    if (highlightClear) {
        drawRoundRect(
            color = NeonGold,
            topLeft = Offset(x, y),
            size = Size(w, w),
            cornerRadius = corner,
            style = Stroke(width = w * 0.11f)
        )
    }
}

@Composable
fun InteractiveBlockBoard(
    board: List<List<CellContent?>>,
    candidates: List<CandidatePiece>,
    hoverPreview: HoverPreview?,
    particles: List<BlastParticle>,
    popups: List<FloatingPopup>,
    onCellTapped: (Int, Int) -> Unit,
    onBoardBoundsUpdated: (Offset, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "boardPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(ArcadeBoardBg)
            .border(2.dp, ArcadeCellBorder, RoundedCornerShape(20.dp))
            .padding(8.dp)
            .onGloballyPositioned { coords ->
                onBoardBoundsUpdated(coords.positionInRoot(), coords.size.width.toFloat())
            }
            .testTag("game_board_container")
    ) {
        // Accessible tap grid for direct cell selection & power-ups
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (r in 0 until BOARD_SIZE) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (c in 0 until BOARD_SIZE) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable { onCellTapped(r, c) }
                                .semantics {
                                    contentDescription = "Katak ${r + 1}-${c + 1}"
                                }
                                .testTag("board_cell_${r}_${c}")
                        )
                    }
                }
            }
        }

        // High-precision Canvas for empty slots, placed jewel blocks, ghost preview, and particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellSize = size.width / BOARD_SIZE
            val hoverPiece = hoverPreview?.let { candidates.getOrNull(it.pieceIndex) }

            for (r in 0 until BOARD_SIZE) {
                for (c in 0 until BOARD_SIZE) {
                    val topLeft = Offset(c * cellSize, r * cellSize)
                    val pad = cellSize * 0.06f
                    val slotW = cellSize - pad * 2

                    val isRowClearing = hoverPreview?.rowsToClear?.contains(r) == true
                    val isColClearing = hoverPreview?.colsToClear?.contains(c) == true
                    val willClear = isRowClearing || isColClearing

                    // Empty cell slot background
                    drawRoundRect(
                        color = if (willClear) NeonGold.copy(alpha = 0.25f * pulseAlpha) else ArcadeCellEmpty,
                        topLeft = Offset(topLeft.x + pad, topLeft.y + pad),
                        size = Size(slotW, slotW),
                        cornerRadius = CornerRadius(slotW * 0.2f, slotW * 0.2f)
                    )

                    val cell = board[r][c]
                    if (cell != null) {
                        drawJewelBlockCell(
                            topLeft = topLeft,
                            cellSize = cellSize,
                            color = cell.color,
                            hasGem = cell.hasGem,
                            iceLayers = cell.iceLayers,
                            isBomb = cell.isBomb,
                            alpha = 1f,
                            highlightClear = willClear
                        )
                    } else if (hoverPreview != null && hoverPreview.isValid && hoverPiece != null) {
                        val pos = GridPos(r, c)
                        if (pos in hoverPreview.cellsToFill) {
                            val relOffset = GridPos(r - hoverPreview.anchorRow, c - hoverPreview.anchorCol)
                            drawJewelBlockCell(
                                topLeft = topLeft,
                                cellSize = cellSize,
                                color = hoverPiece.color,
                                hasGem = (hoverPiece.gemCell == relOffset),
                                isBomb = (hoverPiece.bombCell == relOffset),
                                alpha = 0.58f,
                                highlightClear = willClear
                            )
                        }
                    }
                }
            }

            // Draw blast particles
            for (p in particles) {
                val px = (p.col + p.vx * 0.5f) * cellSize
                val py = (p.row + p.vy * 0.5f) * cellSize
                drawCircle(
                    color = p.color,
                    radius = p.sizeDp.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Floating Combo & Score Popups
        for (popup in popups) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        color = Color(0xDD140D2E),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(1.5.dp, popup.color, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = popup.text,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = popup.color
                        ),
                        textAlign = TextAlign.Center
                    )
                    if (popup.subtitle.isNotEmpty()) {
                        Text(
                            text = popup.subtitle,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CandidatePieceTray(
    candidates: List<CandidatePiece>,
    selectedPieceIndex: Int?,
    onSelectPiece: (Int) -> Unit,
    onDragStartPiece: (Int, Offset) -> Unit,
    onDragMovePiece: (Int, Offset) -> Unit,
    onDragEndPiece: (Int) -> Unit,
    onDragCancelPiece: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        candidates.forEachIndexed { index, piece ->
            val isSelected = (selectedPieceIndex == index) && !piece.isPlaced
            var slotOriginInRoot = Offset.Zero

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1.05f)
                    .onGloballyPositioned { coords ->
                        slotOriginInRoot = coords.positionInRoot()
                    }
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(enabled = !piece.isPlaced) {
                        onSelectPiece(index)
                    }
                    .pointerInput(piece.uid, piece.isPlaced) {
                        if (!piece.isPlaced) {
                            detectDragGestures(
                                onDragStart = { localOffset ->
                                    onDragStartPiece(index, slotOriginInRoot + localOffset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    onDragMovePiece(index, slotOriginInRoot + change.position)
                                },
                                onDragEnd = {
                                    onDragEndPiece(index)
                                },
                                onDragCancel = {
                                    onDragCancelPiece()
                                }
                            )
                        }
                    }
                    .semantics {
                        contentDescription = "Blok shakl ${index + 1}"
                    }
                    .testTag("candidate_piece_$index"),
                color = if (isSelected) Color(0xFF31226B) else Color(0xFF1D1442),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) ElectricCyan else ArcadeCellBorder
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!piece.isPlaced) {
                        MiniPieceCanvas(
                            piece = piece,
                            maxCellSizeDp = 22.dp
                        )
                    } else {
                        Text(
                            text = "✓",
                            color = Color.White.copy(alpha = 0.22f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniPieceCanvas(
    piece: CandidatePiece,
    maxCellSizeDp: Dp,
    modifier: Modifier = Modifier
) {
    val maxSpan = maxOf(piece.shape.rowSpan, piece.shape.colSpan).coerceAtLeast(1)
    val effectiveCellDp = (maxCellSizeDp * (4f / maxOf(3.5f, maxSpan.toFloat())))
    val widthDp = effectiveCellDp * piece.shape.colSpan
    val heightDp = effectiveCellDp * piece.shape.rowSpan

    Canvas(
        modifier = modifier.size(width = widthDp, height = heightDp)
    ) {
        val cellPx = size.width / piece.shape.colSpan.coerceAtLeast(1)
        for (offset in piece.shape.cells) {
            val topLeft = Offset(offset.col * cellPx, offset.row * cellPx)
            drawJewelBlockCell(
                topLeft = topLeft,
                cellSize = cellPx,
                color = piece.color,
                hasGem = (piece.gemCell == offset),
                isBomb = (piece.bombCell == offset),
                alpha = 1f
            )
        }
    }
}
