package com.example.tfg.games.common

import com.example.tfg.common.utils.Coordinate

class Tmp(private val base: AbstractGame) {

    internal fun cleanHiddenSingles(line: Array<MutableList<Int>>, size: Int): Int {
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

    internal fun cleanHiddenSinglesPairsTriplesInline(line: Array<MutableList<Int>>, size: Int): IntArray {
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


    /*
* Returns a map of values that have locked numbers.
* The inside map represents the line (row or column) 2 positions that are the locked numbers.
* */
    private fun getLockedNumbersInLine(
        possibleValues: Array<MutableList<Int>>,
        size: Int,
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

    internal fun getLockedNumbersInColumn(possibleValues: Array<MutableList<Int>>) = getLockedNumbersInLine(
        possibleValues = possibleValues,
        size = base.numRows,
        line = { columnIndex -> base.getColumnPositions(columnIndex) }
    )

    internal fun getLockedNumbersInRow(possibleValues: Array<MutableList<Int>>) = getLockedNumbersInLine(
        possibleValues = possibleValues,
        size = base.numColumns,
        line = { rowIndex -> base.getRowPositions(rowIndex) }
    )

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

    internal fun cleanColoring(
        possibleValues: Array<MutableList<Int>>,
        numColumns: Int,
        lockedNumbersPerRow: MutableMap<Int, MutableMap<Int, Pair<Int, Int>>> = getLockedNumbersInRow(possibleValues = possibleValues),
        lockedNumbersPerColumn: MutableMap<Int, MutableMap<Int, Pair<Int, Int>>> = getLockedNumbersInColumn(possibleValues = possibleValues)
    ): Int {
        data class TmpMaps(val rowMap: MutableMap<Int, MutableList<Int>> = mutableMapOf(), val columnMap: MutableMap<Int, MutableList<Int>> = mutableMapOf()) {
            fun addToRowMap(coordinate: Coordinate) {
                rowMap.computeIfAbsent(coordinate.row){ mutableListOf() }.add(coordinate.toIndex(numColumns))
            }
            fun addToColumnMap(coordinate: Coordinate) {
                columnMap.computeIfAbsent(coordinate.column){ mutableListOf() }.add(coordinate.toIndex(numColumns))
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

        for (value in (1.. base.maxValueSize())) {
            val lnPerRow = lockedNumbersPerRow[value] ?: continue
            val lnPerColumn = lockedNumbersPerColumn[value] ?: continue
            val graph = graphCoordinates(lnPerRow, lnPerColumn)

            for (tmpMaps in graph) {
                tmpMaps.rowMap.filterValues { it.size == 2 }.forEach { (rowIndex, positions) ->
                    val possibleValuesChanged = cleanValueFromPossibleValues(value, from = base.getRowPositions(rowIndex), possibleValues, positions)
                    if (possibleValuesChanged) result++
                }
                tmpMaps.columnMap.filterValues { it.size == 2 }.forEach { (columnIndex, positions) ->
                    val possibleValuesChanged = cleanValueFromPossibleValues(value, from = base.getColumnPositions(columnIndex), possibleValues, positions)
                    if (possibleValuesChanged) result++
                }
            }
        }

        return result
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

        var numChanges = clean(lockedNumbersPerRow) { base.getColumnPositions(it) }
        numChanges += clean(lockedNumbersPerColumn) { base.getRowPositions(it) }

        return numChanges
    }

    // TODO: This doesn't improve the solver one bit. Find any possible error
    internal fun cleanYWing(
        possibleValues: Array<MutableList<Int>>,
        numColumns: Int,
        lockedNumbersInRows: MutableMap<Int, MutableMap<Int, Pair<Int, Int>>> = getLockedNumbersInRow(possibleValues = possibleValues),
        lockedNumbersInColumns: MutableMap<Int, MutableMap<Int, Pair<Int, Int>>> = getLockedNumbersInColumn(possibleValues = possibleValues)
    ) {
        for ((numberA, data) in lockedNumbersInRows) {
            for ((row, columns) in data) {
                val (columnB, columnC) = columns
                val possValuesB = possibleValues[Coordinate(row, columnB).toIndex(numColumns)]
                val possValuesC = possibleValues[Coordinate(row, columnC).toIndex(numColumns)]
                val numberB = possValuesB.find { it != numberA }!!
                val numberC = possValuesC.find { it != numberA }!!

                if (numberB == numberC || possValuesB.size > 2 || possValuesC.size > 2) continue

                val cleanYWing = { number1: Int, column1: Int, number2: Int, column2: Int ->
                    lockedNumbersInColumns[number1]?.get(column1)
                        ?.let { if (it.first == row) it.second else it.first }
                        ?.let { row2 ->
                            val possValuesB2 = possibleValues[Coordinate(row2, column1).toIndex(numColumns)]
                            if (possValuesB2.size == 2 && possValuesB2.contains(number2)) {
                                //Found Y-Wing. We can delete numberC from (rowB, columnC)
                                val removed = possibleValues[Coordinate(row2, column2).toIndex(numColumns)].remove(number2)
                                removed
                            } else false
                        }
                }
                cleanYWing(numberB, columnB, numberC, columnC)
                cleanYWing(numberC, columnC, numberB, columnB)
            }
        }
    }

}