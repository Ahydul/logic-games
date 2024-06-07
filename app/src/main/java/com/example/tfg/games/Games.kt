package com.example.tfg.games

enum class Games(val title: String) {
    HAKYUU("Hakyuu");

    override fun toString(): String {
        return this.title
    }
}