package com.example.tfg.games.kendoku

import com.example.tfg.common.utils.Utils

class BruteForceHelper private constructor(
    private var bruteForceIteration: Int = 0,
    private val operationPerRegion: Array<KnownKendokuOperation?>,
    private val regionsSetPerIteration: MutableMap<Int, MutableList<Int>> = mutableMapOf()
) {
    constructor(operations: Map<Int, KendokuOperation>) :
            this(operationPerRegion =
            operations.map { (_, operation) ->
               operation.toKnownEnum()
            }.toTypedArray())

    fun get(regionID: Int): KnownKendokuOperation? {
        return operationPerRegion[regionID]
    }

    fun set(operation: KnownKendokuOperation, regionID: Int) {
        require(operationPerRegion[regionID] == null) { "Region already set. Overriding forbidden" }

        operationPerRegion[regionID] = operation

        Utils.addToMapList(key = bruteForceIteration, value = regionID, map = regionsSetPerIteration)
    }

    fun regressIteration() {
        require(bruteForceIteration > 0) { "Tried to regress iteration = 0" }

        // Remove operations set in this iteration
        regionsSetPerIteration[bruteForceIteration]?.forEach { position ->
            operationPerRegion[position] = null
        }

        // Remove iteration
        regionsSetPerIteration.remove(bruteForceIteration)
        bruteForceIteration--
    }
}