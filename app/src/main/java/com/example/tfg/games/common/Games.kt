package com.example.tfg.games.common

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import com.example.tfg.R
import com.example.tfg.common.enums.Games2
import com.example.tfg.common.utils.Utils

enum class Games(val title: String, val minSize: Int, val maxSize: Int) {
    HAKYUU("Hakyuu", 3,13),
    KENDOKU("Kendoku", 3, 9);

    override fun toString(): String {
        return this.title
    }

    fun toGames2(): Games2 {
        return when(this){
            HAKYUU -> Games2.HAKYUU
            KENDOKU -> Games2.KENDOKU
        }
    }

    fun getRules(context: Context): AnnotatedString {
        return when(this){
            HAKYUU -> {
                val jankoPage = context.getString(R.string.janko_page)
                Utils.buildStringWithLink(
                    context = context,
                    link = jankoPage,
                    linkName = jankoPage,
                    text = context.getString(R.string.hakyuu_rule_1) + "\n\n" +
                            context.getString(R.string.hakyuu_rule_2) + "\n\n" +
                            context.getString(R.string.hakyuu_rule_3) + "\n\n" +
                            context.getString(R.string.rules_from) + ":\n",
                    )
            }
            KENDOKU -> {
                val jankoPage = context.getString(R.string.kendoku_page)
                Utils.buildStringWithLink(
                    context = context,
                    link = jankoPage,
                    linkName = jankoPage,
                    text = context.getString(R.string.kendoku_rule_1) + "\n\n" +
                            context.getString(R.string.kendoku_rule_2) + "\n\n" +
                            context.getString(R.string.kendoku_rule_3) + "\n\n" +
                            context.getString(R.string.kendoku_rule_4) + "\n\n" +
                            context.getString(R.string.rules_from) + ":\n",
                )
            }

        }
    }

}