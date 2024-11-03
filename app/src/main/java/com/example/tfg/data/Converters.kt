package com.example.tfg.data

import androidx.room.TypeConverter
import com.example.tfg.games.common.Score
import com.example.tfg.games.common.ScoreDeserializer
import com.example.tfg.games.common.ScoreSerializer
import com.example.tfg.games.kendoku.KendokuOperation
import com.example.tfg.games.kendoku.KnownKendokuOperation
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // LocalDateTime converters

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let {
            LocalDateTime.parse(it, formatter)
        }
    }


    // Pair<Int, Int> converters

    @TypeConverter
    fun fromPair(value: Pair<Int, Int>): String {
        return Gson().toJson(value)
    }


    // Set<Pair<Int, Int>> converters

    @TypeConverter
    fun fromPairSet(set: MutableSet<Pair<Int, Int>>?): String? {
        return Gson().toJson(set)
    }

    @TypeConverter
    fun toPairSet(setString: String?): MutableSet<Pair<Int, Int>>? {
        return setString?.let {
            val type = object : TypeToken<MutableSet<Pair<Int, Int>>>() {}.type
            Gson().fromJson(it, type)
        }
    }


    // IntArray converters

    @TypeConverter
    fun fromIntArray(value: IntArray?): String? {
        return value?.let {
            Gson().toJson(it)
        }
    }

    @TypeConverter
    fun toIntArray(value: String?): IntArray? {
        return value?.let {
            Gson().fromJson(it, IntArray::class.java)
        }
    }


    // Kendoku converters

    @TypeConverter
    fun fromKendokuMap(value: MutableMap<Int, KendokuOperation>?): String? {
        return value?.let {
            Gson().toJson(it)
        }
    }

    @TypeConverter
    fun toKendokuMap(value: String?): MutableMap<Int, KendokuOperation>? {
        return value?.let {
            (Gson().fromJson(it, MutableMap::class.java) as MutableMap<String, String>)
                .map { (key, value) ->  key.toInt() to KendokuOperation.valueOf(value) }.toMap().toMutableMap()
        }
    }

    @TypeConverter
    fun fromKendokuArray(value: Array<KnownKendokuOperation>?): String? {
        return value?.let {
            Gson().toJson(it)
        }
    }

    @TypeConverter
    fun toKendokuArray(value: String?): Array<KnownKendokuOperation>? {
        return value?.let {
            Gson().fromJson(it, Array<KnownKendokuOperation>::class.java)
        }
    }

    // Score converters

    @TypeConverter
    fun fromScore(score: Score): String {
        return fromScoreGson().toJson(score)
    }

    private fun fromScoreGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Score::class.java, ScoreSerializer())
            .create()
    }

    @TypeConverter
    fun toScore(value: String): Score {
        return value.let {
            toScoreGson().fromJson(it, Score::class.java)
        }
    }

    private fun toScoreGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Score::class.java, ScoreDeserializer())
            .create()
    }
}