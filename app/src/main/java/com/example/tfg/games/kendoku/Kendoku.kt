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
    private val allowedOperations: Array<KnownKendokuOperation> = KnownKendokuOperation.allOperations(),

    // Helper variables
    @Ignore
    private val operationResultPerRegion: MutableMap<Int, Int> = mutableMapOf(),
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
    // Use this instead of numColumns or numRows
    private val size = numColumns

    // Helper variables
    private var currentID = 0
    private val primes = listOf(1,2,3,5,7,11,13,17,19).takeWhile { it <= size }

    override fun maxRegionSize(): Int = size

    override fun createGame(difficulty: Difficulty) {
        super.createGame(difficulty)

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
                score = res
            }
        }
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

        val regions = mutableMapOf<Int, MutableList<Int>>()
        getPositions().forEach { position ->
            regions.getOrPut(getRegionId(position)) { mutableListOf() }.add(completedBoard[position])
        }

        regions.forEach { (regionID, values) ->
            val operation = if (values.size == 1) KendokuOperation.SUM_UNKNOWN
                else allowedOperations.filterNot {
                        // Subtractions can't have more than 2 operands
                    (it == KnownKendokuOperation.SUBTRACT && values.size != 2) ||
                        // Divisions can't have more than 2 operands and must result in integers
                    (it == KnownKendokuOperation.DIVIDE && (values.size != 2 || (values.max() % values.min() != 0))) ||
                        // Multiplications can't be result in numbers higher than 1000
                    (it == KnownKendokuOperation.MULTIPLY && (values.reduce { acc, num -> acc * num } > 1000))
                }.map { it.toGeneralEnum() }.random(random)

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
        val scoreResult = KendokuScore()
        val checkedRegions = mutableListOf<Int>()

        for (position in (0..< numPositions())) {
            if (board[position] != 0) continue
            val regionID = getRegionId(position)
            if (!checkedRegions.contains(regionID) && regionIsOneCell(regionID, position)) {
                board[position] = operationResultPerRegion[regionID]!!
                scoreResult.addScoreNewValue()
            }
            else {
                checkedRegions.add(regionID)
                possibleValues[position].addAll((1..size).toMutableSet())
            }
        }

        board.withIndex().filter { it.value != 0 }.forEach { (index, value) ->
            val coordinate = Coordinate.fromIndex(index, size, size)
            getRowPositions(coordinate.row).forEach { possibleValues[it].remove(value) }
            getColumnPositions(coordinate.column).forEach { possibleValues[it].remove(value) }
        }

        return scoreResult
    }

    override fun populateValues(boardData: BoardData): PopulateResult {
        val score = KendokuScore()

        val boardData = boardData as KendokuBoardData
        if (!boardData.isInitialized()) boardData.initialize(operationPerRegion)

        val possibleValues = boardData.possibleValues
        val actualValues = boardData.actualValues
        val knownOperations = boardData.knownOperations
        val regionCombinations = boardData.regionCombinations

        //TODO: we can put all this outside the function
        val regions = mutableMapOf<Int, MutableList<Int>>()

        getPositions().forEach { position ->
            regions.getOrPut(getRegionId(position)) { mutableListOf() }.add(position)
        }

        for (position in getPositions()) {
            if (actualValues[position] != 0) continue

            val values = possibleValues[position]
            if (values.size == 1) {
                val value = values.first()
                addValueToActualValues(values, actualValues, position, score)
                val coordinate = Coordinate.fromIndex(index = position, size, size)

                // Delete value from the possible values in the row of position
                getRowPositions(coordinate.row).forEach { index -> possibleValues[index].remove(value) }

                // Delete value from the possible values in the column of position
                getColumnPositions(coordinate.column).forEach { index -> possibleValues[index].remove(value) }

                // When brute force has been used the value may be wrong. We check the region
                val regionID = getRegionId(position)
                val regionValues = regions[regionID]!!.map { actualValues[it] }
                if (regionValues.all { it != 0 } && !checkRegionIsOk(regionValues, regionID)) {
                    return PopulateResult.contradiction()
                }
            }
            else if(values.size == 0) {
                return PopulateResult.contradiction()
            }
        }

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)

        executeForEachLine { line ->
            val linePossibleValues = line.map { possibleValues[it] }.toTypedArray()
            val numPairs = cleanNakedPairsInLine(linePossibleValues)
            score.addNakedPairs(numPairs)

            val numTriples = cleanNakedTriplesInLine(linePossibleValues)
            score.addNakedTriples(numTriples)

            val numSPT = cleanHiddenSinglesPairsTriplesInline(linePossibleValues)
            score.addHiddenSPT(numSPT)
        }

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)


        for ((regionID, region) in regions.entries) {
            val regionValues = region.map {
                val possVal = possibleValues[it]
                if (possVal.isEmpty()) mutableListOf(actualValues[it])
                else possVal
            }.toTypedArray()

            if (regionValues.all { it.size == 1 }) { //Region is completed
                regionCombinations.remove(regionID)
                continue
            }


            val combinations = regionCombinations[regionID] ?.also { reduceCombinations(it, regionValues) }
                ?: run {
                    val operationRes = operationResultPerRegion[regionID]!!
                    knownOperations[regionID]
                        ?.let { getRegionCombinations(boardData.possibleValues, boardData.actualValues, region, operationRes, it) }
                        ?: run { forceGetRegionCombinations(boardData, regionID, region, operationRes) }
                }.also { boardData.setRegionCombinations(regionID, it) }


            val regionIndexesPerColumn = mutableMapOf<Int, MutableList<Int>>()
            val regionIndexesPerRow = mutableMapOf<Int, MutableList<Int>>()
            region.forEachIndexed { index, position ->
                val coordinate = Coordinate.fromIndex(position, size, size)
                regionIndexesPerRow.getOrPut(coordinate.row) { mutableListOf() }.add(index)
                regionIndexesPerColumn.getOrPut(coordinate.column) { mutableListOf() }.add(index)
            }

            // This reduces combinations only
            val numCUO1 = cageUnitOverlapType1(region, combinations, actualValues, possibleValues, regionIndexesPerColumn, regionIndexesPerRow)
            score.addCageUnitOverlapType1(numCUO1)

            // This reduces combinations only
            val numChanges2 = biValueAttackOnRegion(region, possibleValues, combinations, regionIndexesPerColumn, regionIndexesPerRow)
            score.addBiValueAttack(numChanges2)

            val numCUO2 = cleanCageUnitOverlapType2(regionID, region, combinations, possibleValues)
            score.addCageUnitOverlapType2(numCUO2)

            val numValuesRemoved = reducePossibleValuesUsingCombinations(combinations, region, possibleValues)
            score.addCombinations(numValuesRemoved)
        }

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)


        executeForEachLine { line ->
            combinationComparison(line, regions, regionCombinations)
        }

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)


        val numInniesOuties = cleanInniesAndOuties(actualValues, regions, possibleValues) { regionID -> knownOperations[regionID] == KnownKendokuOperation.SUM }
        score.addInniesOuties(numInniesOuties)

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)


        val lockedNumbersInRows = getLockedNumbersInRow(possibleValues = possibleValues)
        val lockedNumbersInColumns = getLockedNumbersInColumn(possibleValues = possibleValues)

        val numXWings = cleanXWing(possibleValues, lockedNumbersInRows, lockedNumbersInColumns)
        score.addXWings(numXWings)

        val numColoring = cleanColoring(possibleValues, lockedNumbersInRows, lockedNumbersInColumns)
        score.addColoring(numColoring)


        return if (score.get() > 0) PopulateResult.success(score)
        else PopulateResult.noChangesFound()
    }

    private fun checkRegionIsOk(regionValues: List<Int>, regionID: Int): Boolean {
        return allowedOperations
            .filterNot { regionValues.size > 2 && it.isDivideOrSubtract() }
            .any { it.operate(regionValues) == operationResultPerRegion[regionID] }
    }

    private fun executeForEachLine(strategies: (IntProgression) -> Unit) {
        for (rowIndex in (0..< size)) {
            strategies(getRowPositions(rowIndex))
        }
        for (columnIndex in (0..< size)) {
            strategies(getColumnPositions(columnIndex))
        }
    }

    /*
    * Returns a map of values that have locked numbers.
    * The inside map represents the line (row or column) 2 positions that are the locked numbers.
    * */
    private fun getLockedNumbersInLine(
        possibleValues: Array<MutableList<Int>>,
        line: (Int) -> IntProgression
    ): MutableMap<Int, MutableMap<Int, Pair<Int, Int>>> {
        val lockedNumbers = mutableMapOf<Int, MutableMap<Int, Pair<Int, Int>>>()
        for (lineIndex in (0..< size)) {
            //If line is a row the index are the columns and vice versa.
            val indexesPerNumber = Array(size){ mutableListOf<Int>() }
            line(lineIndex).map { possibleValues[it] }.forEachIndexed { index, ints ->
                ints.forEach { number -> indexesPerNumber[number-1].add(index) }
            }

            //size == 2 means its a locked number, a number that appears only twice in the line
            indexesPerNumber.withIndex().filter { it.value.size == 2 }.forEach{ (number, indexes) ->
                val map = lockedNumbers.getOrPut(number+1) { mutableMapOf() }
                map[lineIndex] = indexes[0] to indexes[1]
            }
        }
        return lockedNumbers
    }

    private fun getLockedNumbersInColumn(possibleValues: Array<MutableList<Int>>) = getLockedNumbersInLine(
        possibleValues = possibleValues,
        line = { columnIndex -> getColumnPositions(columnIndex) }
    )

    private fun getLockedNumbersInRow(possibleValues: Array<MutableList<Int>>) = getLockedNumbersInLine(
        possibleValues = possibleValues,
        line = { rowIndex -> getRowPositions(rowIndex) }
    )

    internal fun cleanColoring(
        possibleValues: Array<MutableList<Int>>,
        lockedNumbersPerRow: MutableMap<Int, MutableMap<Int, Pair<Int, Int>>> = getLockedNumbersInRow(possibleValues = possibleValues),
        lockedNumbersPerColumn: MutableMap<Int, MutableMap<Int, Pair<Int, Int>>> = getLockedNumbersInColumn(possibleValues = possibleValues)
    ): Int {
        data class TmpMaps(val rowMap: MutableMap<Int, MutableList<Int>> = mutableMapOf(), val columnMap: MutableMap<Int, MutableList<Int>> = mutableMapOf()) {
            fun addToRowMap(coordinate: Coordinate) {
                rowMap.computeIfAbsent(coordinate.row){ mutableListOf() }.add(coordinate.toIndex(size))
            }
            fun addToColumnMap(coordinate: Coordinate) {
                columnMap.computeIfAbsent(coordinate.column){ mutableListOf() }.add(coordinate.toIndex(size))
            }
        }

        fun searchConnectedCoordinates(
            row: Int, column: Int,
            lnPerColumn: MutableMap<Int, Pair<Int, Int>>,
            lnPerRow: MutableMap<Int, Pair<Int, Int>>,
            visitedCoordinates: MutableSet<Coordinate>,
            tmpMaps: TmpMaps
        ) {
            val searchConnectedCoordinates = { row: Int, column: Int ->
                val coordinate = Coordinate(row, column)
                val valueAdded = visitedCoordinates.add(coordinate)
                if (valueAdded) {
                    lnPerRow[row]?.let { (column1, column2) ->
                        if (column == column2) searchConnectedCoordinates(row, column1, lnPerColumn, lnPerRow, visitedCoordinates, tmpMaps)
                        else searchConnectedCoordinates(row, column2, lnPerColumn, lnPerRow, visitedCoordinates, tmpMaps)
                    } ?: run {
                        tmpMaps.addToRowMap(coordinate)
                    }
                }
            }

            val coordinate = Coordinate(row, column)
            val valueAdded = visitedCoordinates.add(coordinate)
            if (!valueAdded) return

            lnPerColumn[column]?.let { (row1, row2) ->
                if (row == row2) searchConnectedCoordinates(row1, column)
                else searchConnectedCoordinates(row2, column)
            } ?: run {
                tmpMaps.addToColumnMap(coordinate)
            }
        }

        fun graphCoordinates(
            lnPerRow: MutableMap<Int, Pair<Int, Int>>,
            lnPerColumn: MutableMap<Int, Pair<Int, Int>>
        ): MutableList<TmpMaps> {
            val graph: MutableList<TmpMaps> = mutableListOf()
            val visitedCoordinates = mutableSetOf<Coordinate>()
            for ((row, lnColumn) in lnPerRow) {
                val tmpMaps = TmpMaps()
                searchConnectedCoordinates(row, lnColumn.first, lnPerColumn, lnPerRow, visitedCoordinates, tmpMaps)
                searchConnectedCoordinates(row, lnColumn.second, lnPerColumn, lnPerRow, visitedCoordinates, tmpMaps)
                graph.add(tmpMaps)
            }// No need to loop the columns because the only coordinates left wont be connected to any graph
            return graph
        }

        var result = 0

        for (value in (1.. size)) {
            val lnPerRow = lockedNumbersPerRow[value] ?: continue
            val lnPerColumn = lockedNumbersPerColumn[value] ?: continue
            val graph = graphCoordinates(lnPerRow, lnPerColumn)

            for (tmpMaps in graph) {
                tmpMaps.rowMap.filterValues { it.size == 2 }.forEach { (rowIndex, positions) ->
                    val possibleValuesChanged = cleanValueFromPossibleValues(value, from = getRowPositions(rowIndex), possibleValues, positions)
                    if (possibleValuesChanged) result++
                }
                tmpMaps.columnMap.filterValues { it.size == 2 }.forEach { (columnIndex, positions) ->
                    val possibleValuesChanged = cleanValueFromPossibleValues(value, from = getColumnPositions(columnIndex), possibleValues, positions)
                    if (possibleValuesChanged) result++
                }
            }
        }

        return result
    }


    /*
* Deletes value from the possibleValues following the positions that from returns.
* Ignores the positions in ignorePositions and the indexes in ignoreIndexes.
* */
    private fun cleanValueFromPossibleValues(
        value: Int,
        from: IntProgression,
        possibleValues: Array<MutableList<Int>>,
        ignorePositions: List<Int> = emptyList(),
        ignoreIndexes: List<Int> = emptyList()
    ): Boolean {
        var possibleValuesChanged = false
        from.withIndex().filterNot { ignorePositions.contains(it.value) || ignoreIndexes.contains(it.index) }.forEach { (_, position) ->
            val valuesChanged = possibleValues[position].remove(value)
            possibleValuesChanged = possibleValuesChanged || valuesChanged
        }
        return possibleValuesChanged
    }


    internal fun cleanXWing(
        possibleValues: Array<MutableList<Int>>,
        lockedNumbersPerRow: MutableMap<Int, MutableMap<Int, Pair<Int, Int>>> = getLockedNumbersInRow(possibleValues = possibleValues),
        lockedNumbersPerColumn: MutableMap<Int, MutableMap<Int, Pair<Int, Int>>> = getLockedNumbersInColumn(possibleValues = possibleValues)
    ): Int {

        fun clean(lockedNumbersMap: MutableMap<Int, MutableMap<Int, Pair<Int, Int>>>, from: (Int) -> IntProgression): Int {
            var numChanges = 0
            for ((number, lockedNumbers) in lockedNumbersMap.entries) {
                for ((drop, entry) in lockedNumbers.entries.withIndex()) {
                    val otherEntry = lockedNumbers.entries.drop(drop+1).find { it.value == entry.value } ?: break
                    val ignoreIndexes = listOf(entry.key, otherEntry.key)
                    // Found x-wing
                    var changed = cleanValueFromPossibleValues(number, from(entry.value.first), possibleValues, ignoreIndexes = ignoreIndexes)
                    changed = cleanValueFromPossibleValues(number, from(entry.value.second), possibleValues, ignoreIndexes = ignoreIndexes) || changed
                    if (changed) numChanges++
                }
            }
            return numChanges
        }

        var numChanges = clean(lockedNumbersPerRow) { getColumnPositions(it) }
        numChanges += clean(lockedNumbersPerColumn) { getRowPositions(it) }

        return numChanges
    }

    // TODO: This doesn't improve the solver one bit. Find any possible error
    internal fun cleanYWing(
        possibleValues: Array<MutableList<Int>>,
        lockedNumbersInRows: MutableMap<Int, MutableMap<Int, Pair<Int, Int>>> = getLockedNumbersInRow(possibleValues = possibleValues),
        lockedNumbersInColumns: MutableMap<Int, MutableMap<Int, Pair<Int, Int>>> = getLockedNumbersInColumn(possibleValues = possibleValues)
    ) {
        for ((numberA, data) in lockedNumbersInRows) {
            for ((row, columns) in data) {
                val (columnB, columnC) = columns
                val possValuesB = possibleValues[Coordinate(row, columnB).toIndex(size)]
                val possValuesC = possibleValues[Coordinate(row, columnC).toIndex(size)]
                val numberB = possValuesB.find { it != numberA }!!
                val numberC = possValuesC.find { it != numberA }!!

                if (numberB == numberC || possValuesB.size > 2 || possValuesC.size > 2) continue

                val cleanYWing = { number1: Int, column1: Int, number2: Int, column2: Int ->
                    lockedNumbersInColumns[number1]?.get(column1)
                        ?.let { if (it.first == row) it.second else it.first }
                        ?.let { row2 ->
                            val possValuesB2 = possibleValues[Coordinate(row2, column1).toIndex(size)]
                            if (possValuesB2.size == 2 && possValuesB2.contains(number2)) {
                                //Found Y-Wing. We can delete numberC from (rowB, columnC)
                                val removed = possibleValues[Coordinate(row2, column2).toIndex(size)].remove(number2)
                                removed
                            } else false
                        }
                }
                cleanYWing(numberB, columnB, numberC, columnC)
                cleanYWing(numberC, columnC, numberB, columnB)
            }
        }

    }

    internal fun combinationComparison(
        line: IntProgression,
        regions: MutableMap<Int, MutableList<Int>>,
        regionCombinations: MutableMap<Int, MutableList<IntArray>>
    ) {
        val lineCombinations = mutableListOf<MutableList<IntArray>>()
        val combinationsIndexes = mutableListOf<MutableList<Int>>()

        fun foundContradiction(indexes: List<Int>, values: List<Int>): Boolean {
            if (indexes.isEmpty()) return false

            val index = indexes.first()
            val combIndexes = combinationsIndexes[index]
            val combinations = lineCombinations[index].filterNot { combination ->
                combIndexes.any { i -> values.contains(combination[i]) }
            }

            return combinations.isEmpty() || combinations.all { combination ->
                foundContradiction(indexes.drop(1), combination.filterIndexed { i, _ -> combIndexes.contains(i) }.plus(values))
            }
        }

        // Initialize lineCombinations and combinationsIndexes
        var position = line.first
        while (position <= line.last) {
            val regionID = getRegionId(position)
            val positions = regions[regionID]!!
            val combinations = regionCombinations[regionID]

            if (combinations == null){
                position += line.step
                continue
            }

            //if (combinations.size > 9) return

            lineCombinations.add(combinations)

            val indexes = mutableListOf<Int>()
            var index = positions.indexOf(position)
            while (index != -1) {
                indexes.add(index)
                position += line.step
                index = positions.indexOf(position)
            }
            combinationsIndexes.add(indexes)
        }

        val indexes = lineCombinations.map { it.size }.withIndex().sortedBy { it.value }.map { it.index }
        indexes.forEach { index ->
            val combinations = lineCombinations[index]
            val combIndexes = combinationsIndexes[index]
            combinations.removeIf { combination ->
                foundContradiction(indexes.filter { it != index }, combination.filterIndexed { i, _ -> combIndexes.contains(i) })
            }
        }
    }

    internal fun cleanInniesAndOuties(
        actualValues: IntArray,
        regions: MutableMap<Int, MutableList<Int>>,
        possibleValues: Array<MutableList<Int>>,
        getRegionID: (Int) -> Int = { getRegionId(it) },
        regionIsSum: (Int) -> Boolean
    ): Int {
        fun clean(lines: IntRange, rectangleSum: Int, getLinePositions: (Int) -> IntProgression): Int {
            var sum = 0
            val unknownPositions = mutableListOf<Int>()
            val sumRegionsInside = mutableMapOf<Int, MutableList<Int>>()

            lines.forEach { line ->
                getLinePositions(line).forEach { position ->
                    val regionID = getRegionID(position)
                    if (!regionIsSum(regionID)) {
                        val actualValue = actualValues[position]
                        sum += actualValue
                        if (actualValue == 0) {
                            if (possibleValues[position].isEmpty()) return 0
                            unknownPositions.add(position)
                        }
                    } else {
                        sumRegionsInside.getOrPut(regionID) { mutableListOf() }.add(position)
                    }
                }
            }

            if (unknownPositions.size > 3 || unknownPositions.isEmpty()) return 0

            for ((regionID, positionsInside) in sumRegionsInside.entries) {
                val positionsOutside = regions[regionID]!!.filter { !positionsInside.contains(it) }

                if (positionsOutside.any { actualValues[it] == 0 }) return 0

                val sumOutside = positionsOutside.sumOf { actualValues[it] }
                sum += operationResultPerRegion[regionID]!! - sumOutside
            }

            val combSum = rectangleSum - sum
            val combinations = getRegionSumCombinations(possibleValues, unknownPositions, combSum)

            return reducePossibleValuesUsingCombinations(
                combinations,
                unknownPositions,
                possibleValues
            )
        }

        var numValuesRemoved = 0
        (1..4).forEach { numRowsColumns ->
            (0..size-numRowsColumns).forEach { firstRowColumn ->
                val rectangleSum = (1..size).sum() * numRowsColumns
                val rows = (firstRowColumn..< firstRowColumn+numRowsColumns)
                numValuesRemoved += clean(rows, rectangleSum) { int: Int -> getRowPositions(int) }
                val columns = (firstRowColumn..< firstRowColumn+numRowsColumns)
                numValuesRemoved += clean(columns, rectangleSum) { int: Int -> getColumnPositions(int) }
            }
        }

        return numValuesRemoved
    }

    // If a number appears in a combination and that number forms a line we can delete that number from
    // the other positions in that line
    internal fun cleanCageUnitOverlapType2(
        regionID: Int,
        region: MutableList<Int>,
        combinations: MutableList<IntArray>,
        possibleValues: Array<MutableList<Int>>
    ): Int {
        var result = 0

        val numberFrequency = IntArray(size)
        val numberRegionPositions = Array(size){ mutableSetOf<Int>() }
        combinations.forEach { combination ->
            val numberAppears = BooleanArray(size)
            combination.forEachIndexed { regionIndex, number ->
                if (!numberAppears[number-1]){
                    numberAppears[number-1] = true
                    numberFrequency[number-1] ++
                }
                val position = region[regionIndex]
                numberRegionPositions[number-1].add(position)
            }
        }

        val deleteNumberFromLinePossibleValues = { number: Int, line: IntProgression ->
            var possibleValuesChanged = false
            line.filter { getRegionId(it) != regionID }.forEach {
                val res = possibleValues[it].remove(number)
                possibleValuesChanged = possibleValuesChanged || res
            }
            possibleValuesChanged
        }

        for ((index, frequency) in numberFrequency.withIndex()) {
            val regionPositions = numberRegionPositions[index]

            // We skip if the number doesn't appear in all combinations or appears only in one position
            if (frequency < combinations.size || regionPositions.size < 2) continue

            val number = index + 1
            val positions = regionPositions.drop(1)

            val coordinate = Coordinate.fromIndex(regionPositions.first(), size, size)
            val possibleValuesChanged =
                if (positions.all { coordinate.row == Coordinate.getRow(it, size) }) { // Same row
                    deleteNumberFromLinePossibleValues(number, getRowPositions(coordinate.row))
                } else if(positions.all { coordinate.column == Coordinate.getColumn(it, size) }) { // Same column
                    deleteNumberFromLinePossibleValues(number, getColumnPositions(coordinate.column))
                } else false

            if (possibleValuesChanged) result ++
        }

        return result
    }

    internal fun cageUnitOverlapType1(
        region: MutableList<Int>,
        combinations: MutableList<IntArray>,
        actualValues: IntArray,
        possibleValues: Array<MutableList<Int>>,
        regionIndexesPerColumn: MutableMap<Int, MutableList<Int>>,
        regionIndexesPerRow: MutableMap<Int, MutableList<Int>>
    ): Int {

        fun reduceCombinations(linePositions: IntProgression, regionIndexes: MutableList<Int>): Int {
            val regionPositions = regionIndexes.map { index -> region[index] }
                .filterNot { position -> actualValues[position] != 0 }

            if (regionPositions.size < 2) return 0

            val numberAppearances = BooleanArray(size)
            linePositions.filterNot { position -> regionPositions.contains(position) }
                .forEach {
                    possibleValues[it].forEach { number -> numberAppearances[number-1] = true }
                    val v = actualValues[it]
                    if (v != 0) numberAppearances[v-1] = true
                }
            val values = numberAppearances.withIndex().filterNot { (_, b) -> b }.map { (index, _) -> index + 1 }

            if (values.isEmpty()) return 0

            return if (combinations.removeIf { comb -> !regionIndexes.any { index -> values.contains(comb[index]) } }) 1 else 0
        }

        var result = regionIndexesPerColumn
            .map { (column, regionIndexes) -> reduceCombinations(getColumnPositions(column), regionIndexes) }
            .sum()
        result += regionIndexesPerRow
            .map { (row, regionIndexes) -> reduceCombinations(getRowPositions(row), regionIndexes) }
            .sum()
        return result
    }

    internal fun biValueAttackOnRegion(
        region: MutableList<Int>, 
        possibleValues: Array<MutableList<Int>>, 
        combinations: MutableList<IntArray>,
        regionIndexesPerColumn: MutableMap<Int, MutableList<Int>>,
        regionIndexesPerRow: MutableMap<Int, MutableList<Int>>
    ): Int {
        var result = 0
        val deleteCombinationsByBiValue = { line: IntProgression, indexes: MutableList<Int> ->
            val biValues = line
                .filter { position -> indexes.all { index -> region[index] != position } }
                .map { position -> possibleValues[position] }
                .filter { it.size == 2 }
            // Delete combinations that have biValue in more than one of the indexes of the combination
            // As its the same line the values won't be repeated
            biValues.forEach { biValue ->
                val res = combinations.removeIf { combination ->
                    indexes.filter { index -> biValue.contains(combination[index]) }.size > 1
                }
                if (res) {
                    result++
                }
            }
        }

        regionIndexesPerRow.forEach { (row, indexes) -> deleteCombinationsByBiValue(getRowPositions(row), indexes)}
        regionIndexesPerColumn.forEach { (column, indexes) -> deleteCombinationsByBiValue(getColumnPositions(column), indexes)}
        return result
    }

    private fun reduceCombinations(combinations: MutableList<IntArray>, values: Array<MutableList<Int>>) {
        combinations.removeIf { combination -> !combination.withIndex().all { (index, value) ->
            values[index].contains(value)
        } }
    }

    private fun reducePossibleValuesUsingCombinations(
        combinations: MutableList<IntArray>,
        region: MutableList<Int>,
        possibleValues: Array<MutableList<Int>>
    ): Int {
        val possibleValuesToKeep = Array(region.size) { mutableSetOf<Int>()  }
        combinations.forEach { combination ->
            combination.forEachIndexed { index, value -> possibleValuesToKeep[index].add(value) }
        }

        var numValuesRemoved = 0
        region.forEachIndexed { index, position ->
            val valuesChanged = possibleValues[position].removeIf { !possibleValuesToKeep[index].contains(it) }
            if (valuesChanged) numValuesRemoved++
        }

        return numValuesRemoved
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
            if (numberAppearances == 0) continue
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

            val union = valueAppearsInIndexes.withIndex().drop(value).filter { it.value.isNotEmpty() }
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
                if (changedValues) {
                    numPairs++
                }
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
                if (changedValues) {
                    numTriples++
                }
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
        //TODO: Calculate multiples and reduce possible values
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
        region: List<Int>,
        subtraction: Int
    ): MutableList<IntArray> {
        val combinations = mutableListOf<IntArray>()
        val possibleValues1 = possibleValues[region[0]]
        val possibleValues2 = possibleValues[region[1]]
        val addValues = { int1: Int, int2: Int ->
            if (possibleValues1.contains(int1) && possibleValues2.contains(int2)) combinations.add(intArrayOf(int1, int2))
            if (possibleValues1.contains(int2) && possibleValues2.contains(int1)) combinations.add(intArrayOf(int2, int1))
        }

        val position = region.find { board[it] != 0 }
        if (position != null) {
            val int1 = board[position]
            possibleValues[position].add(int1)
            addValues(int1, int1 + subtraction)
            addValues(int1, int1 - subtraction)
            possibleValues[position].clear()
        }
        else (subtraction+1 .. size).forEach { addValues(it, it - subtraction) }

        return combinations
    }

    private fun getRegionDivideCombinations(
        possibleValues: Array<MutableList<Int>>,
        board: IntArray,
        region: List<Int>,
        division: Int
    ): MutableList<IntArray> {
        val combinations = mutableListOf<IntArray>()

        if (division == 1) return combinations

        val possibleValues1 = possibleValues[region[0]]
        val possibleValues2 = possibleValues[region[1]]

        val addValues = { int1: Int, int2: Int ->
            if (possibleValues1.contains(int2) && possibleValues2.contains(int1)) combinations.add(intArrayOf(int2, int1))
            if (possibleValues1.contains(int1) && possibleValues2.contains(int2)) combinations.add(intArrayOf(int1, int2))
        }

        val position = region.find { board[it] != 0 }
        if (position != null) {
            val int1 = board[position]
            possibleValues[position].add(int1)
        }

        (division..size).filter { it%division == 0 }.forEach { addValues(it, it/division) }

        if (position != null) possibleValues[position].clear()

        return combinations
    }

    /*
    * Tries to deduce the operation. If it can't, return all the combinations using all the valid operations
    * */
    private fun forceGetRegionCombinations(
        boardData: KendokuBoardData,
        regionID: Int,
        region: MutableList<Int>,
        operationResult: Int
    ): MutableList<IntArray> {
        // TODO: Decide whether to use score here or not

        val (operation, combinations) = if (operationResult == 1) {
            KnownKendokuOperation.SUBTRACT to getRegionCombinations(boardData.possibleValues, boardData.actualValues, region, operationResult, KnownKendokuOperation.SUBTRACT)
        }
        else if (operationResult > region.size*size) {
            KnownKendokuOperation.MULTIPLY to getRegionCombinations(boardData.possibleValues, boardData.actualValues, region, operationResult, KnownKendokuOperation.MULTIPLY)
        }
        else {
            val validOps = allowedOperations.filterNot { it.isDivideOrSubtract() && (region.size > 2 || operationResult > size) }
                .map { it to getRegionCombinations(boardData.possibleValues, boardData.actualValues, region, operationResult, it) }
                .filter { (_, combinations) -> combinations.isNotEmpty() }

            if (validOps.size == 1 || validOps.size == 2 && validOps[0].second.size == validOps[1].second.size
                && validOps[0].second.withIndex().all {(i, comb) -> validOps[1].second[i].contentEquals(comb)}
            ) {
                validOps.first()
            }
            else return validOps.flatMap { it.second }.distinctBy { it.toList() }.toMutableList()
        }

        // We save the operation but the combinations are saved later
        boardData.knownOperations[regionID] = operation

        return combinations
    }

    internal fun getRegionCombinations(
        possibleValues: Array<MutableList<Int>>,
        board: IntArray,
        region: List<Int>,
        operationResult: Int,
        operation: KnownKendokuOperation
    ): MutableList<IntArray> {
        val filteredRegion = mutableListOf<Int>()
        val arraySyntax = IntArray(region.size)
        region.forEachIndexed { index, i ->
            val value = board[i]
            if (value == 0) filteredRegion.add(i)
            arraySyntax[index] = value
        }

        if (filteredRegion.isEmpty() || filteredRegion.any { possibleValues[it].isEmpty() }) return mutableListOf()

        val addFilteredValuesBack = { combinations: MutableList<IntArray> ->
            combinations.replaceAll { arr ->
                val iterator = arr.iterator()
                arraySyntax.map { if (it == 0) iterator.next() else it }.toIntArray()
            }
        }

        return when(operation){
            KnownKendokuOperation.SUBTRACT -> getRegionSubtractCombinations(possibleValues, board, region, operationResult)
            KnownKendokuOperation.DIVIDE -> getRegionDivideCombinations(possibleValues, board, region, operationResult)
            KnownKendokuOperation.SUM -> {
                val combinations = getRegionSumCombinations(possibleValues, filteredRegion,
                    sum = operationResult - arraySyntax.sum())
                if (filteredRegion.size < region.size) addFilteredValuesBack(combinations)
                combinations
            }
            KnownKendokuOperation.MULTIPLY -> {
                val combinations = getRegionMultiplyCombinations(possibleValues, filteredRegion,
                    multiplication = operationResult / (arraySyntax.filter { it != 0 }.reduceOrNull { acc, i -> acc * i } ?: 1)  )
                if (filteredRegion.size < region.size) addFilteredValuesBack(combinations)
                combinations
            }
        }
    }

    override fun boardMeetsRulesStr(board: IntArray): String {
        val regions = mutableMapOf<Int, MutableList<Int>>()
        val fillRegions = { positions: IntProgression ->
            positions.forEach { Utils.addToMapList(getRegionId(it), it, regions) }
        }

        val tmp = IntArray(size)
        val fillTmpArray = { (0..< size).forEach { tmp[it] = it + 1 } }
        val deleteValuesFromTmp = { indexes: IntProgression -> indexes.forEach { tmp[board[it]] = 0 } }
        val tmpArrayIsNotZero = { tmp.sum() != 0 }

        for (rowIndex in (0..< size)) {
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
        fun create(
            size: Int,
            seed: Long,
            difficulty: Difficulty,
            printEachBoardState: Boolean = false
        ): Kendoku {
            val kendoku = Kendoku(
                size = size,
                seed = seed,
                printEachBoardState = printEachBoardState
            )

            kendoku.createGame(difficulty)

            return kendoku
        }

        // For testing
        fun solveBoard(
            seed: Long,
            size: Int,
            startBoard: IntArray,
            completedBoard: IntArray,
            regions: IntArray,
            operationPerRegion: MutableMap<Int, KendokuOperation>,
            operationResultPerRegion: MutableMap<Int, Int>
        ): Kendoku {
            val kendoku = Kendoku(
                id = 0,
                size = size,
                seed = seed,
                regions = regions,
                startBoard = startBoard,
                completedBoard = completedBoard,
                operationPerRegion = operationPerRegion,
                operationResultPerRegion = operationResultPerRegion
            )

            val score = kendoku.solveBoard(kendoku.startBoard)
            kendoku.score.add(score)

            return kendoku
        }
    }
}