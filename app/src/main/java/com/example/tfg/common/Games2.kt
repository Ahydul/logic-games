package com.example.tfg.common

import android.content.Context
import com.example.tfg.R
import com.example.tfg.games.Games

enum class Games2: Selection {
    ALL_GAMES,
    HAKYUU;

    override fun toString(context: Context): String {
        return when(this){
            ALL_GAMES -> context.getString(R.string.all_games)
            HAKYUU -> "Hakyuu"
        }
    }

    fun toGames(): Games? {
        return when(this){
            ALL_GAMES -> null
            HAKYUU -> Games.HAKYUU
        }
    }

    companion object {
        fun fromString(str: String?): Games2? {
            return when (str) {
                Games2.HAKYUU.name -> Games2.HAKYUU
                Games2.ALL_GAMES.name -> Games2.ALL_GAMES
                else -> null
            }
        }
    }

}