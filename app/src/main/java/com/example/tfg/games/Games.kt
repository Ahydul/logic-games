package com.example.tfg.games

import com.example.tfg.common.Games2

enum class Games(val title: String) {
    HAKYUU("Hakyuu");

    override fun toString(): String {
        return this.title
    }

    fun toGames2(): Games2 {
        return when(this){
            Games.HAKYUU -> Games2.HAKYUU
        }
    }

}