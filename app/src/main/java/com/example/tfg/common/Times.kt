package com.example.tfg.common

import android.content.Context
import com.example.tfg.R

enum class Times {
    DAY,
    WEEK,
    MONTH,
    YEAR,
    ALWAYS;

    fun toString(context: Context): String {
        return when(this){
            DAY -> context.getString(R.string.today)
            WEEK -> context.getString(R.string.this_week)
            MONTH -> context.getString(R.string.this_month)
            YEAR -> context.getString(R.string.this_year)
            ALWAYS -> context.getString(R.string.always)
        }
    }

}