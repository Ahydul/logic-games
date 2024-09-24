package com.example.tfg.games.kendoku

import com.example.tfg.common.utils.Coordinate

enum class KnownKendokuOperation {
    SUM,
    SUBTRACT,
    MULTIPLY,
    DIVIDE
}

enum class KendokuOperation {
    SUM,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,

    SUM_UNKNOWN,
    SUBTRACT_UNKNOWN,
    MULTIPLY_UNKNOWN,
    DIVIDE_UNKNOWN;

    fun operate(values: List<Int>): Int {
        return when(this){
            SUM, SUM_UNKNOWN -> values.sum()
            SUBTRACT, SUBTRACT_UNKNOWN -> values.max() - values.min()
            MULTIPLY, MULTIPLY_UNKNOWN -> values.reduce { acc, num -> acc * num }
            DIVIDE, DIVIDE_UNKNOWN -> values.max() / values.min()
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

    fun reverse(): KendokuOperation {
        return when(this){
            SUM -> SUM_UNKNOWN
            SUBTRACT -> SUBTRACT_UNKNOWN
            MULTIPLY -> MULTIPLY_UNKNOWN
            DIVIDE -> DIVIDE_UNKNOWN
            SUM_UNKNOWN -> SUM
            SUBTRACT_UNKNOWN -> SUBTRACT
            MULTIPLY_UNKNOWN -> MULTIPLY
            DIVIDE_UNKNOWN -> DIVIDE
        }
    }

    fun toKnownEnum(): KnownKendokuOperation? {
        return when(this) {
            SUM -> KnownKendokuOperation.SUM
            SUBTRACT -> KnownKendokuOperation.SUBTRACT
            MULTIPLY -> KnownKendokuOperation.MULTIPLY
            DIVIDE -> KnownKendokuOperation.DIVIDE
            else -> null
        }
    }

    companion object {
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