package com.example.tfg.games.kendoku

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
}