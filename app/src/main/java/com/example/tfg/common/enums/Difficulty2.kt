package com.example.tfg.common.enums

import android.content.Context
import com.example.tfg.R
import com.example.tfg.games.common.Difficulty

enum class Difficulty2: Selection {
    ALL_DIFFICULTIES,
    BEGINNER,
    EASY,
    MEDIUM,
    HARD,
    EXPERT,
    MASTER;

    override fun toString(context: Context): String {
        return when(this){
            ALL_DIFFICULTIES -> context.getString(R.string.all_difficulties)
            BEGINNER -> context.getString(R.string.beginner)
            EASY -> context.getString(R.string.easy)
            MEDIUM -> context.getString(R.string.medium)
            HARD -> context.getString(R.string.hard)
            EXPERT -> context.getString(R.string.expert)
            MASTER -> context.getString(R.string.master)
        }
    }

    fun toDifficulty(): Difficulty? {
        return when(this){
            ALL_DIFFICULTIES -> null
            BEGINNER -> Difficulty.BEGINNER
            EASY -> Difficulty.EASY
            MEDIUM -> Difficulty.MEDIUM
            HARD -> Difficulty.HARD
            EXPERT -> Difficulty.EXPERT
            MASTER -> Difficulty.MASTER
        }
    }


}