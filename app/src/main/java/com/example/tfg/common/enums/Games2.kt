package com.example.tfg.common.enums

import android.content.Context
import com.example.tfg.R
import com.example.tfg.games.common.Games
import java.util.Locale

enum class Games2: Selection {
    ALL_GAMES,
    HAKYUU,
    KENDOKU,
    FACTORS,
    SUMDOKU;

    override fun toString(context: Context): String {
        return if (this == ALL_GAMES) context.getString(R.string.all_games)
            else this.name.lowercase().replaceFirstChar { it.titlecase(Locale.ROOT) }
    }

    fun toGames(): Games? {
        return if (this == ALL_GAMES) null
            else Games.valueOf(this.name)
    }
}