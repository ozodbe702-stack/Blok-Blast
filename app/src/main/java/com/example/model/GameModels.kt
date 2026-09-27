package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmberBase
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmethystBase
import com.example.ui.theme.AmethystDark
import com.example.ui.theme.AmethystLight
import com.example.ui.theme.CoralBase
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralLight
import com.example.ui.theme.CyanBase
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanLight
import com.example.ui.theme.EmeraldBase
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.RubyBase
import com.example.ui.theme.RubyDark
import com.example.ui.theme.RubyLight
import com.example.ui.theme.SapphireBase
import com.example.ui.theme.SapphireDark
import com.example.ui.theme.SapphireLight
import com.example.ui.theme.StoneBase
import com.example.ui.theme.StoneDark
import com.example.ui.theme.StoneLight
import com.example.ui.theme.TangerineBase
import com.example.ui.theme.TangerineDark
import com.example.ui.theme.TangerineLight

const val BOARD_SIZE = 8

enum class BlockColor(
    val titleUz: String,
    val baseColor: Color,
    val lightColor: Color,
    val darkColor: Color
) {
    RUBY("Qizil Yoqut", RubyBase, RubyLight, RubyDark),
    EMERALD("Yashil Zumrad", EmeraldBase, EmeraldLight, EmeraldDark),
    SAPPHIRE("Moviy Sapfir", SapphireBase, SapphireLight, SapphireDark),
    AMBER("Oltin Kahrabo", AmberBase, AmberLight, AmberDark),
    AMETHYST("Binafsha Ametist", AmethystBase, AmethystLight, AmethystDark),
    CORAL("Pushti Marjon", CoralBase, CoralLight, CoralDark),
    CYAN("Feruza Kristal", CyanBase, CyanLight, CyanDark),
    TANGERINE("Olovli Apelsin", TangerineBase, TangerineLight, TangerineDark),
    STONE("Tosh G'isht", StoneBase, StoneLight, StoneDark);

    companion object {
        val playableColors = listOf(
            RUBY, EMERALD, SAPPHIRE, AMBER, AMETHYST, CORAL, CYAN, TANGERINE
        )
    }
}

data class GridPos(val row: Int, val col: Int)

data class CellContent(
    val color: BlockColor,
    val hasGem: Boolean = false,
    val iceLayers: Int = 0, // 0 = normal, 1 = cracked ice, 2 = thick ice
    val isBomb: Boolean = false
)

enum class ShapeDifficulty { EASY, MEDIUM, HARD }

data class BlockShape(
    val id: String,
    val cells: List<GridPos>,
    val difficulty: ShapeDifficulty = ShapeDifficulty.EASY
) {
    val rowSpan: Int = (cells.maxOfOrNull { it.row } ?: 0) + 1
    val colSpan: Int = (cells.maxOfOrNull { it.col } ?: 0) + 1

    fun rotate90(): BlockShape {
        val rotatedRaw = cells.map { GridPos(row = it.col, col = -it.row) }
        val minRow = rotatedRaw.minOfOrNull { it.row } ?: 0
        val minCol = rotatedRaw.minOfOrNull { it.col } ?: 0
        val normalized = rotatedRaw.map { GridPos(it.row - minRow, it.col - minCol) }
            .sortedWith(compareBy({ it.row }, { it.col }))
        return copy(id = "${id}_rot", cells = normalized)
    }
}

object ShapeCatalog {
    private fun shape(id: String, diff: ShapeDifficulty, vararg coords: Pair<Int, Int>): BlockShape {
        return BlockShape(
            id = id,
            cells = coords.map { GridPos(it.first, it.second) },
            difficulty = diff
        )
    }

