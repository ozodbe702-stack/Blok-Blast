package com.example

import com.example.model.BOARD_SIZE
import com.example.model.BlockColor
import com.example.model.LevelCatalog
import com.example.model.ShapeCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun levelCatalog_has15ValidLevels() {
    assertEquals(15, LevelCatalog.levels.size)
    LevelCatalog.levels.forEach { level ->
      val board = level.boardBuilder()
      assertEquals(BOARD_SIZE, board.size)
      board.forEach { row -> assertEquals(BOARD_SIZE, row.size) }
      assertTrue(level.targetScore > 0)
    }
  }

  @Test
  fun shapeRotation_preservesCellCountAndNormalizes() {
    ShapeCatalog.allShapes.forEach { shape ->
      val rotated = shape.rotate90()
      assertEquals(shape.cells.size, rotated.cells.size)
      assertEquals(0, rotated.cells.minOf { it.row })
      assertEquals(0, rotated.cells.minOf { it.col })
    }
  }

  @Test
  fun playableColors_hasEightJewelColors() {
    assertEquals(8, BlockColor.playableColors.size)
  }
}
