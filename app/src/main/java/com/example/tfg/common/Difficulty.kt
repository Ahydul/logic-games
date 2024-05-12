package com.example.tfg.common

import android.content.Context
import com.example.tfg.R

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD;

    fun toString(context: Context): String {
        return when(this){
            EASY -> context.getString(R.string.easy)
            MEDIUM -> context.getString(R.string.medium)
            HARD -> context.getString(R.string.hard)
        }
    }

    companion object {
        fun get(value: String): Difficulty {
            return Difficulty.entries.find { it.name.lowercase() == value.lowercase() }!!
        }
        fun get(index: Int): Difficulty {
            return Difficulty.entries[index]
        }
    }
}