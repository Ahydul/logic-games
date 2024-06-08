package com.example.tfg.games.common

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

}