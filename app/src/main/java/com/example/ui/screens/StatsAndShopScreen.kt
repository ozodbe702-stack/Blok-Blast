package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.GameHistoryEntity
import com.example.data.local.PlayerStatsEntity
import com.example.model.PowerUpType
import com.example.ui.theme.ArcadeCellBorder
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.ArcadeSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.NeonGold
import com.example.ui.theme.VibrantMagenta

@Composable
fun StatsAndShopScreen(
    stats: PlayerStatsEntity,
    history: List<GameHistoryEntity>,
    onBuyPowerUp: (PowerUpType) -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptic: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("stats_shop_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header with Coin Balance
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Do'kon va Rekordlar",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Kuchaytirgichlar xaridi va shaxsiy natijalar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Surface(
                    color = ArcadeSurfaceVariant,
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${stats.coins} tanga",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonGold
                        )
                    }
                }
            }
        }

        // 2. Booster Shop Section
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
                        text = "Kuchaytirgichlar Do'koni",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonGold
                    )

                    PowerUpType.entries.forEach { powerUp ->
                        val owned = when (powerUp) {
                            PowerUpType.HAMMER -> stats.hammerCount
                            PowerUpType.BOMB -> stats.bombCount
                            PowerUpType.REROLL -> stats.rerollCount
                            PowerUpType.ROTATE -> stats.rotateCount
                        }
                        val icon = when (powerUp) {
                            PowerUpType.HAMMER -> Icons.Default.Build
                            PowerUpType.BOMB -> Icons.Default.LocalFireDepartment
                            PowerUpType.REROLL -> Icons.Default.Autorenew
                            PowerUpType.ROTATE -> Icons.Default.RotateRight
                        }

                        Surface(
                            color = ArcadeSurfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = powerUp.titleUz,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "${powerUp.titleUz} (Zaxirada: $owned ta)",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = powerUp.descriptionUz,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onBuyPowerUp(powerUp) },
                                    enabled = stats.coins >= powerUp.coinCost,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonGold,
                                        contentColor = Color(0xFF1A103C)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("buy_powerup_${powerUp.name}")
                                ) {
                                    Text(
                                        text = "${powerUp.coinCost} T",
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Lifetime Statistics Grid
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = EmeraldMint)
                        Text(
                            text = "Umumiy Statistika",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatMetricBox(
                            title = "Klassik Rekord",
                            value = "${stats.classicBestScore}",
                            color = NeonGold,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricBox(
                            title = "Kunlik Rekord",
                            value = "${stats.dailyBestScore}",
                            color = VibrantMagenta,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatMetricBox(
                            title = "Qatorlar Tozalandi",
                            value = "${stats.totalLinesCleared}",
                            color = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricBox(
                            title = "Olmoslar Yig'ildi",
                            value = "${stats.totalGemsCollected}",
                            color = EmeraldMint,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricBox(
                            title = "Maks Kombo",
                            value = "x${stats.maxComboReached}",
                            color = NeonGold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Sound & Vibration Settings
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ovoz va Tebranish Sozlamalari",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = ElectricCyan)
                            Text("Ovoz effektlari", color = Color.White)
                        }
                        Switch(
                            checked = stats.soundEnabled,
                            onCheckedChange = { onToggleSound() },
                            modifier = Modifier.testTag("toggle_sound_switch")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = NeonGold)
                            Text("Tebranish (Haptic)", color = Color.White)
                        }
                        Switch(
                            checked = stats.hapticEnabled,
                            onCheckedChange = { onToggleHaptic() },
                            modifier = Modifier.testTag("toggle_haptic_switch")
                        )
                    }
                }
            }
        }

        // 5. Recent Game History
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = null, tint = NeonGold)
                Text(
                    text = "So'nggi O'yinlar Tarixi",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }

        if (history.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp),
                    color = ArcadeSurface,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Hali o'yinlar tarixi yo'q. Bosqichlarni o'ynab rekordlar o'rnating!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(history, key = { it.id }) { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .border(1.dp, ArcadeCellBorder, RoundedCornerShape(16.dp)),
                    color = ArcadeSurface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.modeTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "${item.linesCleared} qator • Kombo x${item.maxCombo}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (item.stars > 0) {
                                Row {
                                    repeat(item.stars) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = NeonGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${item.score} ball",
                                style = MaterialTheme.typography.titleMedium,
                                color = EmeraldMint
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ArcadeSurfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
