package com.example.tfg.common.enums

import android.content.Context
import com.example.tfg.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoField

enum class Times: Selection {
    ALWAYS,
    DAY,
    WEEK,
    MONTH,
    YEAR;

    override fun toString(context: Context): String {
        return when(this){
            DAY -> context.getString(R.string.today)
            WEEK -> context.getString(R.string.this_week)
            MONTH -> context.getString(R.string.this_month)
            YEAR -> context.getString(R.string.this_year)
            ALWAYS -> context.getString(R.string.always)
        }
    }

    fun toLocalDateTimes(): LocalDateTime? {
        return when(this){
            DAY -> LocalDate.now().atStartOfDay()
            WEEK -> LocalDate.now().with(ChronoField.DAY_OF_WEEK, 1).atStartOfDay()
            MONTH -> LocalDate.now().withDayOfMonth(1).atStartOfDay()
            YEAR -> LocalDate.now().withDayOfYear(1).atStartOfDay()
            ALWAYS -> null
        }
    }

}