    val allShapes: List<BlockShape> = listOf(
        // EASY SHAPES (1 to 3 cells, 2x2)
        shape("dot_1", ShapeDifficulty.EASY, 0 to 0),
        shape("h_2", ShapeDifficulty.EASY, 0 to 0, 0 to 1),
        shape("v_2", ShapeDifficulty.EASY, 0 to 0, 1 to 0),
        shape("h_3", ShapeDifficulty.EASY, 0 to 0, 0 to 1, 0 to 2),
        shape("v_3", ShapeDifficulty.EASY, 0 to 0, 1 to 0, 2 to 0),
        shape("sq_2x2", ShapeDifficulty.EASY, 0 to 0, 0 to 1, 1 to 0, 1 to 1),
        shape("corner_tl", ShapeDifficulty.EASY, 0 to 0, 0 to 1, 1 to 0),
        shape("corner_tr", ShapeDifficulty.EASY, 0 to 0, 0 to 1, 1 to 1),
        shape("corner_bl", ShapeDifficulty.EASY, 0 to 0, 1 to 0, 1 to 1),
        shape("corner_br", ShapeDifficulty.EASY, 0 to 1, 1 to 0, 1 to 1),

        // MEDIUM SHAPES (4 cells, L, T, Z, S, 2x3)
        shape("h_4", ShapeDifficulty.MEDIUM, 0 to 0, 0 to 1, 0 to 2, 0 to 3),
        shape("v_4", ShapeDifficulty.MEDIUM, 0 to 0, 1 to 0, 2 to 0, 3 to 0),
        shape("l_1", ShapeDifficulty.MEDIUM, 0 to 0, 1 to 0, 2 to 0, 2 to 1),
        shape("l_2", ShapeDifficulty.MEDIUM, 0 to 1, 1 to 1, 2 to 0, 2 to 1),
        shape("l_3", ShapeDifficulty.MEDIUM, 0 to 0, 0 to 1, 0 to 2, 1 to 0),
        shape("l_4", ShapeDifficulty.MEDIUM, 0 to 0, 0 to 1, 0 to 2, 1 to 2),
        shape("t_up", ShapeDifficulty.MEDIUM, 0 to 1, 1 to 0, 1 to 1, 1 to 2),
        shape("t_down", ShapeDifficulty.MEDIUM, 0 to 0, 0 to 1, 0 to 2, 1 to 1),
        shape("t_left", ShapeDifficulty.MEDIUM, 0 to 1, 1 to 0, 1 to 1, 2 to 1),
        shape("t_right", ShapeDifficulty.MEDIUM, 0 to 0, 1 to 0, 1 to 1, 2 to 0),
        shape("z_h", ShapeDifficulty.MEDIUM, 0 to 0, 0 to 1, 1 to 1, 1 to 2),
        shape("s_h", ShapeDifficulty.MEDIUM, 0 to 1, 0 to 2, 1 to 0, 1 to 1),
        shape("diag_2", ShapeDifficulty.MEDIUM, 0 to 0, 1 to 1),

        // HARD SHAPES (5 cells, 3x3, Big L, Plus, U, 2x3)
        shape("h_5", ShapeDifficulty.HARD, 0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4),
        shape("v_5", ShapeDifficulty.HARD, 0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0),
        shape("big_l_1", ShapeDifficulty.HARD, 0 to 0, 1 to 0, 2 to 0, 2 to 1, 2 to 2),
        shape("big_l_2", ShapeDifficulty.HARD, 0 to 0, 0 to 1, 0 to 2, 1 to 0, 2 to 0),
        shape("big_l_3", ShapeDifficulty.HARD, 0 to 2, 1 to 2, 2 to 0, 2 to 1, 2 to 2),
        shape("big_l_4", ShapeDifficulty.HARD, 0 to 0, 0 to 1, 0 to 2, 1 to 2, 2 to 2),
        shape("rect_2x3", ShapeDifficulty.HARD, 0 to 0, 0 to 1, 0 to 2, 1 to 0, 1 to 1, 1 to 2),
        shape("rect_3x2", ShapeDifficulty.HARD, 0 to 0, 0 to 1, 1 to 0, 1 to 1, 2 to 0, 2 to 1),
        shape("sq_3x3", ShapeDifficulty.HARD, 0 to 0, 0 to 1, 0 to 2, 1 to 0, 1 to 1, 1 to 2, 2 to 0, 2 to 1, 2 to 2),
        shape("plus_5", ShapeDifficulty.HARD, 0 to 1, 1 to 0, 1 to 1, 1 to 2, 2 to 1),
        shape("u_shape", ShapeDifficulty.HARD, 0 to 0, 0 to 2, 1 to 0, 1 to 1, 1 to 2)
    )

    val easyShapes = allShapes.filter { it.difficulty == ShapeDifficulty.EASY }
    val mediumShapes = allShapes.filter { it.difficulty == ShapeDifficulty.MEDIUM }
    val hardShapes = allShapes.filter { it.difficulty == ShapeDifficulty.HARD }
}

data class CandidatePiece(
    val uid: Long,
    val shape: BlockShape,
    val color: BlockColor,
    val gemCell: GridPos? = null,
    val bombCell: GridPos? = null,
    val isPlaced: Boolean = false
)

enum class PowerUpType(
    val titleUz: String,
    val descriptionUz: String,
    val coinCost: Int
) {
    HAMMER("Bolg'a", "Doskadagi 1 ta blok yoki muzni sindiradi", 40),
    BOMB("Bomba 3x3", "Tanlangan 3x3 maydonni portlatib tozalaydi", 75),
    REROLL("Yangilash", "Navbatdagi 3 ta shaklni yangisiga almashtiradi", 50),
    ROTATE("Burish 90°", "Navbatdagi barcha shakllarni 90 darajaga buradi", 35)
}

enum class GameMode(val titleUz: String) {
    ADVENTURE("Sarguzasht Bosqichi"),
    CLASSIC("Klassik Cheksiz"),
    DAILY("Kunlik Sinov")
}

enum class AppScreen {
    HOME,
    LEVELS,
    GAME,
    STATS_SHOP
}

data class BlastParticle(
    val id: Long,
    val row: Float,
    val col: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val sizeDp: Float
)

data class FloatingPopup(
    val id: Long,
    val text: String,
    val subtitle: String = "",
    val color: Color,
    val row: Float = 3.5f,
    val col: Float = 3.5f
)
