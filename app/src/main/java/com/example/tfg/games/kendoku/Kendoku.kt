package com.example.tfg.games.kendoku

import androidx.room.Entity
import androidx.room.Ignore
import com.example.tfg.common.IdGenerator
import com.example.tfg.common.enums.Direction
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.utils.Curves
import com.example.tfg.common.utils.Utils
import com.example.tfg.games.common.AbstractGame
import com.example.tfg.games.common.BoardData
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.PopulateResult
import com.example.tfg.games.common.Score

@Entity
class Kendoku(
    id: Long = IdGenerator.generateId("kendokuGame"),
    size: Int,
    seed: Long,
    score: KendokuScore = KendokuScore(),
    completedBoard: IntArray = IntArray(size * size),
    startBoard: IntArray = IntArray(size * size),
    regions: IntArray = IntArray(size * size),
    private val operationPerRegion: MutableMap<Int, KendokuOperation> = mutableMapOf(),
    private val allowedOperations: Array<KendokuOperation> = KendokuOperation.allOperations(),

    @Ignore
    private var printEachBoardState: Boolean = false
): AbstractGame(
    id = id,
    type = Games.HAKYUU,
    numColumns = size,
    numRows = size,
    seed = seed,
    score = score,
    completedBoard = completedBoard,
    startBoard = startBoard,
    boardRegions = regions
) {
    // Helper variables
    private var currentID = 0
    private val operationResultPerRegion: MutableMap<Int, Int> = mutableMapOf()

    // Use this instead of numColumns or numRows
    private val size = numColumns

    override fun maxRegionSize(): Int = size

    override fun createGame(difficulty: Difficulty) {
        super.createGame(difficulty)
        score.reset()

        var actualScore: Score? = null
        val knownOperationsPerRegion = operationPerRegion.filterValues { !it.isUnknown() }.toMutableMap()
        while (knownOperationsPerRegion.isNotEmpty()) {
            // Remove random value from remainingOperationsPerRegion
            val randomRegion = knownOperationsPerRegion.keys.random(random)
            knownOperationsPerRegion.remove(randomRegion)

            // Reverse the random region operation to its unknown version
            operationPerRegion[randomRegion] = operationPerRegion[randomRegion]!!.reverse()

            val tmpBoard = startBoard.clone()

            val res = solveBoard(tmpBoard)

            if (res == null || res.isTooHighForDifficulty(difficulty)) {
                // Reverse the operation back
                operationPerRegion[randomRegion] = operationPerRegion[randomRegion]!!.reverse()
            }
            else {
                actualScore = res
            }
        }

        score.add(actualScore)
    }

    override fun createCompleteBoard(remainingPositions: MutableSet<Int>) {
        // Create regions

        while (remainingPositions.isNotEmpty()) {
            propagateRandomEmptyRegion(remainingPositions)
        }

        // Populate cells

        latinSquare(size).forEachIndexed { row, values ->
            values.forEachIndexed { column, value ->
                completedBoard[row*size + column] = value
            }
        }

        // Create operations

        boardRegions.groupBy { it }.forEach { (regionID, values) ->
            val operation = if (values.size == 1) KendokuOperation.SUM_UNKNOWN
                else KendokuOperation.knownOperations().filterNot {
                        // Filter out disallowed operations
                    !allowedOperations.contains(it) ||
                        // Subtractions can't have more than 2 operands
                    (it == KendokuOperation.SUBTRACT && values.size != 2) ||
                        // Divisions can't have more than 2 operands and must result in integers
                    (it == KendokuOperation.DIVIDE && (values.size != 2 || (values.max() % values.min() != 0))) ||
                        // Multiplications can't be result in numbers higher than 1000
                    (it == KendokuOperation.MULTIPLY && (values.reduce { acc, num -> acc * num } > 1000))
                }.random(random)

            operationResultPerRegion[regionID] = operation.operate(values)
            operationPerRegion[regionID] = operation
        }
    }

    private fun propagateRandomEmptyRegion(
        remainingPositions: MutableSet<Int>,
        numPropagations: Int = randomPropagationNumber()
    ) {
        currentID++

        val seed = remainingPositions.random(random)
        boardRegions[seed] = currentID
        remainingPositions.remove(seed)

        val region = mutableListOf(seed)

        for (index in 1..numPropagations) {
            propagateOnce(remainingPositions = remainingPositions, region = region)
        }

        if (printEachBoardState) {
            print(printBoardHTML(completedBoard, boardRegions, true))
        }
    }

    private fun randomPropagationNumber(): Int {
        return ((maxRegionSize() - 1) * Curves.lowerValues(random.nextDouble(1.0))).toInt() + 1
    }

    private fun propagateOnce(remainingPositions: MutableSet<Int>, region: MutableList<Int>) {
        if (region.size == maxRegionSize()) return

        for (position in region.shuffled(random)) {
            for (direction in Direction.entries.shuffled(random)) {
                val propagation = Coordinate.move(direction = direction, position = position, numColumns=size, numRows = size)

                if (propagation != null && remainingPositions.contains(propagation)){
                    region.add(propagation)
                    boardRegions[propagation] = currentID
                    remainingPositions.remove(propagation)
                    return
                }
            }
        }
    }

    private fun latinSquare(size: Int): Array<IntArray> {
        val latinSquare = Array(size) { IntArray(size) }

        for (i in 0 until size) {
            for (j in 0 until size) {
                latinSquare[i][j] = (i + j) % size + 1
            }
        }

        latinSquare.shuffle(random)

        val transposed = Array(size) { IntArray(size) }
        for (i in 0 until size) {
            for (j in 0 until size) {
                transposed[i][j] = latinSquare[j][i]
            }
        }
        transposed.shuffle(random)
        for (i in 0 until size) {
            for (j in 0 until size) {
                latinSquare[i][j] = transposed[j][i]
            }
        }

        val perm = (1..size).shuffled(random)
        for (i in 0 until size) {
            for (j in 0 until size) {
                latinSquare[i][j] = perm[latinSquare[i][j] - 1]
            }
        }

        return latinSquare
    }

    override fun fillPossibleValues(possibleValues: Array<MutableList<Int>>, board: IntArray): Score {
        // These arrays shows the possible values allowed in each column and row.
        // At the beginning its all the values (1..size)
        val columnsPossibleValues = Array(size - 1){ (1..size).toMutableSet() }
        val rowsPossibleValues = Array(size - 1){ (1..size).toMutableSet() }
        val scoreResult = KendokuScore()

        for (position in (0..<numPositions())) {
            val regionID = getRegionId(position)

            val coordinate = Coordinate.fromIndex(index = position, size, size)

            // values have the common possible values of the row and column of the coordinate
            val values = rowsPossibleValues[coordinate.row].intersect(columnsPossibleValues[coordinate.column])
            val value = if (values.size == 1) values.first() else operationResultPerRegion[regionID]!!

            // If values is only one value, that value is the only possible value in that position.
            // If not, if the region is of size 1 operationResultPerRegion[regionID] is the value in that position.
            if (board[position] == 0 && (values.size == 1 || regionIsOneCell(regionID, position))) {
                board[position] = value
                scoreResult.addScoreNewValue()

                columnsPossibleValues[coordinate.column].remove(value)
                rowsPossibleValues[coordinate.row].remove(value)
            }
            else if (board[position] == 0) {
                possibleValues[position].addAll(values)
            }
        }

        return scoreResult
    }

    private fun deduceOperation(
        regionID: Int,
        region: MutableList<Int>,
        boardData: KendokuBoardData
    ): KnownKendokuOperation? {
        //TODO: Maybe this isn't exhaustive: An area can be sum or multiply but the numbers are the same 2*2 = 2+2
        val operations = allowedOperations.filter {
            it.filterOperation(
                region.associate { position ->
                    val values = boardData.possibleValues[position]
                    if (values.isEmpty()) values.add(boardData.actualValues[position])
                    Coordinate.fromIndex(position, size, size) to values
                }
            )
        }
        if (operations.size == 1) {
            val res = operations.first().reverse().toKnownEnum()!!
            boardData.knownOperations[regionID] = res
            return res
        }
        return null
    }

    override fun populateValues(boardData: BoardData): PopulateResult {
        val score = KendokuScore()

        val boardData = boardData as KendokuBoardData
        val possibleValues = boardData.possibleValues
        val actualValues = boardData.actualValues
        val knownOperations = boardData.knownOperations
        val regionCombinations = boardData.regionCombinations

        val regions = mutableMapOf<Int, MutableList<Int>>()

        for (position in getPositions()) {
            val regionID = getRegionId(position)
            Utils.addToMapList(regionID, position, regions)

            if (actualValues[position] == 0) continue

            val values = possibleValues[position]
            if (values.size == 1) {
                addValueToActualValues(values, actualValues, position, score)
                val coordinate = Coordinate.fromIndex(index = position, size, size)

                val value = values.first()

                // Delete value from the possible values in the row of position
                (0..< size).filter { row -> row != coordinate.row }.mapNotNull { row ->
                    Coordinate(row = row, column = coordinate.column).toIndex(size,size)
                }.forEach { index ->
                    possibleValues[index].remove(value)
                }

                // Delete value from the possible values in the column of position
                (0..< size).filter { column -> column != coordinate.column }.mapNotNull { column ->
                    Coordinate(row = coordinate.row, column = column).toIndex(size,size)
                }.forEach { index ->
                    possibleValues[index].remove(value)
                }
            }
            else if(values.size == 0) {
                return PopulateResult.contradiction()
            }
        }

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)

        //TODO: Complete score functionality
        for (rowIndex in (0..< size)) {
            val row = getRowPositions(rowIndex).map { possibleValues[it] }.toTypedArray()

            val numPairs = cleanNakedPairsInLine(row)

            val numTriples = cleanNakedTriplesInLine(row)

            val numSPT = cleanHiddenSinglesPairsTriplesInline(row)
        }

        for (columnIndex in (0..< size)) {
            val column = getColumnPositions(columnIndex).map { possibleValues[it] }.toTypedArray()

            val numPairs = cleanNakedPairsInLine(column)

            val numTriples = cleanNakedTriplesInLine(column)

            val numSPT = cleanHiddenSinglesPairsTriplesInline(column)
        }

        for ((regionID, region) in regions.entries) {
            val operation = knownOperations.getOrDefault(regionID, null)
                ?: deduceOperation(regionID, region, boardData)
                ?: continue

            val operationRes = operationResultPerRegion[regionID]!!

            val combinations = regionCombinations.getOrDefault(regionID, null)
                ?: getRegionCombinations(possibleValues, actualValues, region, operationRes, operation)


            TODO()
        }


        return if (score.get() > 0) PopulateResult.success(score)
        else PopulateResult.noChangesFound()
    }

    internal fun cleanHiddenSingles(line: Array<MutableList<Int>>): Int {
        var numSingles = 0
        val valueCount = IntArray(size)
        val lastAppearanceInLine = IntArray(size)

        line.forEachIndexed { lineIndex, ints ->
            ints.forEach {
                val index = it - 1
                valueCount[index]++
                lastAppearanceInLine[index] = lineIndex
            }
        }

        valueCount.withIndex().filter { (_, i) -> i == 1 }.forEach { (index, _) ->
            val possibleValues = line[lastAppearanceInLine[index]]
            val result = possibleValues.removeIf { it != index+1 }
            if (result) numSingles++
        }

        return numSingles
    }

    internal fun cleanHiddenSinglesPairsTriplesInline(line: Array<MutableList<Int>>): IntArray {
        //Number of singles, pairs and triples
        val numberSPT = IntArray(3)

        val valueAppearsInIndexes = Array(size){ mutableListOf<Int>() }
        line.forEachIndexed { lineIndex, ints ->
            ints.forEach { valueAppearsInIndexes[it-1].add(lineIndex) }
        }

        for ((i, indexes) in valueAppearsInIndexes.withIndex()) {
            val value = i + 1
            val numberAppearances = indexes.size
            if (numberAppearances == 1) {
                // Hidden single
                val res = line[indexes.first()].removeIf { it != value }
                if (res) numberSPT[0]++
                continue
            } else if (numberAppearances == 2) {
                // Possible hidden pair/triple

                val otherPairIndex = valueAppearsInIndexes.withIndex().drop(value)
                    .find { it.value == indexes }?.index

                if (otherPairIndex != null) { // Found pair
                    val valuesNotToRemove = intArrayOf(value, otherPairIndex+1)
                    var valuesChanged = false
                    indexes.forEach { index ->
                        val res = line[index].removeIf { !valuesNotToRemove.contains(it) }
                        valuesChanged = valuesChanged || res
                    }
                    if (valuesChanged) numberSPT[1]++
                    continue
                }
            }

            // Possible hidden triple

            val union = valueAppearsInIndexes.withIndex().drop(value)
                .map { (i2, indexes2) -> i2 to indexes.union(indexes2) }

            val otherTriples = union.filter { other ->
                // Different position, same content and size 3 means its a triple
                union.any { it.first != other.first && it.second == other.second && it.second.size == 3 }
            }

            if (otherTriples.size == 2) { // Found triple
                val valuesNotToRemove = intArrayOf(value, otherTriples[0].first + 1, otherTriples[1].first + 1)
                val indexes = otherTriples[0].second //previous indexes may be incomplete because triples take many forms
                var valuesChanged = false
                indexes.forEach { index ->
                    val res = line[index].removeIf { !valuesNotToRemove.contains(it) }
                    valuesChanged = valuesChanged || res
                }
                if (valuesChanged) numberSPT[2]++
            }
        }

        return numberSPT
    }

    internal fun cleanNakedPairsInLine(line: Array<MutableList<Int>>): Int {
        var numPairs = 0
        val filteredLine = line.withIndex().filter { (_, ints) -> ints.size == 2 }

        filteredLine.forEachIndexed { drop, (index, ints) ->
            val otherPair = filteredLine.drop(drop+1).find { (_, ints2) -> ints == ints2 }?.index
            if (otherPair != null) {
                var changedValues = false
                line.filterIndexed { index2, _ -> index2 != otherPair && index2 != index }
                    .forEach {
                        val res = it.removeAll(ints)
                        changedValues = changedValues || res
                    }
                if (changedValues) numPairs++
            }
        }
        return numPairs
    }

    internal fun cleanNakedTriplesInLine(line: Array<MutableList<Int>>): Int {
        var numTriples = 0
        val filteredLine = line.withIndex().filter { (_, ints) -> ints.size in 2..3 }

        filteredLine.forEachIndexed { drop, (index1, ints) ->
            val union = filteredLine.drop(drop+1).map { (index2, ints2) -> index2 to ints.union(ints2) }

            val otherTriples = union.filter { other ->
                // Different position, same content and size 3 means its a triple
                union.any { it.first != other.first && it.second == other.second && it.second.size == 3 }
            }

            if (otherTriples.size == 2) {
                var changedValues = false
                line.filterIndexed { index, _ -> index !in intArrayOf(index1, otherTriples[0].first, otherTriples[1].first) }
                    .forEach {
                        val res = it.removeAll(otherTriples[0].second)
                        changedValues = changedValues || res
                    }
                if (changedValues) numTriples++
            }
        }

        return numTriples
    }

    private fun getRegionSumCombinations(
        possibleValues: Array<MutableList<Int>>,
        region: MutableList<Int>,
        sum: Int
    ): MutableList<IntArray> {
        val combinations = mutableListOf<IntArray>()
        val regionSize = region.size
        val lastCombination = IntArray(regionSize)

        val helper = region.mapIndexed { index, position1  ->
            region.slice(0..< index).reversed().filter { position2 ->
                Coordinate.sameColumnOrRow(position1 = position1, position2 = position2, numColumns = size)
            }.map { region.indexOf(it) }
        }.toTypedArray()

        val valueAlreadyInRowOrColumn = { index: Int, value: Int ->
            helper[index].any { lastCombination[it] == value }
        }

        fun backtrack(index: Int = 0, actualSum: Int = 0): Boolean {
            val values = possibleValues[region[index]]
            if (index == regionSize - 1) {
                val subtraction = sum - actualSum
                if (values.contains(subtraction) && !valueAlreadyInRowOrColumn(index, subtraction)) {
                    lastCombination[index] = subtraction
                    combinations.add(lastCombination.clone())
                }
                return values.last() > subtraction
            }

            for (value in values.reversed()) {
                val newActualSum = actualSum + value
                if (newActualSum >= sum) continue
                if (valueAlreadyInRowOrColumn(index, value)) continue

                lastCombination[index] = value
                if (!backtrack(index = index + 1, actualSum = newActualSum)) break
            }

            return true
        }


        backtrack()

        return combinations
    }

    private fun getRegionMultiplyCombinations(
        possibleValues: Array<MutableList<Int>>,
        region: MutableList<Int>,
        multiplication: Int
    ): MutableList<IntArray> {
        val combinations = mutableListOf<IntArray>()
        val regionSize = region.size
        val lastCombination = IntArray(regionSize)

        val helper = region.mapIndexed { index, position1  ->
            region.slice(0..< index).reversed().filter { position2 ->
                Coordinate.sameColumnOrRow(position1 = position1, position2 = position2, numColumns = size)
            }.map { region.indexOf(it) }
        }.toTypedArray()

        val valueAlreadyInRowOrColumn = { index: Int, value: Int ->
            helper[index].any { lastCombination[it] == value }
        }

        fun backtrack(index: Int = 0, actualMultiplication: Int = 1): Boolean {
            val values = possibleValues[region[index]]
            if (index == regionSize - 1) {
                val division = multiplication / actualMultiplication
                if (multiplication % actualMultiplication == 0 &&
                    values.contains(division) &&
                    !valueAlreadyInRowOrColumn(index, division))
                {
                    lastCombination[index] = division
                    combinations.add(lastCombination.clone())
                }
                return values.last() > division
            }

            for (value in values.reversed()) {
                val newActualMultiplication = actualMultiplication * value
                if (newActualMultiplication > multiplication) continue
                if (valueAlreadyInRowOrColumn(index, value)) continue

                lastCombination[index] = value
                if (!backtrack(index = index + 1, actualMultiplication = newActualMultiplication)) break
            }

            return true
        }


        backtrack()

        return combinations
    }

    private fun getRegionSubtractCombinations(
        possibleValues: Array<MutableList<Int>>,
        board: IntArray,
        region: MutableList<Int>,
        subtraction: Int
    ): List<IntArray> {
        val combinations = mutableListOf<IntArray>()

        val position = region.find { board[it] != 0 }
        if (position != null) {
            val int = board[position]
            val otherPositionPossibleValues = possibleValues[region.find { it != position }!!]

            val int1 = int + subtraction
            val int2 = int - subtraction

            if (otherPositionPossibleValues.contains(int1)) combinations.add(intArrayOf(int1))
            if (otherPositionPossibleValues.contains(int2)) combinations.add(intArrayOf(int2))
        }
        else (subtraction+1 .. size).forEach {
            val possibleValues1 = possibleValues[region[0]]
            val possibleValues2 = possibleValues[region[1]]

            val int1 = it
            val int2 = int1 - subtraction

            if (possibleValues1.contains(int1) && possibleValues2.contains(int2)) combinations.add(intArrayOf(int1, int2))
            if (possibleValues1.contains(int2) && possibleValues2.contains(int1)) combinations.add(intArrayOf(int2, int1))
        }

        return combinations
    }

    private fun getRegionDivideCombinations(
        possibleValues: Array<MutableList<Int>>,
        board: IntArray,
        region: MutableList<Int>,
        division: Int
    ): List<IntArray> {
        val combinations = mutableListOf<IntArray>()
        val possibleValues1 = possibleValues[region[0]]
        val possibleValues2 = possibleValues[region[1]]

        val addValues = { int1: Int, int2: Int ->
            if (possibleValues1.contains(int2) && possibleValues2.contains(int1)) combinations.add(intArrayOf(int2, int1))
            if (possibleValues1.contains(int1) && possibleValues2.contains(int2)) combinations.add(intArrayOf(int1, int2))
        }

        val position = region.find { board[it] != 0 }
        if (position != null) {
            val int = board[position]
            val otherPositionPossibleValues = possibleValues[region.find { it != position }!!]

            val int1 = int * division
            val int2 = int / division

            if (otherPositionPossibleValues.contains(int1)) combinations.add(intArrayOf(int1))
            if (int%division == 0 && otherPositionPossibleValues.contains(int2)) combinations.add(intArrayOf(int2))
        }
        else {
            (2..division).filter { division%it == 0 }.forEach { addValues(it, division/it) }
            (division+1..size).filter { it%division == 0 }.forEach { addValues(it, it/division) }
        }

        return combinations
    }


    internal fun getRegionCombinations(
        possibleValues: Array<MutableList<Int>>,
        board: IntArray,
        region: MutableList<Int>,
        operationResult: Int,
        operation: KnownKendokuOperation
    ): List<IntArray> {
        val filteredRegion = mutableListOf<Int>()
        val arraySyntax = IntArray(region.size)
        region.forEachIndexed { index, i ->
            val value = board[i]
            if (value == 0) filteredRegion.add(i)
            arraySyntax[index] = value
        }

        if (filteredRegion.isEmpty()) return emptyList()

        return when(operation){
            KnownKendokuOperation.SUBTRACT -> getRegionSubtractCombinations(possibleValues, board, region, operationResult)
            KnownKendokuOperation.DIVIDE -> getRegionDivideCombinations(possibleValues, board, region, operationResult)
            KnownKendokuOperation.SUM -> getRegionSumCombinations(possibleValues, filteredRegion,
                sum = operationResult - arraySyntax.sum())
            KnownKendokuOperation.MULTIPLY -> getRegionMultiplyCombinations(possibleValues, filteredRegion,
                multiplication = operationResult / (arraySyntax.filter { it != 0 }.reduceOrNull { acc, i -> acc * i } ?: 1)  )
        }
    }

    override fun boardMeetsRulesStr(board: IntArray): String {
        val regions = mutableMapOf<Int, MutableList<Int>>()
        val fillRegions = { positions: IntProgression ->
            positions.forEach { Utils.addToMapList(getRegionId(it), it, regions) }
        }

        val tmp = IntArray(size)
        val fillTmpArray = { (1.. size).forEach { tmp[it] = it } }
        val deleteValuesFromTmp = { indexes: IntProgression -> indexes.forEach { tmp[board[it]] = 0 } }
        val tmpArrayIsNotZero = { tmp.sum() != 0 }

        for (rowIndex in (0..< size)) {
            val n = rowIndex*size
            val rowIndexes = getRowPositions(rowIndex)

            fillTmpArray()
            deleteValuesFromTmp(rowIndexes)

            fillRegions(rowIndexes)

            if (tmpArrayIsNotZero()) return "Row: $rowIndex is not made of unique values. Indexes: $rowIndexes"
        }

        for (columnIndex in (0..< size)) {
            val columnIndexes = getColumnPositions(columnIndex)

            fillTmpArray()
            deleteValuesFromTmp(columnIndexes)

            fillRegions(columnIndexes)

            if (tmpArrayIsNotZero()) return "Column: $columnIndex is not made of unique values. Indexes: $columnIndexes"
        }

        for ((regionID, positions) in regions) {
            val operation = operationPerRegion[regionID]!!
            val operationResult = operation.operate(positions.map { board[it] })
            val actualResult = operation.operate(positions.map { completedBoard[it] })

            if (operationResult != actualResult) return "Region: $regionID with positions: $positions, operation: $operation doesn't result in the correct value $actualResult. Result obtained: $operationResult"
        }

        return ""
    }

    override fun checkValue(position: Int, value: Int, actualValues: IntArray): Set<Int> {
        val res = mutableSetOf<Int>()

        if (value == 0) return res

        //Check row for same value
        for (rowIndex in (0..< size)) {
            getRowPositions(rowIndex)
                .filterNot { it == position }
                .filter { actualValues[it] == value }
                .forEach { res.add(it) }
        }

        //Check column for same value
        for (columnIndex in (0..< size)) {
            getColumnPositions(columnIndex)
                .filterNot { it == position }
                .filter { actualValues[it] == value }
                .forEach { res.add(it) }
        }

        return res
    }


    companion object {
        fun create(size: Int, seed: Long, difficulty: Difficulty, printEachBoardState: Boolean = false): Kendoku {
            val kendoku = Kendoku(
                size = size,
                seed = seed,
                printEachBoardState = printEachBoardState
            )

            kendoku.createGame(difficulty)

            return kendoku
        }
    }
}