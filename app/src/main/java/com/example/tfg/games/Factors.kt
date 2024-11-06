package com.example.tfg.games

import androidx.room.Entity
import com.example.tfg.common.IdGenerator
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.Score
import com.example.tfg.games.kendoku.Kendoku
import com.example.tfg.games.kendoku.KendokuOperation
import com.example.tfg.games.kendoku.KendokuScore
import com.example.tfg.games.kendoku.KnownKendokuOperation
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

@Entity
class Factors @JvmOverloads constructor(
    id: Long = IdGenerator.generateId("factorsGame"),
    numColumns: Int,
    numRows: Int,
    seed: Long,
    score: Score = KendokuScore(),
    completedBoard: IntArray = IntArray(numColumns * numRows),
    startBoard: IntArray = IntArray(numColumns * numRows),
    boardRegions: IntArray = IntArray(numColumns * numRows),
    operationPerRegion: MutableMap<Int, KendokuOperation> = mutableMapOf(),
    allowedOperations: Array<KnownKendokuOperation> = KnownKendokuOperation.multiplyOperation(),
): Kendoku(
    id = id,
    type = Games.FACTORS,
    numColumns = numColumns,
    numRows = numRows,
    seed = seed,
    score = score,
    completedBoard = completedBoard,
    startBoard = startBoard,
    boardRegions = boardRegions,
    operationPerRegion = operationPerRegion,
    allowedOperations = allowedOperations
) {
    companion object {
        suspend fun create(
            size: Int,
            seed: Long,
            difficulty: Difficulty,
        ): Factors? {
            val factors = Factors(
                numColumns = size,
                numRows = size,
                seed = seed
            )

            factors.createGame(difficulty)

            return if (coroutineContext.isActive) factors else null
        }
    }
}