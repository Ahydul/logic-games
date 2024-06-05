package com.example.tfg.games.hakyuu

import com.example.tfg.common.Difficulty
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.utils.Curves
import com.example.tfg.common.utils.Direction
import com.example.tfg.common.utils.Utils
import com.example.tfg.games.GameType
import com.example.tfg.games.Games
import com.example.tfg.games.Score

const val TIMEOUT_SOLVER = 500L
const val TIMEOUT = 500L

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
    private val helperRemainingPositions: MutableSet<Int> = initRemainingPositionsHelper()
    private var currentID = 0
    private var maxAmmountOfBruteForces = 20
    private var startTime = 0L

    private fun initRemainingPositionsHelper():  MutableSet<Int> {
        return getPositions().toMutableSet()
    }

    private fun getPositions(): IntRange {
        return (0..< numPositions())
    }

    private fun getRemainingPositions(actualValues: IntArray): List<Int> {
        return getPositions().filter { actualValues[it] == 0 }
    }

    override fun createGame(difficulty: Difficulty) {
        do {
            val noTimedout = createGame2(difficulty)
        } while (!noTimedout)
    }

    fun createGame2(difficulty: Difficulty): Boolean {
        // Create completedBoard

        while (!boardCreated()) {
            startTime = System.currentTimeMillis()
            propagateRandomRegion()
            if (System.currentTimeMillis() - startTime > TIMEOUT) {
                println("TIMEOUT creating regions")
                return false
            }
        }

        // Create startBoard

        val remainingPositions = mutableSetOf<Int>()
        startBoard.indices.forEach {
            startBoard[it] = completedBoard[it]
            remainingPositions.add(it) // Helper variable
        }

        maxAmmountOfBruteForces = HakyuuScore.getMaxBruteForceValue(difficulty)
        var actualScore: Score? = null

        while (!remainingPositions.isEmpty()) {
            // Remove random value from startBoard
            val randomPosition = remainingPositions.random(random)
            remainingPositions.remove(randomPosition)
            startBoard[randomPosition] = 0

            val tmpBoard = startBoard.clone()

            val res = solveBoard(tmpBoard)

            if (res == null || res.isTooHighForDifficulty(difficulty)) {
                if (System.currentTimeMillis() - startTime > TIMEOUT_SOLVER) println("TIMEOUT solving board")
                // Add the value back
                startBoard[randomPosition] = completedBoard[randomPosition]
            }
            else if (!res.isTooHighForDifficulty(difficulty)){
                actualScore = res
                if (res.isTooLowForDifficulty(difficulty)) continue
            }
        }

        score.add(actualScore)

        return true
    }

    override fun solveBoard(board: IntArray): Score? {
        startTime = System.currentTimeMillis()

        val possibleValues = Array(numPositions()) { mutableListOf<Int>() }
        val score = HakyuuScore()
        for (position in (0..<numPositions())) {
            val size = getRegionSize(getRegionId(position))
            if (size == 1 && board[position] == 0) {
                board[position] = 1
                score.addScoreNewValue()
            }
            else if (board[position] == 0) possibleValues[position].addAll(1.. size)
        }

        val res = populatePositions(possibleValues = possibleValues, actualValues = board)
        if (res != null) res.add(score)

        return res
    }

    override fun boardMeetsRulesStr(board: IntArray): String {
        val tmp = mutableMapOf<Int, MutableSet<Int>>()

        board.withIndex().filter { (_, value) -> value != 0 }.forEach { (position, value) ->
            val regionID = getRegionId(position)
            if (!checkRule3(value = value, position = position, actualValues = board)) {
                return "Rule 3 failed: with value:$value, position:$position"
            }
            if (tmp.containsKey(regionID)) {
                val elementAdded = tmp[regionID]!!.add(value)
                if (!elementAdded) {
                    return "Rule 2 failed: with region:${tmp[regionID]}"
                }
            }
            else{
                tmp[regionID] = mutableSetOf(value)
            }
        }
        return ""
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
        return helperRemainingPositions.isEmpty()
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
        return helperRemainingPositions.random(random)
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
            helperRemainingPositions.addAll(region)
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
            helperRemainingPositions.remove(position)
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
            val errorFoundInDirection = (1..value)
                .mapNotNull { moveValue: Int ->
                    Coordinate.move(
                        direction = direction,
                        position = position,
                        numRows = numRows,
                        numColumns = numColumns,
                        value = moveValue
                    )
                } //Null values are out of bounds of the board and can be ignored
                .any { otherPosition: Int ->
                    actualValues[otherPosition] == value
                }

            !errorFoundInDirection
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
        if (propagation != null && helperRemainingPositions.contains(propagation) && !region.contains(propagation)){
            region.add(propagation)
            return true
        }
        return false
    }

    private fun populatePositions(
        possibleValues: Array<MutableList<Int>>,
        actualValues: IntArray,
        foundSPT: MutableList<Int> = mutableListOf(),
        ammountOfBruteForces: Int = 0
    ): HakyuuScore? {
        val score = HakyuuScore()

        while (getRemainingPositions(actualValues).isNotEmpty())
        {
            if (System.currentTimeMillis() - startTime > TIMEOUT_SOLVER)   return null

            val res = populateValues(
                possibleValues = possibleValues,
                actualValues = actualValues,
                foundSPT = foundSPT,
                ammountOfBruteForces = ammountOfBruteForces
            ) ?: return null // Found contradiction -> can't populate

            score.add(res)
        }
        return if (boardMeetsRules(actualValues)) {
            score
        } else {
            null
        }
    }

    private fun addValueToActualValues(
        values:  MutableList<Int>,
        actualValues: IntArray,
        position: Int,
        score: HakyuuScore
    ) {
        actualValues[position] = values.first()
        values.clear()
        score.addScoreNewValue()
    }

    // Tries to populate values while there is no contradiction
    // Return if there wasnt a contradiction
    private fun populateValues(
        possibleValues: Array<MutableList<Int>>,
        actualValues: IntArray,
        foundSPT: MutableList<Int>,
        ammountOfBruteForces: Int
    ): HakyuuScore? {
        val score = HakyuuScore()

        val addToRegions = { regionID: Int, position: Int, regions: MutableMap<Int, MutableList<Int>> ->
            if (regions.containsKey(regionID)) regions[regionID]!!.add(position)
            else regions[regionID] = mutableListOf(position)
        }

        val removeFromRegions = { regionID: Int, position: Int, regions: MutableMap<Int, MutableList<Int>> ->
            if (regions.containsKey(regionID)) regions[regionID]!!.remove(position)
        }

        val filteredRegions = mutableMapOf<Int, MutableList<Int>>()
        val remainingRegions = mutableMapOf<Int, MutableList<Int>>()
        getPositions().forEach { position ->
            val regionID = getRegionId(position)
            if (actualValues[position] != 0) addToRegions(regionID, position, filteredRegions)
            else addToRegions(regionID, position, remainingRegions)
        }

        for (position in getRemainingPositions(actualValues)) {
            val regionID = getRegionId(position)
            val possibleValuesInPosition = possibleValues[position]
            val region = filteredRegions[regionID]
            val valuesInRegion = region?.map { actualValues[it] }

            possibleValuesInPosition.removeIf { value ->
                var res = false
                // Check rule 2
                if (valuesInRegion!=null && valuesInRegion.contains(value)) {
                    score.addScoreRule2()
                    res = true
                }
                // Check rule 3
                else if (!checkRule3(value = value, position = position, actualValues = actualValues)) {
                    score.addScoreRule3()
                    res = true
                }
                res
            }

            if (possibleValuesInPosition.size == 1) {
                addValueToActualValues(possibleValuesInPosition, actualValues, position, score)
                addToRegions(regionID, position, filteredRegions)
                removeFromRegions(regionID, position, remainingRegions)
            }
            else if(possibleValuesInPosition.size == 0) return null
        }

        // Possible values changed
        if (score.get() > 0) return score

        for (region in remainingRegions.values) {
            val positionsPerValue = getPositionsPerValues(region = region, possibleValues = possibleValues)

            //Everytime a SPT is found AND the possible values change the score is properly changed.
            val singles = cleanHiddenSingles(positionsPerValue = positionsPerValue, possibleValues = possibleValues, foundSPT = foundSPT)
            score.addScoreHiddenSingle(singles.size)

            var pairs = cleanHiddenPairs(positionsPerValue = positionsPerValue, possibleValues = possibleValues, foundSPT = foundSPT)
            score.addScoreHiddenPairs(pairs.size/2)

            var triples = cleanHiddenTriples(positionsPerValue = positionsPerValue, possibleValues = possibleValues, foundSPT = foundSPT)
            score.addScoreHiddenTriples(triples.size/3)

            pairs = cleanObviousPairs(region = region, possibleValues = possibleValues, foundSPT = foundSPT)
            score.addScoreObviousPairs(pairs.size/2)

            triples = cleanObviousTriples(region = region, possibleValues = possibleValues, foundSPT = foundSPT)
            score.addScoreObviousTriples(triples.size/3)
        }

        // Possible values changed
        if (score.get() > 0) {
            for (position in getRemainingPositions(actualValues)) {
                val values = possibleValues[position]
                if (values.size == 1) addValueToActualValues(values, actualValues, position, score)
                else if(values.size == 0)  return null
            }
            return if (boardMeetsRules(actualValues)) score else {
                null
            }
        }

        // If the possible values didn't change: Brute force a value
        val bruteForceResult = bruteForce(
            possibleValues = possibleValues,
            actualValues = actualValues,
            foundSPT = foundSPT,
            ammountOfBruteForces = ammountOfBruteForces
        )

        return bruteForceResult?.let {
            // Brute force was successful
            score.add(bruteForceResult)
            score
        }
    }

    private fun bruteForce(
        possibleValues: Array<MutableList<Int>>,
        actualValues: IntArray,
        foundSPT: MutableList<Int>,
        ammountOfBruteForces: Int
    ): HakyuuScore? {
        if (ammountOfBruteForces > maxAmmountOfBruteForces) return null

        val (position, minPossibleValues) = getRemainingPositions(actualValues)
            .map { it to possibleValues[it] }
            .minBy { (_, values) -> values.size }

        val results = mutableListOf<BruteForceResult>()
        for(chosenValue in minPossibleValues.toList()) {
            val result = bruteForceAValue(chosenValue, position, possibleValues, actualValues, foundSPT,ammountOfBruteForces)
            if (result!= null) results.add(result)
        }

        if (results.size == 1) {
            val (newPossibleValues, newActualValues, newFoundSPT, score) = results.first()
            Utils.replaceArray(thisArray = possibleValues, with = newPossibleValues)
            Utils.replaceArray(thisArray = actualValues, with = newActualValues)
            foundSPT.clear()
            foundSPT.addAll(newFoundSPT)

            score.addScoreBruteForce()
            return score
        }
        //if (results.size > 1) println("Not a unique board")

        //If results > 2 -> Not a unique board
        //If results == 0 -> Brute force didn't solve the board
        return null
    }

    private fun bruteForceAValue(
        chosenValue: Int,
        position: Int,
        possibleValues: Array<MutableList<Int>>,
        actualValues: IntArray,
        foundSPT: MutableList<Int>,
        ammountOfBruteForces: Int
    ): BruteForceResult? {
        val newPossibleValues: Array<MutableList<Int>> = Array(possibleValues.size) {
            val ls = mutableListOf<Int>()
            ls.addAll(possibleValues[it])
            ls
        }
        possibleValues[position].removeAt(0)
        newPossibleValues[position].clear()

        val newActualValues = actualValues.clone()
        newActualValues[position] = chosenValue

        val newFoundSPT = foundSPT.toMutableList()

        val result = populatePositions(
            possibleValues = newPossibleValues,
            actualValues = newActualValues,
            foundSPT = newFoundSPT,
            ammountOfBruteForces = ammountOfBruteForces + 1
        )
        return if (result == null) null
            else BruteForceResult(newPossibleValues, newActualValues, newFoundSPT, result)
    }

    internal fun cleanObviousPairs(region: List<Int>, possibleValues: Array<MutableList<Int>>, foundSPT: MutableList<Int> = mutableListOf()): List<Int> {
        val filteredRegions = region.filter { position -> possibleValues[position].size == 2 && !foundSPT.contains(position) }

        val res = mutableListOf<Int>()
        filteredRegions.forEachIndexed { index, position1 ->
            val position2 = filteredRegions.drop(index + 1)
                .find { position2 -> possibleValues[position2] == possibleValues[position1] }
            if (position2 != null) {
                foundSPT.add(position1)
                foundSPT.add(position2)

                var possibleValuesChanged = false
                val values = possibleValues[position1]
                region.filter { position -> position!=position1 && position!=position2 }
                    .forEach { position ->
                        possibleValuesChanged = possibleValuesChanged || possibleValues[position].removeAll(values)
                    }

                if (possibleValuesChanged){
                    res.add(position1)
                    res.add(position2)
                }
            }
        }
        return res
    }

    internal fun cleanObviousTriples(region: List<Int>, possibleValues: Array<MutableList<Int>>, foundSPT: MutableList<Int> = mutableListOf()): List<Int> {
        // Obvious triples can only have size 2 or 3
        val filteredRegions = region.filter { position ->
            ((possibleValues[position].size == 2 || possibleValues[position].size == 3) && !foundSPT.contains(position))
        }

        val res = mutableListOf<Int>()
        filteredRegions.forEachIndexed { index, position1 ->
            val position1PossibleValues = possibleValues[position1]
            // Get pairs (position2, possible values of position2 and position1)
            val union = filteredRegions.drop(index + 1).map { position2 ->
                Pair(position2, possibleValues[position2].union(position1PossibleValues))
            }
            val otherTriples = union.filter { other ->
                // Different position, same content and sizes 3 or 2 means its a triple
                union.any { it.first != other.first && it.second == other.second && (it.second.size == 3 || it.second.size == 2) }
            }

            if (otherTriples.size == 2) {
                val position2 = otherTriples[0].first
                val position3 = otherTriples[1].first

                foundSPT.add(position1)
                foundSPT.add(position2)
                foundSPT.add(position3)

                var possibleValuesChanged = false
                val values = possibleValues[position1].union(possibleValues[position2]).union(possibleValues[position3])
                region.filter { position -> position!=position1 && position!=position2 && position!=position3 }
                    .forEach { position ->
                        possibleValuesChanged = possibleValuesChanged || possibleValues[position].removeAll(values)
                    }

                if (possibleValuesChanged){
                    res.add(position1)
                    res.add(position2)
                    res.add(position3)
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
                val possibleValuesChanged = possibleValues[position].removeIf { it != value + 1 }
                if (possibleValuesChanged) res.add(position)
                foundSPT.add(position)
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
                // Remove from each position the possible values that are not the hidden pairs
                value.forEach {position ->
                    val possibleValuesChanged = possibleValues[position]
                        .removeIf { it != index1+1 && it != index2+1 }
                    if (possibleValuesChanged) res.add(position)
                    foundSPT.add(position)
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
                // Remove from each position the possible values that are not the hidden triples
                otherTriples[0].value.forEach { position ->
                    val possibleValuesChanged = possibleValues[position]
                        .removeIf { it != index1+1 && it != index2+1 && it != index3+1 }
                    if (possibleValuesChanged) res.add(position)
                    foundSPT.add(position)
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
            hakyuu.score.add(score)

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

