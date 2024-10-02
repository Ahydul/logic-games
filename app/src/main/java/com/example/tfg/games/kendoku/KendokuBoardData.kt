package com.example.tfg.games.kendoku

import com.example.tfg.games.common.BoardData

class KendokuBoardData(
    possibleValues: Array<MutableList<Int>>,
    actualValues: IntArray,
    val knownOperations: MutableMap<Int, KnownKendokuOperation>,
    val regionCombinations: MutableMap<Int, MutableList<IntArray>> = mutableMapOf()
) : BoardData(possibleValues, actualValues) {
    override fun clone(): BoardData {
        val newBoardData = super.clone()
        return KendokuBoardData(
            newBoardData.possibleValues,
            newBoardData.actualValues,
            knownOperations.toMutableMap(),
            // No need to deep clone because the arrays are only created, not modified
            regionCombinations.mapValues { (_, list) ->
                list.toMutableList()
            }.toMutableMap()
        )
    }

    override fun replaceDataWith(newBoardData: BoardData) {
        super.replaceDataWith(newBoardData)
        val newBoardData = newBoardData as KendokuBoardData
        knownOperations.clear()
        knownOperations.putAll(newBoardData.knownOperations)
        regionCombinations.clear()
        regionCombinations.putAll(newBoardData.regionCombinations)
    }

    fun setRegionCombinations(regionID: Int, combinations: MutableList<IntArray>) {
        regionCombinations[regionID] = combinations
    }

    companion object {
        fun create(
            possibleValues: Array<MutableList<Int>>,
            actualValues: IntArray,
            operationPerRegion: MutableMap<Int, KendokuOperation>
        ): KendokuBoardData {
            val knownOperations = operationPerRegion.mapNotNull {
                val op = it.value.toKnownEnum()
                if (op == null) null
                else it.key to op
            }.toMap().toMutableMap()
            return KendokuBoardData(possibleValues, actualValues, knownOperations)
        }
    }
}