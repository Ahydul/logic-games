package com.example.tfg.games

import com.example.tfg.games.hakyuu.Hakyuu
import com.example.tfg.games.hakyuu.HakyuuScore
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class GameTypeDeserializer : JsonDeserializer<GameType> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): GameType {
        val jsonObject = json.asJsonObject
        val type = Games.valueOf(jsonObject.get("type").asString.uppercase())
        val numColumns = jsonObject.get("numColumns").asInt
        val numRows = jsonObject.get("numRows").asInt
        val seed = jsonObject.get("seed").asLong
        val score = jsonObject.get("score").asJsonObject["score"].asInt
        val startBoard = jsonObject.get("startBoard").asJsonArray.map { it.asInt }.toIntArray()
        val completedBoard = jsonObject.get("completedBoard").asJsonArray.map { it.asInt }.toIntArray()
        val boardRegions = jsonObject.get("boardRegions").asJsonArray.map { it.asInt }.toIntArray()

        return when (type) {
            Games.HAKYUU ->
                Hakyuu(
                    numColumns = numColumns,
                    numRows = numRows,
                    seed = seed,
                    score = HakyuuScore(score),
                    startBoard = startBoard,
                    completedBoard = completedBoard,
                    regions = boardRegions
                )
        }
    }
}