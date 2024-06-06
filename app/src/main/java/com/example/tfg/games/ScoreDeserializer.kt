package com.example.tfg.games

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
                val newValue = jsonObject.get("newValue").asInt
                val rule2 = jsonObject.get("rule2").asInt
                val rule3 = jsonObject.get("rule3").asInt
                val hiddenSingle = jsonObject.get("hiddenSingle").asInt
                val hiddenPair = jsonObject.get("hiddenPair").asInt
                val hiddenTriple = jsonObject.get("hiddenTriple").asInt
                val obviousPair = jsonObject.get("obviousPair").asInt
                val obviousTriple = jsonObject.get("obviousTriple").asInt
                val bruteForce = jsonObject.get("bruteForce").asInt

                HakyuuScore(
                    newValue = newValue,
                    rule2 = rule2,
                    rule3 = rule3,
                    hiddenSingle = hiddenSingle,
                    hiddenPair = hiddenPair,
                    hiddenTriple = hiddenTriple,
                    obviousPair = obviousPair,
                    obviousTriple = obviousTriple,
                    bruteForce = bruteForce
                )
            }
        }
    }
}