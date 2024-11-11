package com.example.tfg.games.hakyuu

import androidx.room.Entity
import androidx.room.Ignore
import com.example.tfg.common.IdGenerator
import com.example.tfg.games.common.Difficulty
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.utils.Curves
import com.example.tfg.common.enums.Direction
import com.example.tfg.common.utils.Utils
import com.example.tfg.games.common.AbstractGame
import com.example.tfg.games.common.BoardData
import com.example.tfg.games.common.CommonStrategies
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.PopulateResult
import com.example.tfg.games.common.Score
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.coroutineContext

@Entity
class Hakyuu @JvmOverloads constructor(
    id: Long = IdGenerator.generateId("hakyuuGame"),
    numColumns: Int,
    numRows: Int,
    seed: Long,
    score: Score = HakyuuScore(),
    completedBoard: IntArray = IntArray(numColumns * numRows),
    startBoard: IntArray = IntArray(numColumns * numRows),
    boardRegions: IntArray = IntArray(numColumns * numRows),
): AbstractGame(
    id = id,
    type = Games.HAKYUU,
    numColumns = numColumns,
    numRows = numRows,
    seed = seed,
    score = score,
    completedBoard = completedBoard,
    startBoard = startBoard,
    boardRegions = boardRegions
) {

    @delegate:Ignore
    @get:Ignore
    private val strategies by lazy { CommonStrategies(this) }

    // Helper variables
    @Ignore
    private var currentID = 0

    override fun createCompleteBoard(remainingPositions: MutableSet<Int>) {
        while (remainingPositions.isNotEmpty()) {
            propagateRandomRegion(remainingPositions)
        }
    }

    override fun fillPossibleValues(possibleValues: Array<MutableList<Int>>, board: IntArray): Score {
        val scoreResult = HakyuuScore()
        for (position in (0..<numPositions())) {
            val size = getRegionSize(getRegionId(position))
            if (size == 1 && board[position] == 0) {
                board[position] = 1
                scoreResult.addScoreNewValue()
            }
            else if (board[position] == 0) possibleValues[position].addAll(1.. size)
        }
        return scoreResult
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
        positions.filter { pos -> pos != position && actualValues[pos] == value }
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

    private fun propagateRandomRegion(
        remainingPositions: MutableSet<Int>,
        numPropagations: Int = randomPropagationNumber(),
        iterations: Int = 1
    ) {
        val seed = remainingPositions.random(random)
        val region = mutableListOf(seed)

        for (index in 1..numPropagations) {
            propagateOnce(remainingPositions = remainingPositions, region = region)
        }

        val result = populateRegion(remainingPositions = remainingPositions, region = region)

        if (!result) {
            if (iterations > 10) {
                modifyNeighbouringRegions(remainingPositions = remainingPositions, seed = seed)
                propagateRandomRegion(remainingPositions = remainingPositions, iterations = 5)
            }
            else {
                propagateRandomRegion(remainingPositions = remainingPositions, iterations = iterations+1)
            }
        }
    }

    private fun randomPropagationNumber(): Int {
        return ((maxRegionSize() - 1) * Curves.easierInOutSine(random.nextDouble(1.0))).toInt() + 1
    }

    private fun modifyNeighbouringRegions(remainingPositions: MutableSet<Int>, seed: Int) {
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

    private fun populateRegion(remainingPositions: MutableSet<Int>, region: List<Int>): Boolean {
        val possibleValuesPerPosition = region.map { position ->
            position to (1..region.size)
                .filter { value -> checkRule3(value = value, position = position, actualValues = completedBoard) }
                .toMutableList()
        }.sortedBy { it.second.size }.toMap()

        val (valuesPerPosition, result) = assignValues(possibleValuesPerPosition = possibleValuesPerPosition)

        if (result) finalizeRegion(remainingPositions = remainingPositions, valuesPerPosition = valuesPerPosition)

        return result
    }

    private fun finalizeRegion(remainingPositions: MutableSet<Int>, valuesPerPosition: Map<Int, Int>) {
        currentID++
        for ((position, value) in valuesPerPosition.entries) {
            boardRegions[position] = currentID
            completedBoard[position] = value
            remainingPositions.remove(position)
        }
    }

    fun assignValues(possibleValuesPerPosition: Map<Int, List<Int>>): Pair<Map<Int, Int>, Boolean> {
        val positions = possibleValuesPerPosition.keys.toList()
        val assignedValues = mutableMapOf<Int, Int>()

        fun backtrack(index: Int): Boolean {
            // End condition
            if (index == positions.size) return true

            val position = positions[index]

            for (value in possibleValuesPerPosition[position]!!) {
                if (value in assignedValues.values) continue

                assignedValues[position] = value

                if (backtrack(index + 1)) return true

                assignedValues.remove(position)
            }

            // Coudn't populate value
            return false
        }

        return if (backtrack(0)) assignedValues to true
            else assignedValues to false
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

    private fun propagateOnce(remainingPositions: MutableSet<Int>, region: MutableList<Int>) {
        if (region.size == maxRegionSize()) return

        for (position in region.shuffled(random)) {
            for (direction in Direction.entries.shuffled(random)) {
                val propagation = Coordinate.move(direction = direction, position = position, numColumns=numColumns, numRows = numRows)

                if (propagation != null && remainingPositions.contains(propagation) && !region.contains(propagation)){
                    region.add(propagation)
                    return
                }
            }
        }
    }

    // Tries to populate values while there is no contradiction
    // Return if there wasnt a contradiction
    override fun populateValues(boardData: BoardData): PopulateResult {
        val score = HakyuuScore()

        val possibleValues = boardData.possibleValues
        val actualValues = boardData.actualValues

        val filteredRegions = mutableMapOf<Int, MutableList<Int>>()
        val remainingRegions = mutableMapOf<Int, MutableList<Int>>()
        getPositions().forEach { position ->
            val regionID = getRegionId(position)
            if (actualValues[position] != 0) Utils.addToMapList(regionID, position, filteredRegions)
            else Utils.addToMapList(regionID, position, remainingRegions)
        }

        for (position in getRemainingPositions(actualValues)) {
            val regionID = getRegionId(position)
            val possibleValuesInPosition = possibleValues[position]
            val region = filteredRegions[regionID]
            val valuesInRegion = region?.map { actualValues[it] }

            possibleValuesInPosition.removeIf { value ->
                var res = false
                // Check rule 2: In each region there must be numbers 1 to size of region
                if (valuesInRegion != null && valuesInRegion.contains(value)) {
                    score.addScoreRule2()
                    res = true
                }
                // Check rule 3: Between each number K there must be K positions that are not K
                else if (!checkRule3(value = value, position = position, actualValues = actualValues)) {
                    score.addScoreRule3()
                    res = true
                }
                res
            }

            if (possibleValuesInPosition.size == 1) {
                addValueToActualValues(possibleValuesInPosition, actualValues, position, score)
                Utils.addToMapList(regionID, position, filteredRegions)
                Utils.removeFromMapList(regionID, position, remainingRegions)
            }
            else if(possibleValuesInPosition.size == 0) return PopulateResult.contradiction()
        }

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)

        for (region in remainingRegions.values) {
            val regionPossibleValues = region.map { possibleValues[it] }.toTypedArray()
            val numPairs = strategies.cleanNakedPairsInLine(regionPossibleValues)
            score.addNakedPairs(numPairs)

            val numTriples = strategies.cleanNakedTriplesInLine(regionPossibleValues)
            score.addNakedTriples(numTriples)

            val numSPT = strategies.cleanHiddenSinglesPairsTriplesInline(regionPossibleValues, maxValueSize())
            score.addHiddenSPT(numSPT)
        }

        // Possible values changed
        if (score.get() > 0) {
            for (position in getRemainingPositions(actualValues)) {
                val values = possibleValues[position]
                if (values.size == 1) addValueToActualValues(values, actualValues, position, score)
                else if(values.size == 0) return PopulateResult.contradiction()
            }
            return if (boardMeetsRules(actualValues)) PopulateResult.success(score)
                else PopulateResult.contradiction()
        }

        return PopulateResult.noChangesFound()
    }


    companion object {

        suspend fun create(numRows: Int, numColumns: Int, seed: Long, difficulty: Difficulty): Hakyuu? {
            val hakyuu = Hakyuu(
                numRows = numRows,
                numColumns = numColumns,
                seed = seed
            )

            hakyuu.createGame(difficulty)

            return if (coroutineContext.isActive) hakyuu else null
        }

        // For testing
        suspend fun createTesting(numRows: Int, numColumns: Int, seed: Long, difficulty: Difficulty): Hakyuu {
            val hakyuu = Hakyuu(
                id = 0,
                numRows = numRows,
                numColumns = numColumns,
                seed = seed
            )

            hakyuu.createGame(difficulty)

            return hakyuu
        }

        fun createTesting(
            seed: Long,
            numColumns: Int,
            numRows: Int,
            startBoard: IntArray,
            completedBoard: IntArray,
            boardRegions: IntArray
        ): Hakyuu {
            return Hakyuu(
                seed = seed,
                score = HakyuuScore(500),
                numColumns = numColumns,
                numRows = numRows,
                startBoard = startBoard,
                completedBoard = completedBoard,
                boardRegions = boardRegions
            )
        }

        // For testing
        fun createTesting(numRows: Int, numColumns: Int, seed: Long, startBoard: String, completedBoard: String, boardRegions: String, reverse: Boolean = false): Hakyuu {
            val start = Hakyuu.parseBoardString(startBoard)
            val completed = Hakyuu.parseBoardString(completedBoard)
            val regions = Hakyuu.parseRegionString(boardRegions, reverse)

            require(start.size == completed.size && start.size == regions.size && start.size == numRows*numColumns) { "Incompatible sizes provided to Hakyuu" }

            val hakyuu = Hakyuu(
                id = 0,
                numRows = numRows,
                numColumns = numColumns,
                seed = seed,
                startBoard = start,
                completedBoard = completed,
                boardRegions = regions
            )

            return hakyuu
        }
        // For testing
        fun solveBoard(
            seed: Long,
            numColumns: Int,
            numRows: Int,
            startBoard: IntArray,
            completedBoard: IntArray,
            regions: IntArray
        ): Hakyuu {
            val hakyuu = Hakyuu(
                id = 0,
                numColumns = numColumns,
                numRows = numRows,
                seed = seed,
                boardRegions = regions,
                startBoard = startBoard,
                completedBoard = completedBoard
            )

            val score = runBlocking { hakyuu.solveBoard(hakyuu.startBoard) }
            hakyuu.score.add(score)

            return hakyuu
        }

        // For testing
        fun solveBoard(seed: Long, boardToSolve: String, boardRegions: String, reverseCoordinates: Boolean = false): Hakyuu {
            val start = Hakyuu.parseBoardString(boardToSolve)
            val regions = Hakyuu.parseRegionString(boardRegions, reverseCoordinates)
            val completed = Hakyuu.parseBoardString(boardToSolve)
            val numRows = boardToSolve.count { it == '\n' } + 1
            val numColumns = (boardToSolve.substringBefore(delimiter = "\n").length + 1) / 2

            require(start.size == regions.size) { "Incompatible sizes provided to Hakyuu" }

            val hakyuu = Hakyuu(
                id = 0,
                numRows = numRows,
                numColumns = numColumns,
                seed = seed,
                boardRegions = regions,
                startBoard = start,
                completedBoard = completed
            )

            val score = runBlocking { hakyuu.solveBoard(hakyuu.completedBoard) }
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

