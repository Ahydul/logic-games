package com.example.tfg.games.hakyuu

import com.example.tfg.common.utils.Coordinate
import com.example.tfg.games.GameType
import com.example.tfg.games.Games
import com.example.tfg.games.Regions
import kotlin.random.Random

class Hakyuu private constructor(
    override val type: Games = Games.HAKYUU,
    override val rules: List<String> = listOf(),
    override val url: String = "",
    override val noNotes: Boolean = true,
    override val boardRegions: Map<Int, List<Coordinate>>,
) : GameType {


    companion object {
        fun example(): Hakyuu {
            val numColumns = 6
            val numRows = 6
            val sections = intArrayOf(
                0, 1, 2, 3, 4, 5,
                0, 6, 2, 4, 4, 7,
                6, 6, 2, 8, 8, 7,
                9, 6,10, 8, 8, 7,
                9,11,10,12,13,13,
                14,10,10,12,12,12
            )
            val regions = mutableMapOf<Int, MutableList<Coordinate>>()

            sections.forEachIndexed { index, value ->
                if (regions.contains(value)) {
                    regions[value]!!.add(
                        Coordinate.fromIndex(
                            index,
                            numRows = numRows,
                            numColumns = numColumns
                        )
                    )
                } else {
                    regions[value] = mutableListOf(
                        Coordinate.fromIndex(
                            index,
                            numRows = numRows,
                            numColumns = numColumns
                        )
                    )
                }
            }

            return Hakyuu(
                boardRegions = regions
            )
        }

        fun create(numColumns: Int, numRows: Int, minNumberOfRegions: Int, random: Random): Hakyuu {
            val regions = Regions(numColumns = numColumns, numRows = numRows, random).divideRegionsOptionB(minNumberOfRegions)

            return Hakyuu(
                boardRegions = regions
            )
        }
    }
}