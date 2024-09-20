package com.example.tfg.games.common

import com.example.tfg.games.hakyuu.HakyuuScore
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class ScoreDeserializer : JsonDeserializer<Score> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext?
    ): Score {
        val jsonObject = json.asJsonObject
        return when (Games.valueOf(jsonObject.get("game").asString)) {
            Games.HAKYUU -> {
                val score = jsonObject.get("score").asInt
                val bruteForce = jsonObject.get("bruteForce").asInt

                HakyuuScore(score = score, bruteForce = bruteForce)
            }
        }
    }
}