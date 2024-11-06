package com.example.tfg.games.kendoku

import com.example.tfg.R

enum class KnownKendokuOperation(val icon: Int) {
    SUM(R.drawable.add_operation),
    SUBTRACT(R.drawable.minus_operation),
    MULTIPLY(R.drawable.outline_close_24),
    DIVIDE(R.drawable.percent_operation);

    fun toGeneralEnum(): KendokuOperation {
        return when(this) {
            SUM -> KendokuOperation.SUM
            SUBTRACT -> KendokuOperation.SUBTRACT
            MULTIPLY -> KendokuOperation.MULTIPLY
            DIVIDE -> KendokuOperation.DIVIDE
        }
    }

    fun operate(values: List<Int>): Int {
        return when(this){
            SUM -> values.sum()
            SUBTRACT -> values.max() - values.min()
            MULTIPLY -> values.reduce { acc, num -> acc * num }
            DIVIDE -> values.max() / values.min()
        }
    }

    fun isDivideOrSubtract(): Boolean {
        return when(this) {
            SUBTRACT, DIVIDE -> true
            else -> false
        }
    }

    companion object {
        fun allOperations(): Array<KnownKendokuOperation> {
            return KnownKendokuOperation.entries.toTypedArray()
        }

        fun multiplyOperation(): Array<KnownKendokuOperation> {
            return arrayOf(MULTIPLY)
        }

        fun sumOperation(): Array<KnownKendokuOperation> {
            return arrayOf(MULTIPLY)
        }

    }
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
}