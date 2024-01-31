package com.example.tfg.games

interface GameType {
    val name: String
    val rules: List<String>
    val url: String
    val noNotes: Boolean
}