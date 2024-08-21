package com.example.tfg.games.kendoku

import com.example.tfg.common.utils.Coordinate

enum class KendokuOperation {
    SUM,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,

    SUM_UNKNOWN,
    SUBTRACT_UNKNOWN,
    MULTIPLY_UNKNOWN,
    DIVIDE_UNKNOWN,

    ANY;

    fun operate(values: List<Int>): Int {
        return when(this){
            SUM, SUM_UNKNOWN -> values.sum()
            SUBTRACT, SUBTRACT_UNKNOWN -> values.max() - values.min()
            MULTIPLY, MULTIPLY_UNKNOWN -> values.reduce { acc, num -> acc * num }
            DIVIDE, DIVIDE_UNKNOWN -> values.max() / values.min()
            ANY -> values.first()
        }
    }

    fun isUnknown(): Boolean {
        return when(this){
            SUM, SUBTRACT, MULTIPLY, DIVIDE -> false
            else -> true
        }
    }

    fun filterOperation(region: Map<Coordinate, List<Int>>): Boolean {
        return when(this){
            SUM -> TODO()
            SUBTRACT -> TODO()
            MULTIPLY -> TODO()
            DIVIDE -> TODO()
            else -> false
        }
    }

    companion object {
        fun allButOperationAny(): Array<KendokuOperation> {
            return KendokuOperation.entries
                .filterNot { it == ANY }
                .toTypedArray()
        }

        fun knownOperations(): Array<KendokuOperation> {
            return KendokuOperation.entries
                .filterNot { it.isUnknown() }
                .toTypedArray()
        }

        fun allOperations(): Array<KendokuOperation> {
            return KendokuOperation.entries.toTypedArray()
        }
    }
}