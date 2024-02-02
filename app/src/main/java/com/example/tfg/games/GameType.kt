package com.example.tfg.games

interface GameType {
    val type: Games
    val rules: List<String>
    val url: String
    val noNotes: Boolean
}