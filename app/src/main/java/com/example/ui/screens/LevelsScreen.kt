package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.LevelProgressEntity
import com.example.model.LevelCatalog
import com.example.model.LevelConfig
import com.example.ui.components.drawJewelBlockCell
import com.example.ui.theme.ArcadeCellBorder
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.ArcadeSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.NeonGold
import com.example.ui.theme.VibrantMagenta

@Composable
fun LevelsScreen(
    levelProgressList: List<LevelProgressEntity>,
    onSelectLevel: (Int) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    val progressMap = levelProgressList.associateBy { it.levelNumber }
    val totalStars = levelProgressList.sumOf { it.starsEarned }
    val unlockedCount = levelProgressList.count { it.isUnlocked }.coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("levels_screen")
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier.testTag("levels_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Orqaga",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Sarguzasht Bosqichlari",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Ochilgan: $unlockedCount / ${LevelCatalog.levels.size} • Yulduzlar: $totalStars / ${LevelCatalog.levels.size * 3}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonGold
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(LevelCatalog.levels, key = { it.levelNumber }) { config ->
                val progress = progressMap[config.levelNumber]
                // Allow playing unlocked levels, plus allow preview/playing any level if unlocked or level 1
                val isUnlocked = progress?.isUnlocked == true || config.levelNumber == 1
                val stars = progress?.starsEarned ?: 0
                val bestScore = progress?.bestScore ?: 0

                LevelItemCard(
                    config = config,
                    isUnlocked = isUnlocked,
                    stars = stars,
                    bestScore = bestScore,
                    onClick = { onSelectLevel(config.levelNumber) }
                )
            }
        }
    }
}

@Composable
private fun LevelItemCard(
    config: LevelConfig,
    isUnlocked: Boolean,
    stars: Int,
    bestScore: Int,
    onClick: () -> Unit
) {
    val borderColor = when {
        stars == 3 -> NeonGold
        isUnlocked -> config.accentColor.baseColor.copy(alpha = 0.85f)
        else -> ArcadeCellBorder
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .border(1.5.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("level_card_${config.levelNumber}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) ArcadeSurface else ArcadeSurface.copy(alpha = 0.65f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Jewel Badge with Level Number
            Box(
                modifier = Modifier.size(58.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawJewelBlockCell(
                        topLeft = Offset.Zero,
                        cellSize = size.width,
                        color = config.accentColor,
                        alpha = if (isUnlocked) 1f else 0.45f
                    )
                }
                Text(
                    text = "${config.levelNumber}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = Color.White
                )
            }

            // Level Details & Objectives
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = config.titleUz,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Surface(
                        color = ArcadeSurfaceVariant,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = config.difficultyLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = config.accentColor.lightColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = config.subtitleUz,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f)
                )

                // Mission Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    ObjectivePill("Maqsad: ${config.targetScore}", NeonGold)
                    if (config.targetGems > 0) {
                        ObjectivePill("${config.targetGems} Olmos", VibrantMagenta)
                    }
                    if (config.targetIceBroken > 0) {
                        ObjectivePill("${config.targetIceBroken} Muz", ElectricCyan)
                    }
                    if (config.targetLines > 0) {
                        ObjectivePill("${config.targetLines} Qator", EmeraldMint)
                    }
                }

                if (bestScore > 0) {
                    Text(
                        text = "Eng yuqori natija: $bestScore ball",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldMint
                    )
                }
            }

            // Stars & Play Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row {
                    for (s in 1..3) {
                        Icon(
                            imageVector = if (s <= stars) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (s <= stars) NeonGold else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isUnlocked) NeonGold else ArcadeSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.PlayArrow else Icons.Default.Lock,
                        contentDescription = "Boshlash",
                        tint = if (isUnlocked) Color(0xFF1A103C) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ObjectivePill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
