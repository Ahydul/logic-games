package com.example.tfg.games

import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class ScoreSerializer : JsonSerializer<Score> {
    override fun serialize(
        src: Score,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return src.serialize()
    }
}