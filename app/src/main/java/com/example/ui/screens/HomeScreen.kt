package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.LevelProgressEntity
import com.example.data.local.PlayerStatsEntity
import com.example.model.BlockColor
import com.example.model.LevelCatalog
import com.example.ui.components.drawJewelBlockCell
import com.example.ui.theme.ArcadeCellBorder
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.ArcadeSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.NeonGold
import com.example.ui.theme.VibrantMagenta

@Composable
fun HomeScreen(
    stats: PlayerStatsEntity,
    levelProgress: List<LevelProgressEntity>,
    onStartAdventureLevel: (Int) -> Unit,
    onOpenLevels: () -> Unit,
    onStartClassic: () -> Unit,
    onStartDaily: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalStars = levelProgress.sumOf { it.starsEarned }
    val maxStars = LevelCatalog.levels.size * 3
    val highestUnlockedLevel = levelProgress.filter { it.isUnlocked }
        .maxByOrNull { it.levelNumber }?.levelNumber ?: 1
    val currentLevelConfig = LevelCatalog.getLevel(highestUnlockedLevel)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Stats Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = ArcadeSurfaceVariant,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Yulduzlar",
                                tint = NeonGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "$totalStars / $maxStars",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        color = ArcadeSurfaceVariant,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Tangalar",
                                tint = NeonGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${stats.coins} tanga",
                                style = MaterialTheme.typography.labelLarge,
                                color = NeonGold
                            )
                        }
                    }
                }

                Surface(
                    color = ArcadeSurface,
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeCellBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Rekord",
                            tint = EmeraldMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${stats.classicBestScore}",
                            style = MaterialTheme.typography.labelLarge,
                            color = EmeraldMint
                        )
                    }
                }
            }
        }

        // 2. Hero Banner with Generated Illustration
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .height(195.dp),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Blok Blast Boshqotirma O'yini",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x330F0A24),
                                        Color(0xCC0F0A24),
                                        Color(0xF50F0A24)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Surface(
                            color = VibrantMagenta,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "15 TA QIZIQARLI BOSQICH",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "BLOK BLAST",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = NeonGold
                        )
                        Text(
                            text = "Rang-barang javohir bloklar, olmoslar va kuchli kombo portlashlar!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                }
            }
        }

        // 3. Primary Adventure Mode Action Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .border(2.dp, NeonGold.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                    .clickable { onStartAdventureLevel(highestUnlockedLevel) }
                    .testTag("play_adventure_button"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ArcadeSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF2B1B63),
                                    Color(0xFF3E1E68)
                                )
                            )
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SARGUZASHT REJIMI",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonGold
                            )
                            Text(
                                text = "${currentLevelConfig.levelNumber}-Bosqich: ${currentLevelConfig.titleUz}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = currentLevelConfig.subtitleUz,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(NeonGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "O'ynash",
                                tint = Color(0xFF1A103C),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onStartAdventureLevel(highestUnlockedLevel) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("continue_level_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGold,
                                contentColor = Color(0xFF1A103C)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${highestUnlockedLevel}-Bosqichni O'ynash",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Button(
                            onClick = onOpenLevels,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("open_levels_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ArcadeSurfaceVariant,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Barcha 15 Bosqich")
                        }
                    }
                }
            }
        }

        // 4. Secondary Modes Row: Classic Endless & Daily Challenge
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Classic Endless Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.5.dp, ElectricCyan.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                        .clickable { onStartClassic() }
                        .testTag("play_classic_button"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Klassik",
                                tint = ElectricCyan
                            )
                        }
                        Text(
                            text = "Klassik Cheksiz",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Rekord: ${stats.classicBestScore} ball",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ElectricCyan
                        )
                        Text(
                            text = "Cheksiz kombolar va yuqori rekord o'rnating!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                }

                // Daily Challenge Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.5.dp, VibrantMagenta.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                        .clickable { onStartDaily() }
                        .testTag("play_daily_button"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(VibrantMagenta.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "Kunlik Sinov",
                                tint = VibrantMagenta
                            )
                        }
                        Text(
                            text = "Kunlik Sinov",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Mukofot: +140 tanga",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonGold
                        )
                        Text(
                            text = "Maxsus olmosli va bombali kunlik maydon!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                }
            }
        }

        // 5. Colorful Jewel Blocks & Special Elements Showcase
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ArcadeSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Rang-barang Javohir Bloklar va Maxsus Elementlar",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonGold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(BlockColor.playableColors) { blockColor ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Canvas(modifier = Modifier.size(44.dp)) {
                                    drawJewelBlockCell(
                                        topLeft = Offset.Zero,
                                        cellSize = size.width,
                                        color = blockColor,
                                        hasGem = (blockColor == BlockColor.RUBY || blockColor == BlockColor.AMBER),
                                        iceLayers = if (blockColor == BlockColor.CYAN) 1 else 0,
                                        isBomb = (blockColor == BlockColor.TANGERINE)
                                    )
                                }
                                Text(
                                    text = blockColor.titleUz.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
