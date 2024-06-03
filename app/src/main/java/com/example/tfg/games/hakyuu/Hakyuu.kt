package com.example.tfg.games.hakyuu

import com.example.tfg.common.Difficulty
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.utils.Curves
import com.example.tfg.common.utils.Direction
import com.example.tfg.common.utils.Utils
import com.example.tfg.games.GameType
import com.example.tfg.games.Games
import com.example.tfg.games.Score
import kotlin.random.Random

class Hakyuu(
    numColumns: Int,
    numRows: Int,
    seed: Long,
    score: HakyuuScore = HakyuuScore(),
    completedBoard: IntArray = IntArray(numColumns * numRows),
    startBoard: IntArray = IntArray(numColumns * numRows),
    regions: IntArray = IntArray(numColumns * numRows),
    var iterations: Int = 1,
): GameType(
    type = Games.HAKYUU,
    numColumns = numColumns,
    numRows = numRows,
    seed = seed,
    score = score,
    completedBoard = completedBoard,
    startBoard = startBoard,
    boardRegions = regions
) {
    // Helper variables
    private val remainingPositions: MutableSet<Int> = initRemainingPositions()
    private var currentID = 0

    private fun initRemainingPositions():  MutableSet<Int> {
        return (0..< numPositions()).toMutableSet()
    }

    override fun createGame(difficulty: Difficulty) {
        // Create completedBoard

        while (!boardCreated()) {
            propagateRandomRegion()
        }

        // Create startBoard

        startBoard.indices.forEach {
            //if (getRegionSize(getRegionId(it)) > 1) {
            startBoard[it] = completedBoard[it]
            remainingPositions.add(it) // Helper variable
            //}
        }

        var actualScore = 0

        while (!remainingPositions.isEmpty()) {
            // Remove random value from startBoard
            val randomPosition = getRandomPosition()
            remainingPositions.remove(randomPosition)
            startBoard[randomPosition] = 0

            val tmpBoard = startBoard.clone()
            val res = solveBoard(tmpBoard)

            if (res == null) {
                // Add the value back
                startBoard[randomPosition] = completedBoard[randomPosition]
            }
            else {
                actualScore = res.get()
                if (actualScore in difficulty.minScore..difficulty.maxScore) break
            }
        }
        score.add(actualScore)
    }

    override fun solveBoard(board: IntArray): Score? {
        val possibleValues = Array(numPositions()) { mutableListOf<Int>() }
        val score = HakyuuScore()
        for (position in (0..<numPositions())) {
            val size = getRegionSize(getRegionId(position))
            if (size == 1) {
                board[position] = 1
                score.addScoreNewValue()
            }
            else if (board[position] == 0) possibleValues[position].addAll(1.. size)
        }

        val remainingPositions = (0..< numPositions()).filter { board[it] == 0 }.toMutableSet()
        if (remainingPositions.size == 0) return null // Invalid board

        val res = populatePositions(possibleValues = possibleValues, actualValues = board, remainingPositions = remainingPositions)
        if (res != null) res.add(score)

        return res
    }

    override fun boardMeetsRules(board: IntArray): Boolean {
        val tmp = mutableMapOf<Int, MutableSet<Int>>()

        board.withIndex().filter { (_, value) -> value != 0 }.forEach { (position, value) ->
            val regionID = getRegionId(position)
            if (!checkRule3(value = value, position = position, actualValues = board)) {
                println("Rule 3 failed: with value:$value, position:$position")
                return false
            }
            if (tmp.containsKey(regionID)) {
                val elementAdded = tmp[regionID]!!.add(value)
                if (!elementAdded) {
                    println("Rule 2 failed: with region:${tmp[regionID]}")
                    return false
                }
            }
            else{
                tmp[regionID] = mutableSetOf(value)
            }
        }
        return true
    }

    override fun checkValue(position: Int, value: Int, actualValues: IntArray): Set<Int> {
        val res = mutableSetOf<Int>()
        val positions = getRegionPositions(regionId = getRegionId(position = position))

        //Check rule 1
        if (value > positions.size) res.add(position)

        //Check rule 2
        positions.filter { pos -> pos != position && actualValues[pos] == value}
            .forEach { res.add(it) }

        //Check rule 3
        Direction.entries.forEach { direction: Direction ->
            (1..value).mapNotNull { moveValue: Int ->
                Coordinate.move(
                    direction = direction,
                    position = position,
                    numRows = numRows,
                    numColumns = numColumns,
                    value = moveValue
                )
            } //Null values are out of bounds of the board and can be ignored
                .filter { otherPosition: Int ->
                    actualValues[otherPosition] == value
                }
                .forEach { res.add(it) }
        }

        return res
    }

    private fun boardCreated(): Boolean {
        return remainingPositions.isEmpty()
    }

    private fun propagateRandomRegion(numPropagations: Int = randomPropagationNumber(), iterations: Int = 1) {
        val seed = getRandomPosition()
        val region = mutableListOf(seed)

        for (index in 1..numPropagations) {
            propagateOnce(region)
        }

        val result = populateRegion(region)

        if (!result) {
            if (iterations > 20) modifyNeighbouringRegions(seed)
            propagateRandomRegion(numPropagations = numPropagations, iterations = iterations+1)
        }
        else {
            this.iterations += iterations
        }
    }

    private fun getRandomPosition(): Int {
        return remainingPositions.random(random)
    }

    private fun randomPropagationNumber(): Int {
        //return random.nextInt(maxRegionSize() - 1) + 1
        return ((maxRegionSize() - 1) * Curves.easierInOutSine(random.nextDouble(1.0))).toInt() + 1
    }

    private fun modifyNeighbouringRegions(seed: Int) {
        val position = Direction.entries.shuffled(random).mapNotNull { direction: Direction ->
            Coordinate.move(
                direction = direction,
                position = seed,
                numRows = numRows,
                numColumns = numColumns
            )
        }.find { boardRegions[it] > 0 }

        if (position != null) {
            val regionId = getRegionId(position)
            val region = getRegionPositions(regionId)
            remainingPositions.addAll(region)
            deleteRegion(regionId)
        }
    }

    private fun populateRegion(region: List<Int>): Boolean {
        val values = Array(region.size) { emptyList<Int>() }
        val positions = Array(region.size) { -1 }
        region.map { position ->
            position to (1..region.size)
                .filter { value -> checkRule3(value = value, position = position, actualValues = completedBoard) }
                .toList()
        }
            .sortedBy { it.second.size }
            .forEachIndexed { index, pair ->
                values[index] = pair.second
                positions[index] = pair.first
            }

        val res = tryPopulateRegion(possibleValuesPerPosition = values, index = 0, result = Array(region.size) { -1 })

        return if (res == null){
            false
        } else{
            finalizeRegion(positions = positions, values = res)
            true
        }
    }

    private fun finalizeRegion(positions: Array<Int>, values: Array<Int>) {
        currentID++
        for (p in positions.withIndex()) {
            val position = p.value
            boardRegions[position] = currentID
            completedBoard[position] = values[p.index]
            remainingPositions.remove(position)
        }
    }

    private fun tryPopulateRegion(possibleValuesPerPosition: Array<List<Int>>, index: Int, result: Array<Int>): Array<Int>? {
        //Return condition
        if (possibleValuesPerPosition.isEmpty()) return result

        //Check first position
        val possibleValues = possibleValuesPerPosition.first()
        if (possibleValues.isEmpty()) return null //backtrack

        for (value in possibleValues.shuffled(random)) {
            result[index] = value
            val newPossibleValues = Array(possibleValuesPerPosition.size - 1) { it ->
                possibleValuesPerPosition[it+1].filter { it != value }
            }
            val res = tryPopulateRegion(newPossibleValues, index + 1, result)
            if (res != null) return res
        }

        // Populate failed
        return null
    }

    // If two fields in a row or column contain the same number Z, there must be at least Z fields with different numbers between these two fields.
    private fun checkRule3(value: Int, position: Int, actualValues: IntArray): Boolean {
        val errorNotFound = Direction.entries.all { direction: Direction ->
            val errorNotFoundInDirection = (1..value)
                .mapNotNull { moveValue: Int ->
                    Coordinate.move(
                        direction = direction,
                        position = position,
                        numRows = numRows,
                        numColumns = numColumns,
                        value = moveValue
                    )
                } //Null values are out of bounds of the board and can be ignored
                .all { otherPosition: Int ->
                    actualValues[otherPosition] != value
                }

            errorNotFoundInDirection
        }
        return errorNotFound
    }

    private fun propagateOnce(region: MutableList<Int>) {
        if (region.size == maxRegionSize()) return

        for (position in region.shuffled(random)) {
            for (direction in Direction.entries.shuffled(random)) {
                val result = tryPropagate(
                    propagation = Coordinate.move(direction = direction, position = position, numColumns=numColumns, numRows = numRows),
                    region = region
                )
                if (result) return
            }
        }

    }

    private fun tryPropagate(propagation: Int?, region: MutableList<Int>): Boolean {
        if (propagation != null && remainingPositions.contains(propagation) && !region.contains(propagation)){
            region.add(propagation)
            return true
        }
        return false
    }

    private fun populatePositions(
        possibleValues: Array<MutableList<Int>>,
        actualValues: IntArray,
        remainingPositions: MutableSet<Int>,
        foundSPT: MutableList<Int> = mutableListOf()
    ): HakyuuScore? {
        val score = HakyuuScore()

        while (remainingPositions.isNotEmpty())
        {
            // If ended populating return if its a correct board
            if (boardPopulated(actualValues) && boardMeetsRules(actualValues)) return score

            val res = populateValues(
                possibleValues = possibleValues,
                actualValues = actualValues,
                remainingPositions = remainingPositions,
                foundSPT = foundSPT,
            ) ?: return null // Found contradiction -> can't populate

            score.add(res)
        }
        return null
    }

    private fun addValueToActualValues(
        values:  MutableList<Int>,
        actualValues: IntArray,
        position: Int,
        remainingPositions: MutableSet<Int>,
        score: HakyuuScore
    ) {
        actualValues[position] = values.first()
        values.clear()
        remainingPositions.remove(position)
        score.addScoreNewValue()
    }

    // Tries to populate values while there is no contradiction
    // Return if there wasnt a contradiction
    private fun populateValues(
        possibleValues: Array<MutableList<Int>>,
        actualValues: IntArray,
        remainingPositions: MutableSet<Int>,
        foundSPT: MutableList<Int>
    ): HakyuuScore? {
        val score = HakyuuScore()

        val tmpRule2 = mutableMapOf<Int, MutableSet<Int>>()
        actualValues.withIndex().filter { (_, value) -> value != 0 }.forEach { (position, value) ->
            val regionID = getRegionId(position)
            if (tmpRule2.containsKey(regionID)) tmpRule2[regionID]!!.add(value)
            else tmpRule2[regionID] = mutableSetOf(value)
        }

        val regions = mutableMapOf<Int, MutableList<Int>>()
        for (position in remainingPositions.toList()) {
            val regionID = getRegionId(position)
            for (value in possibleValues[position].toList()) {
                // Check rule 2
                if (tmpRule2[regionID]?.contains(value) == true) {
                    score.addScoreRule2()
                    val values = possibleValues[position]
                    values.remove(value)

                    if (values.size == 1) addValueToActualValues(values, actualValues, position, remainingPositions, score)
                    else if(values.size == 0)  return null

                    continue
                }

                // Check rule 3
                if (!checkRule3(value = value, position = position, actualValues = actualValues)) {
                    score.addScoreRule3()
                    val values = possibleValues[position]
                    values.remove(value)

                    if (values.size == 1) addValueToActualValues(values, actualValues, position, remainingPositions, score)
                    else if(values.size == 0)  return null

                    continue
                }
            }

            // Populate region
            if (regions.containsKey(regionID)) {
                regions[regionID]!!.add(position)
            } else {
                regions[regionID] = mutableListOf(position)
            }
        }

        // Possible values changed
        if (score.get() > 0) return score
/*
        for (region in regions.values) {
            val positionsPerValue = getPositionsPerValues(region = region, possibleValues = possibleValues)

            val singles = cleanHiddenSingles(positionsPerValue = positionsPerValue, possibleValues = possibleValues, foundSPT = foundSPT)
            foundSPT.addAll(singles)
            score.addScoreHiddenSingle(singles.size)

            var pairs = cleanHiddenPairs(positionsPerValue = positionsPerValue, possibleValues = possibleValues, foundSPT = foundSPT)
            foundSPT.addAll(pairs)
            score.addScoreHiddenPairs(pairs.size/2)

            var triples = cleanHiddenTriples(positionsPerValue = positionsPerValue, possibleValues = possibleValues, foundSPT = foundSPT)
            foundSPT.addAll(triples)
            score.addScoreHiddenTriples(triples.size/3)

            region.removeAll(foundSPT)

            pairs = cleanObviousPairs(region = region, possibleValues = possibleValues)
            foundSPT.addAll(pairs)
            score.addScoreObviousSingle(pairs.size/2)
            region.removeAll(pairs)

            triples = cleanObviousTriples(region = region, possibleValues = possibleValues)
            foundSPT.addAll(triples)
            score.addScoreObviousPairs(triples.size/3)

        }

 */

        // Possible values changed
        if (score.get() > 0) {
            for (position in remainingPositions.toList()) {
                val values = possibleValues[position]
                if (values.size == 1) addValueToActualValues(values, actualValues, position, remainingPositions, score)
                else if(values.size == 0)  return null
            }
            return if (boardMeetsRules(actualValues)) score else null
        }

        // If the possible values didn't change: Brute force a value
        val bruteForceResult = bruteForceAValue(
            possibleValues = possibleValues,
            actualValues = actualValues,
            remainingPositions = remainingPositions,
            foundSPT = foundSPT
        )

        return bruteForceResult?.let {
            // Brute force was successful
            score.addScoreBruteForce()
            score
        }
    }

    //TODO: brute force must return null if it tries all possiblevalues and 2 returns true => null if not unique board
    private fun bruteForceAValue(
        possibleValues: Array<MutableList<Int>>,
        actualValues: IntArray,
        remainingPositions: MutableSet<Int>,
        foundSPT: MutableList<Int>
    ): HakyuuScore? {
        if (remainingPositions.size == 1) return null

        val (position, minPossibleValues) = remainingPositions.map { it to possibleValues[it] }.minBy { (_, values) -> values.size }
        remainingPositions.remove(position)
        val newPossibleValues: Array<MutableList<Int>> = Array(possibleValues.size) {
            val ls = mutableListOf<Int>()
            ls.addAll(possibleValues[it])
            ls
        }

        for(chosenValue in minPossibleValues.toList()) {
            possibleValues[position].removeAt(0)
            newPossibleValues[position].clear()
            val newActualValues = actualValues.clone()
            newActualValues[position] = chosenValue
            val newFoundSPT = foundSPT.toMutableList()

            val result = populatePositions(
                possibleValues = newPossibleValues,
                actualValues = newActualValues,
                remainingPositions = remainingPositions,
                foundSPT = newFoundSPT,
            )

            if (result != null) {
                //newPossibleValues/newActualValues are invalid now!
                Utils.replaceArray(thisArray = possibleValues, with = newPossibleValues)
                Utils.replaceArray(thisArray = actualValues, with = newActualValues)
                foundSPT.clear()
                foundSPT.addAll(newFoundSPT)

                return result
            }
        }
        // If brute force didn't solve the board this is an invalid state
        return null
    }

    internal fun cleanObviousPairs(region: List<Int>, possibleValues: Array<MutableList<Int>>): List<Int> {
        val filteredRegions = region.filter { position -> possibleValues[position].size == 2 }

        val res = mutableListOf<Int>()
        filteredRegions.forEachIndexed { index, position1 ->
            val position2 = filteredRegions.drop(index + 1).find { position2 -> possibleValues[position2] == possibleValues[position1] }
            if (position2 != null) {
                res.add(position1)
                res.add(position2)
                region.filter { position -> position!=position1 && position!=position2 }
                    .forEach { coordinate ->
                        possibleValues[coordinate].remove(position1)
                        possibleValues[coordinate].remove(position2)
                    }
            }
        }
        return res
    }

    internal fun cleanObviousTriples(region: List<Int>, possibleValues: Array<MutableList<Int>>): List<Int> {
        // Obvious triples can only have size 2 or 3
        val filteredRegions = region.filter { coordinate ->
            (possibleValues[coordinate].size == 2 || possibleValues[coordinate].size == 3)
        }

        val res = mutableListOf<Int>()
        filteredRegions.forEachIndexed { index, position1 ->
            // Substract possible values: {coord2 values} - {position1 values}
            val union = filteredRegions.drop(index + 1).map { position2 ->
                Pair(position2, possibleValues[position2].union(possibleValues[position1]))
            }
            // Find two coordinates whose substracted possible values are the same
            val otherTriples = union.filter { other ->
                union.any { it.first != other.first && it.second == other.second && (it.second.size == 3 || it.second.size == 2) }
            }
            // If it was found add it
            if (otherTriples.size == 2) {
                val position2 = otherTriples[0].first
                val position3 = otherTriples[1].first
                res.add(position1)
                res.add(position2)
                res.add(position3)

                region.filter { position -> position!=position1 && position!=position2 && position!=position3 }
                    .forEach { coordinate ->
                        possibleValues[coordinate].remove(position1)
                        possibleValues[coordinate].remove(position2)
                        possibleValues[coordinate].remove(position3)
                    }
            }
        }
        return res
    }

    internal fun getPositionsPerValues(region: List<Int>, possibleValues: Array<MutableList<Int>>): Array<MutableList<Int>> {
        val maxValue = region.maxOf { possibleValues[it].maxOrNull() ?: 0 }
        val positionsPerValue = Array(maxValue) { mutableListOf<Int>() }
        region.forEach { position ->
            possibleValues[position].forEach { value -> positionsPerValue[value - 1].add(position) }
        }
        return positionsPerValue
    }

    // Delete values to reveal hidden singles
    internal fun cleanHiddenSingles(possibleValues: Array<MutableList<Int>>, positionsPerValue: Array<MutableList<Int>>, foundSPT: MutableList<Int> = mutableListOf()): List<Int> {
        val res = mutableListOf<Int>()
        positionsPerValue.withIndex().filter { (_,positions) -> positions.size == 1 && !foundSPT.contains(positions.first())}
            .forEach { (value, positions) ->
                // Remove all the possible values but the hidden single
                val position = positions.first()
                val positionPossibleValues = possibleValues[position]
                positionPossibleValues.clear()
                positionPossibleValues.add(value + 1)

                res.add(position)
            }
        return res
    }

    // Delete values to reveal hidden pairs
    internal fun cleanHiddenPairs(possibleValues: Array<MutableList<Int>>, positionsPerValue: Array<MutableList<Int>>, foundSPT: MutableList<Int> = mutableListOf()): List<Int> {
        val filteredPossiblePairs = positionsPerValue.withIndex()
            .filter { (_, positions) -> positions.size == 2 && !positions.any { position -> foundSPT.contains(position) } }

        val res = mutableListOf<Int>()
        var drop = 0
        filteredPossiblePairs.forEach { (index1, value) ->
            drop++
            val otherPair = filteredPossiblePairs.drop(drop).find { (_, value2) ->
                value2 == value
            }

            if (otherPair != null) { //index1, index2 are pairs
                val index2 = otherPair.index
                // Remove from each coordinate the possible values that are not the hidden pairs
                value.forEach {coordinate ->
                    possibleValues[coordinate].removeIf { it != index1+1 && it != index2+1 }
                    res.add(coordinate)
                }
            }
        }
        return res
    }

    // Delete values to reveal hidden triples
    internal fun cleanHiddenTriples(possibleValues: Array<MutableList<Int>>, positionsPerValue: Array<MutableList<Int>>, foundSPT: MutableList<Int> = mutableListOf()): List<Int> {
        val filteredPossibleTriples = positionsPerValue.withIndex().filter { it.value.size == 2 || it.value.size == 3 }
            .filter { (_, positions) -> (positions.size == 2 || positions.size == 3) && !positions.any { position -> foundSPT.contains(position) } }

        val res = mutableListOf<Int>()
        var drop = 0
        filteredPossibleTriples.forEach { (index1, value) ->
            drop++
            val union = filteredPossibleTriples.drop(drop).map { (i, v) ->
                IndexedValue(index = i, value = v.union(value))
            }.filter { it.value.size==3 }

            val otherTriples = union.filter { other ->
                union.any { it.value == other.value && it.index != other.index }
            }

            if (otherTriples.size == 2) { //index1, index2 and index3 are triples
                val index2 = otherTriples[0].index
                val index3 = otherTriples[1].index
                // Remove from each coordinate the possible values that are not the hidden triples
                otherTriples[0].value.forEach { coordinate ->
                    possibleValues[coordinate].removeIf { it != index1+1 && it != index2+1 && it != index3+1 }
                    res.add(coordinate)
                }
            }
        }
        return res
    }

    companion object {
        fun create(numRows: Int, numColumns: Int, seed: Long, difficulty: Difficulty): Hakyuu {
            val hakyuu = Hakyuu(
                numRows = numRows,
                numColumns = numColumns,
                seed = seed,
            )

            hakyuu.createGame(difficulty)

            return hakyuu
        }

        fun create(numRows: Int, numColumns: Int, seed: Long, startBoard: String, completedBoard: String, boardRegions: String, reverse: Boolean = false): Hakyuu {
            val start = Hakyuu.parseBoardString(startBoard)
            val completed = Hakyuu.parseBoardString(completedBoard)
            val regions = Hakyuu.parseRegionString(boardRegions, reverse)

            require(start.size == completed.size && start.size == regions.size && start.size == numRows*numColumns) { "Incompatible sizes provided to Hakyuu" }

            val hakyuu = Hakyuu(
                numRows = numRows,
                numColumns = numColumns,
                seed = seed,
                startBoard = start,
                completedBoard = completed,
                regions = regions
            )

            return hakyuu
        }

        fun solveBoard(seed: Long, boardToSolve: String, boardRegions: String, reverseCoordinates: Boolean = false): Hakyuu {
            val start = Hakyuu.parseBoardString(boardToSolve)
            val regions = Hakyuu.parseRegionString(boardRegions, reverseCoordinates)
            val completed = Hakyuu.parseBoardString(boardToSolve)
            val numRows = boardToSolve.count { it == '\n' } + 1
            val numColumns = (boardToSolve.substringBefore(delimiter = "\n").length + 1) / 2

            require(start.size == regions.size) { "Incompatible sizes provided to Hakyuu" }

            val hakyuu = Hakyuu(
                numRows = numRows,
                numColumns = numColumns,
                seed = seed,
                regions = regions,
                startBoard = start,
                completedBoard = completed
            )

            val score = hakyuu.solveBoard(hakyuu.completedBoard)
            hakyuu.score.add(score?.get()?:0)

            return hakyuu
        }

        fun parseBoardString(str: String): IntArray {
            return str.replace('\n',' ').split(" ").map { if (it=="-") 0 else it.toInt() }.toIntArray()
        }

        fun parseRegionString(str: String, reverseCoordinates: Boolean): IntArray {
            val map = mutableMapOf<Coordinate, Int>()
            val lines = str.replace("[","").replace("]","").split('\n')

            var maxCoordinate = Coordinate(0,0)
            for (line in lines) {
                val spl = line.split(':')
                val coords = spl[1].split(", ").map { Coordinate.parseString(it, reverseCoordinates) }
                val regionId = spl[0].toInt()
                coords.forEach { coordinate ->
                    if (coordinate > maxCoordinate) maxCoordinate = coordinate
                    map[coordinate] = regionId
                }
            }

            val numColumns = maxCoordinate.column + 1
            val numRows = maxCoordinate.row + 1

            val res = IntArray(size = numColumns * numRows) {
                val coord = Coordinate.fromIndex(index = it, numRows = numRows, numColumns = numColumns)
                map[coord]!!
            }

            return res
        }
    }
}

