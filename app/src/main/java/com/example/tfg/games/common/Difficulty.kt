package com.example.tfg.games.common

import android.content.Context
import com.example.tfg.R

enum class Difficulty {
    BEGINNER,
    EASY,
    MEDIUM,
    HARD,
    EXPERT,
    MASTER;

    fun toString(context: Context): String {
        return when(this){
            BEGINNER -> context.getString(R.string.beginner)
            EASY -> context.getString(R.string.easy)
            MEDIUM -> context.getString(R.string.medium)
            HARD -> context.getString(R.string.hard)
            EXPERT -> context.getString(R.string.expert)
            MASTER -> context.getString(R.string.master)
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