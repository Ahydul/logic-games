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
    // Use this instead of numColumns or numRows
    private val size = numColumns

    // Helper variables
    private var currentID = 0
    private val primes = listOf(1,2,3,5,7,11,13,17,19).takeWhile { it <= size }

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
        val scoreResult = KendokuScore()
        val checkedRegions = mutableListOf<Int>()

        for (position in (0..< numPositions())) {
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

        executeLineStrategies(possibleValues) { line ->
            val numPairs = cleanNakedPairsInLine(line)
            score.addNakedPairs(numPairs)

            val numTriples = cleanNakedTriplesInLine(line)
            score.addNakedTriples(numTriples)

            val numSPT = cleanHiddenSinglesPairsTriplesInline(line)
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
            if (regionValues.all { it.size == 1 }) continue //Region is completed

            val operationRes = operationResultPerRegion[regionID]!!
            val operation = knownOperations[regionID]
                ?: deduceOperation(regionID, region, boardData, operationRes)
                ?: continue
            var combinations = regionCombinations[regionID]

            if (combinations == null){
                combinations = getRegionCombinations(possibleValues, actualValues, region, operationRes, operation)
                boardData.setRegionCombinations(regionID, combinations)
            }
            else reduceCombinations(combinations, regionValues)

            val numChanges = biValueAttackOnRegion(region, possibleValues, combinations)
            score.addBiValueAttack(numChanges)

            val numCUO = cleanCageUnitOverlap(regionID, region, combinations, possibleValues)
            score.addCageUnitOverlap(numCUO)

            val numValuesRemoved = reducePossibleValuesUsingCombinations(combinations, region, possibleValues)
            score.addCombinations(numValuesRemoved)
        }

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)


        //val numInniesOuties = cleanInniesAndOuties(actualValues, regions, possibleValues) { regionID -> knownOperations[regionID] == KnownKendokuOperation.SUM }
        //score.addInniesOuties(numInniesOuties)

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)


        val numXWings = cleanXWing(possibleValues)
        score.addXWings(numXWings)

        return if (score.get() > 0) PopulateResult.success(score)
        else PopulateResult.noChangesFound()
    }

    private fun checkRegionIsOk(regionValues: List<Int>, regionID: Int): Boolean {
        return allowedOperations
            .filterNot { regionValues.size > 2 && it.isDivideOrSubtract() }
            .any { it.operate(regionValues) == operationResultPerRegion[regionID] }
    }

    private fun executeLineStrategies(possibleValues: Array<MutableList<Int>>, strategies: (Array<MutableList<Int>>) -> Unit) {
        for (rowIndex in (0..< size)) {
            val row = getRowPositions(rowIndex).map { possibleValues[it] }.toTypedArray()
            strategies(row)
        }
        for (columnIndex in (0..< size)) {
            val column = getColumnPositions(columnIndex).map { possibleValues[it] }.toTypedArray()
            strategies(column)
        }
    }

    private fun deduceOperation(
        regionID: Int,
        region: MutableList<Int>,
        boardData: KendokuBoardData,
        operationResult: Int
    ): KnownKendokuOperation? {

        val operation = if (operationResult == 1) {
            KnownKendokuOperation.SUBTRACT
        }
        else if (operationResult > region.size*size) {
            KnownKendokuOperation.MULTIPLY
        }
        else {
            //TODO: Maybe this isn't exhaustive: An area can be sum or multiply but the numbers are the same 1+3+2 = 1*3*2 = 6
            val validOperations = allowedOperations.filterNot { it.isDivideOrSubtract() && region.size > 2 && operationResult <= size }
                .filter {
                val combinations = getRegionCombinations(boardData.possibleValues, boardData.actualValues, region, operationResult, it)
                combinations.isNotEmpty()
            }
            if (validOperations.size == 1) validOperations.first()

            else return null
        }

        boardData.knownOperations[regionID] = operation

        return operation
    }

    internal fun cleanXWing(possibleValues: Array<MutableList<Int>>): Int {

        fun getLockedNumbers(line: (Int) -> IntProgression): MutableMap<Int, MutableMap<Pair<Int, Int>, MutableList<Int>>> {
            val lockedNumbers = mutableMapOf<Int,MutableMap<Pair<Int,Int>,MutableList<Int>>>()
            for (rowIndex in (0..< size)) {
                val columnsPerNumber = Array(size){ mutableListOf<Int>() }
                line(rowIndex).map { possibleValues[it] }.forEachIndexed { column, ints ->
                    ints.forEach { number -> columnsPerNumber[number-1].add(column) }
                }

                //size == 2 means its a locked number, a number that appears only twice in the line
                columnsPerNumber.withIndex().filter { it.value.size == 2 }.forEach{ (number, columns) ->
                    lockedNumbers.getOrPut(number+1) { mutableMapOf() }
                        .getOrPut(columns[0] to columns[1]) { mutableListOf() }.add(rowIndex)
                }
            }
            return lockedNumbers
        }

        fun clean(lockedNumbers: MutableMap<Int, MutableMap<Pair<Int, Int>, MutableList<Int>>>, line: (Int) -> IntProgression): Int {
            var numChanges = 0
            for ((lockedNumber, columnPairs) in lockedNumbers.entries) {
                //Find pairs that repeat and delete in those columns the lockedNumber (except the actual locked numbers in the row)
                columnPairs.filter { (_, rows) -> rows.size == 2 }
                    .forEach { (columns, rows) -> columns.toList().forEach { columnIndex ->
                        var changed = false
                        line(columnIndex).forEachIndexed { rowIndex, i ->
                            changed = possibleValues[i].removeIf { value -> value == lockedNumber && !rows.contains(rowIndex) } || changed
                        }
                        if (changed) numChanges++
                    } }
            }
            return numChanges
        }

        val lockedNumbersWithColumns = getLockedNumbers { rowIndex -> getRowPositions(rowIndex) }
        var numChanges = clean(lockedNumbersWithColumns) { columnIndex -> getColumnPositions(columnIndex) }

        val lockedNumbersWithRows = getLockedNumbers { columnIndex -> getColumnPositions(columnIndex) }
        numChanges += clean(lockedNumbersWithRows) { rowIndex -> getRowPositions(rowIndex) }

        return numChanges
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

            val actualCombination = unknownPositions.map { completedBoard[it] }.toIntArray()
            if (!combinations.any { actualCombination.contentEquals(it) }) {
                val pito = 0
            }

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
    internal fun cleanCageUnitOverlap(
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

    internal fun biValueAttackOnRegion(
        region: MutableList<Int>, 
        possibleValues: Array<MutableList<Int>>, 
        combinations: MutableList<IntArray>
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

        val regionIndexesPerColumn = mutableMapOf<Int, MutableList<Int>>()
        val regionIndexesPerRow = mutableMapOf<Int, MutableList<Int>>()
        region.forEachIndexed { index, position ->
            val coordinate = Coordinate.fromIndex(position, size, size)
            regionIndexesPerRow.getOrPut(coordinate.row) { mutableListOf() }.add(index)
            regionIndexesPerColumn.getOrPut(coordinate.column) { mutableListOf() }.add(index)
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