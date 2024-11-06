package com.example.tfg.games.hakyuu

import com.example.tfg.R
import com.example.tfg.games.common.GameValue

enum class NumberValue(override val value: Int, override val icon: Int): GameValue {
    EMPTY(0, R.drawable.outline_close_24),
    ZERO(0, R.drawable.number_zero),
    ONE(1, R.drawable.number_1),
    TWO(2, R.drawable.number_2),
    THREE(3, R.drawable.number_3),
    FOUR(4, R.drawable.number_4),
    FIVE(5, R.drawable.number_5),
    SIX(6, R.drawable.number_6),
    SEVEN(7, R.drawable.number_7),
    EIGHT(8, R.drawable.number_8),
    NINE(9, R.drawable.number_9),
    TEN(10, R.drawable.number_10),
    ELEVEN(11, R.drawable.number_11),
    TWELVE(12, R.drawable.number_12),
    THIRTEEN(13, R.drawable.number_13),
    FOURTEEN(14, R.drawable.number_14),
    FIFTEEN(15, R.drawable.number_15),
    SIXTEEN(16, R.drawable.number_16),
    SEVENTEEN(17, R.drawable.number_17),
    EIGHTEEN(18, R.drawable.number_18),
    NINETEEN(19, R.drawable.number_19),
    TWENTY(20, R.drawable.number_20);

    companion object {
        fun get(value: Int): NumberValue {
            if (value < 21) return entries[value + 1]
            return EMPTY
        }

        fun getBigNumber(value: Int) : List<NumberValue> {
            val res = mutableListOf<NumberValue>()
            var value = value
            while (value > 0) {
                res.add(NumberValue.get(value % 10))
                value /= 10
            }
            return res.reversed()
        }
    }
}