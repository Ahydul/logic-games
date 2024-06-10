package com.example.tfg.games.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.example.tfg.common.enums.Games2

enum class Games(val title: String) {
    HAKYUU("Hakyuu");

    override fun toString(): String {
        return this.title
    }

    fun toGames2(): Games2 {
        return when(this){
            HAKYUU -> Games2.HAKYUU
        }
    }

    fun getRules(): AnnotatedString {
        return when(this){
            HAKYUU -> buildAnnotatedString {
                    append("1. A number must be entered in each field of the diagram.\n\n")
                    append("2. In every area of N fields, every number from the range 1~N must appear exactly once.\n\n")
                    append("3. If two fields in a row or column contain the same number Z, there must be at least Z fields with different numbers between these two fields.\n\n")

                    append("Rules from: ")

                    pushStringAnnotation(tag = "web", annotation = "https://www.janko.at/Raetsel/Hakyuu/index.htm")
                    append("https://www.janko.at/Raetsel/Hakyuu/index.htm")
                    pop()
                }
        }
    }

}