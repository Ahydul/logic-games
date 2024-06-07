package com.example.tfg.common

import android.content.Context
import com.example.tfg.R

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
            Difficulty2.ALL_DIFFICULTIES -> context.getString(R.string.all_difficulties)
            Difficulty2.BEGINNER -> context.getString(R.string.beginner)
            Difficulty2.EASY -> context.getString(R.string.easy)
            Difficulty2.MEDIUM -> context.getString(R.string.medium)
            Difficulty2.HARD -> context.getString(R.string.hard)
            Difficulty2.EXPERT -> context.getString(R.string.expert)
            Difficulty2.MASTER -> context.getString(R.string.master)
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