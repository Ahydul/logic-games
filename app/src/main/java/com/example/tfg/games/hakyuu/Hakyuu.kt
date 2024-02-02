package com.example.tfg.games.hakyuu

import com.example.tfg.games.GameType
import com.example.tfg.games.Games

class Hakyuu(
    override val type: Games = Games.HAKYUU,
    override val rules: List<String> = listOf(),
    override val url: String = "",
    override val noNotes: Boolean = true,
) : GameType {

    //Logic


}