package com.example.tfg.games.kendoku

import com.example.tfg.common.utils.Utils

class BruteForceHelper private constructor(
    private var bruteForceIteration: Int = 0,
    private val operationPerRegion: Array<KendokuOperation?>,
    private val regionsSetPerIteration: MutableMap<Int, MutableList<Int>> = mutableMapOf()
) {
    constructor(operations: Map<Int, KendokuOperation>) :
            this(operationPerRegion = operations.map { (_, operation) ->
                if (operation.isUnknown()) null
                else operation
            }.toTypedArray())

    fun get(position: Int): KendokuOperation? {
        return operationPerRegion[position]
    }

    fun set(operation: KendokuOperation, position: Int) {
        require(!operation.isUnknown()) { "Operation provided is unknown" }
        require(operationPerRegion[position] == null) { "Region already set. Overriding forbidden" }

        operationPerRegion[position] = operation

        Utils.addToMapList(key = bruteForceIteration, value = position, map = regionsSetPerIteration)
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