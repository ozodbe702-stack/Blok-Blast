package com.example.model

data class LevelConfig(
    val levelNumber: Int,
    val titleUz: String,
    val subtitleUz: String,
    val difficultyLabel: String,
    val targetScore: Int,
    val targetGems: Int = 0,
    val targetIceBroken: Int = 0,
    val targetLines: Int = 0,
    val star2Threshold: Int,
    val star3Threshold: Int,
    val rewardCoins: Int,
    val accentColor: BlockColor,
    val boardBuilder: () -> List< List<CellContent?> >
)

object LevelCatalog {

    private fun buildGrid(setup: (Array<Array<CellContent?>>) -> Unit): List<List<CellContent?>> {
        val grid = Array(BOARD_SIZE) { arrayOfNulls<CellContent>(BOARD_SIZE) }
        setup(grid)
        return grid.map { it.toList() }
    }

    val levels: List<LevelConfig> = listOf(
        LevelConfig(
            levelNumber = 1,
            titleUz = "Ilk Qadamlar",
            subtitleUz = "Qator va ustunlarni to'ldirib bloklarni portlating",
            difficultyLabel = "Oson",
            targetScore = 400,
            targetLines = 3,
            star2Threshold = 650,
            star3Threshold = 950,
            rewardCoins = 40,
            accentColor = BlockColor.EMERALD,
            boardBuilder = {
                buildGrid { g ->
                    // Friendly nearly-complete center cross to give immediate satisfying blast
                    for (c in listOf(0, 1, 2, 5, 6, 7)) {
                        g[3][c] = CellContent(BlockColor.EMERALD, hasGem = (c == 1 || c == 6))
                        g[4][c] = CellContent(BlockColor.CYAN)
                    }
                }
            }
        ),
        LevelConfig(
            levelNumber = 2,
            titleUz = "Javohir Ovchisi",
            subtitleUz = "Porloq olmosli bloklarni qator to'ldirib yig'ing",
            difficultyLabel = "Oson",
            targetScore = 600,
            targetGems = 5,
            star2Threshold = 900,
            star3Threshold = 1300,
            rewardCoins = 50,
            accentColor = BlockColor.RUBY,
            boardBuilder = {
                buildGrid { g ->
                    val positions = listOf(1 to 1, 1 to 6, 3 to 3, 4 to 4, 6 to 1, 6 to 6)
                    positions.forEachIndexed { idx, (r, c) ->
                        g[r][c] = CellContent(
                            color = BlockColor.playableColors[idx % BlockColor.playableColors.size],
                            hasGem = true
                        )
                    }
                    g[1][2] = CellContent(BlockColor.RUBY)
                    g[1][5] = CellContent(BlockColor.RUBY)
                    g[6][2] = CellContent(BlockColor.AMBER)
                    g[6][5] = CellContent(BlockColor.AMBER)
                }
            }
        ),
        LevelConfig(
            levelNumber = 3,
            titleUz = "Muzlik Davri",
            subtitleUz = "Muz qoplangan bloklarni eritib doskani tozalang",
            difficultyLabel = "Oson",
            targetScore = 750,
            targetIceBroken = 6,
            star2Threshold = 1100,
            star3Threshold = 1500,
            rewardCoins = 60,
            accentColor = BlockColor.CYAN,
            boardBuilder = {
                buildGrid { g ->
                    for (c in 1..6) {
                        g[2][c] = CellContent(BlockColor.SAPPHIRE, iceLayers = 1)
                        g[5][c] = CellContent(BlockColor.CYAN, iceLayers = 1)
                    }
                }
            }
        ),
        LevelConfig(
            levelNumber = 4,
            titleUz = "Oltin Piramida",
            subtitleUz = "Piramida shaklidagi oltin bloklar va javohirlarni yig'ing",
            difficultyLabel = "O'rtacha",
            targetScore = 950,
            targetGems = 6,
            targetLines = 5,
            star2Threshold = 1350,
            star3Threshold = 1850,
            rewardCoins = 70,
            accentColor = BlockColor.AMBER,
            boardBuilder = {
                buildGrid { g ->
                    for (c in 1..6) g[7][c] = CellContent(BlockColor.AMBER, hasGem = (c == 2 || c == 5))
                    for (c in 2..5) g[6][c] = CellContent(BlockColor.TANGERINE, hasGem = (c == 3 || c == 4))
                    for (c in 3..4) g[5][c] = CellContent(BlockColor.RUBY, hasGem = true)
                }
            }
        ),
        LevelConfig(
            levelNumber = 5,
            titleUz = "Bomba Maydoni",
            subtitleUz = "Bomba bloklarni portlatib zanjirli reaksiyalar hosil qiling",
            difficultyLabel = "O'rtacha",
            targetScore = 1200,
            targetLines = 6,
            star2Threshold = 1650,
            star3Threshold = 2200,
            rewardCoins = 85,
            accentColor = BlockColor.TANGERINE,
            boardBuilder = {
                buildGrid { g ->
                    g[2][2] = CellContent(BlockColor.TANGERINE, isBomb = true)
                    g[2][5] = CellContent(BlockColor.TANGERINE, isBomb = true)
                    g[5][2] = CellContent(BlockColor.RUBY, isBomb = true)
                    g[5][5] = CellContent(BlockColor.RUBY, isBomb = true)
                    for (r in 1..6) {
                        if (r != 3 && r != 4) {
                            g[r][1] = CellContent(BlockColor.AMBER)
                            g[r][6] = CellContent(BlockColor.AMBER)
                        }
                    }
                    g[3][3] = CellContent(BlockColor.STONE)
                    g[4][4] = CellContent(BlockColor.STONE)
                }
            }
        ),
        LevelConfig(
            levelNumber = 6,
            titleUz = "Shaxmat Doskasi",
            subtitleUz = "Shaxmat tartibidagi muz va olmos bloklarini zabt eting",
            difficultyLabel = "O'rtacha",
            targetScore = 1350,
            targetGems = 7,
            targetIceBroken = 6,
            star2Threshold = 1800,
            star3Threshold = 2400,
            rewardCoins = 95,
            accentColor = BlockColor.AMETHYST,
            boardBuilder = {
                buildGrid { g ->
                    for (r in 2..5) {
                        for (c in 2..5) {
                            if ((r + c) % 2 == 0) {
                                g[r][c] = CellContent(
                                    color = BlockColor.AMETHYST,
                                    hasGem = (r == 2 || r == 5),
                                    iceLayers = 1
                                )
                            }
                        }
                    }
                }
            }
        ),
        LevelConfig(
            levelNumber = 7,
            titleUz = "Zumrad Labirint",
            subtitleUz = "Tosh devorlar orasidan yo'l ochib olmoslarni to'plang",
            difficultyLabel = "O'rtacha",
            targetScore = 1500,
            targetGems = 8,
            star2Threshold = 2000,
            star3Threshold = 2700,
            rewardCoins = 105,
            accentColor = BlockColor.EMERALD,
            boardBuilder = {
                buildGrid { g ->
                    for (i in 1..6) {
                        if (i != 3 && i != 4) {
                            g[1][i] = CellContent(BlockColor.EMERALD, hasGem = (i == 1 || i == 6))
                            g[6][i] = CellContent(BlockColor.EMERALD, hasGem = (i == 1 || i == 6))
                            g[i][1] = CellContent(BlockColor.STONE)
                            g[i][6] = CellContent(BlockColor.STONE)
                        }
                    }
                    g[3][3] = CellContent(BlockColor.CYAN, hasGem = true)
                    g[3][4] = CellContent(BlockColor.CYAN, hasGem = true)
                    g[4][3] = CellContent(BlockColor.CYAN, hasGem = true)
                    g[4][4] = CellContent(BlockColor.CYAN, hasGem = true)
                }
            }
        ),
        LevelConfig(
            levelNumber = 8,
            titleUz = "Ikki Qatlamli Muz",
            subtitleUz = "Qalin 2 qavatli muz bloklarini ikki marta eriting",
            difficultyLabel = "Qiyin",
            targetScore = 1700,
            targetIceBroken = 10,
            star2Threshold = 2300,
            star3Threshold = 3000,
            rewardCoins = 120,
            accentColor = BlockColor.SAPPHIRE,
            boardBuilder = {
                buildGrid { g ->
                    val coords = listOf(
                        1 to 3, 1 to 4,
                        3 to 1, 3 to 6,
                        4 to 1, 4 to 6,
                        6 to 3, 6 to 4
                    )
                    coords.forEach { (r, c) ->
                        g[r][c] = CellContent(BlockColor.SAPPHIRE, iceLayers = 2)
                    }
                    g[3][3] = CellContent(BlockColor.CYAN, isBomb = true)
                    g[4][4] = CellContent(BlockColor.CYAN, isBomb = true)
                }
            }
        ),
        LevelConfig(
            levelNumber = 9,
            titleUz = "Kamalak Halqasi",
            subtitleUz = "8 xil rangdagi javohir halqasini portlating",
            difficultyLabel = "Qiyin",
            targetScore = 1900,
            targetGems = 9,
            targetLines = 8,
            star2Threshold = 2600,
            star3Threshold = 3400,
            rewardCoins = 130,
            accentColor = BlockColor.CORAL,
            boardBuilder = {
                buildGrid { g ->
                    val ring = listOf(
                        1 to 2, 1 to 3, 1 to 4, 1 to 5,
                        2 to 6, 3 to 6, 4 to 6, 5 to 6,
                        6 to 5, 6 to 4, 6 to 3, 6 to 2,
                        5 to 1, 4 to 1, 3 to 1, 2 to 1
                    )
                    ring.forEachIndexed { idx, (r, c) ->
                        val color = BlockColor.playableColors[idx % BlockColor.playableColors.size]
                        g[r][c] = CellContent(color = color, hasGem = (idx % 2 == 0))
                    }
                }
            }
        ),
        LevelConfig(
            levelNumber = 10,
            titleUz = "Qadimiy Qal'a",
            subtitleUz = "Qal'a minoralarini bomba va kombolar bilan yiqiting",
            difficultyLabel = "Qiyin",
            targetScore = 2200,
            targetIceBroken = 8,
            targetLines = 9,
            star2Threshold = 2900,
            star3Threshold = 3800,
            rewardCoins = 150,
            accentColor = BlockColor.RUBY,
            boardBuilder = {
                buildGrid { g ->
                    val corners = listOf(0 to 0, 0 to 7, 7 to 0, 7 to 7)
                    corners.forEach { (r, c) ->
                        g[r][c] = CellContent(BlockColor.STONE, iceLayers = 1)
                    }
                    for (r in 2..5) {
                        g[r][2] = CellContent(BlockColor.RUBY, iceLayers = 1)
                        g[r][5] = CellContent(BlockColor.RUBY, iceLayers = 1)
                    }
                    g[3][3] = CellContent(BlockColor.AMBER, isBomb = true, hasGem = true)
                    g[4][4] = CellContent(BlockColor.AMBER, isBomb = true, hasGem = true)
                }
            }
        ),
        LevelConfig(
            levelNumber = 11,
            titleUz = "Kristal Yulduz",
            subtitleUz = "Diagonal olmoslar va muzlatilgan yulduzni oching",
            difficultyLabel = "Qiyin",
            targetScore = 2500,
            targetGems = 10,
            targetIceBroken = 8,
            star2Threshold = 3300,
            star3Threshold = 4200,
            rewardCoins = 165,
            accentColor = BlockColor.CYAN,
            boardBuilder = {
                buildGrid { g ->
                    for (i in 1..6) {
                        g[i][i] = CellContent(BlockColor.CYAN, hasGem = true, iceLayers = 1)
                        g[i][7 - i] = CellContent(BlockColor.AMETHYST, hasGem = (i % 2 == 0), iceLayers = 1)
                    }
                }
            }
        ),
        LevelConfig(
            levelNumber = 12,
            titleUz = "Vulqon Portlashi",
            subtitleUz = "Olovli bloklar va bombalar yordamida toshlarni eriting",
            difficultyLabel = "Ekspert",
            targetScore = 2800,
            targetLines = 11,
            star2Threshold = 3700,
            star3Threshold = 4700,
            rewardCoins = 180,
            accentColor = BlockColor.TANGERINE,
            boardBuilder = {
                buildGrid { g ->
                    for (r in 2..5) {
                        for (c in 2..5) {
                            val isEdge = (r == 2 || r == 5 || c == 2 || c == 5)
                            if (isEdge) {
                                g[r][c] = CellContent(
                                    color = if ((r + c) % 2 == 0) BlockColor.TANGERINE else BlockColor.STONE,
                                    isBomb = (r == c),
                                    hasGem = (r + c == 7)
                                )
                            }
                        }
                    }
                }
            }
        ),
        LevelConfig(
            levelNumber = 13,
            titleUz = "Shimoliy Yog'du",
            subtitleUz = "Qutb muzliklari orasidan 12 ta javohirni qutqaring",
            difficultyLabel = "Ekspert",
            targetScore = 3100,
            targetGems = 12,
            targetIceBroken = 12,
            star2Threshold = 4100,
            star3Threshold = 5200,
            rewardCoins = 200,
            accentColor = BlockColor.EMERALD,
            boardBuilder = {
                buildGrid { g ->
                    for (c in 0..7) {
                        if (c != 3 && c != 4) {
                            g[1][c] = CellContent(BlockColor.EMERALD, hasGem = true, iceLayers = 1)
                            g[6][c] = CellContent(BlockColor.SAPPHIRE, hasGem = true, iceLayers = 1)
                        }
                    }
                    g[3][1] = CellContent(BlockColor.AMETHYST, iceLayers = 2)
                    g[4][1] = CellContent(BlockColor.AMETHYST, iceLayers = 2)
                    g[3][6] = CellContent(BlockColor.AMETHYST, iceLayers = 2)
                    g[4][6] = CellContent(BlockColor.AMETHYST, iceLayers = 2)
                }
            }
        ),
        LevelConfig(
            levelNumber = 14,
            titleUz = "Qirollik Xazinasi",
            subtitleUz = "Oltin sandiqdagi barcha olmos va muzlarni tozalang",
            difficultyLabel = "Ekspert",
            targetScore = 3500,
            targetGems = 14,
            targetLines = 12,
            star2Threshold = 4600,
            star3Threshold = 5800,
            rewardCoins = 230,
            accentColor = BlockColor.AMBER,
            boardBuilder = {
                buildGrid { g ->
                    for (r in 1..6) {
                        for (c in listOf(1, 3, 4, 6)) {
                            if ((r + c) % 2 == 0) {
                                g[r][c] = CellContent(
                                    color = BlockColor.AMBER,
                                    hasGem = true,
                                    iceLayers = if (r == 1 || r == 6) 2 else 1
                                )
                            }
                        }
                    }
                    g[0][3] = CellContent(BlockColor.RUBY, isBomb = true)
                    g[7][4] = CellContent(BlockColor.RUBY, isBomb = true)
                }
            }
        ),
        LevelConfig(
            levelNumber = 15,
            titleUz = "Afsonaviy Blast Ustasi",
            subtitleUz = "Yakuniy sinov: muz, tosh, olmos va bombalar uyg'unligi!",
            difficultyLabel = "Afsonaviy",
            targetScore = 4000,
            targetGems = 15,
            targetIceBroken = 14,
            targetLines = 14,
            star2Threshold = 5200,
            star3Threshold = 6800,
            rewardCoins = 300,
            accentColor = BlockColor.CORAL,
            boardBuilder = {
                buildGrid { g ->
                    for (r in 0..7) {
                        for (c in 0..7) {
                            val distFromCenter = kotlin.math.abs(r - 3.5) + kotlin.math.abs(c - 3.5)
                            if (distFromCenter in 2.5..3.5) {
                                val color = BlockColor.playableColors[(r * 3 + c) % BlockColor.playableColors.size]
                                g[r][c] = CellContent(
                                    color = color,
                                    hasGem = ((r + c) % 2 == 0),
                                    iceLayers = if ((r + c) % 3 == 0) 2 else 1,
                                    isBomb = (r == 1 && c == 3) || (r == 6 && c == 4)
                                )
                            }
                        }
                    }
                }
            }
        )
    )

    fun getLevel(levelNumber: Int): LevelConfig {
        return levels.find { it.levelNumber == levelNumber } ?: levels.first()
    }
}
