package com.example.tfg.common.enums

import android.content.Context
import com.example.tfg.R
import com.example.tfg.games.common.Games

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
                HAKYUU.name -> HAKYUU
                ALL_GAMES.name -> ALL_GAMES
                else -> null
            }
        }
    }

}