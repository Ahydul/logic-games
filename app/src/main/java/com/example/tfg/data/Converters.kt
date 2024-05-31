package com.example.tfg.data

import androidx.room.TypeConverter
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.games.GameType
import com.example.tfg.games.GameTypeDeserializer
import com.example.tfg.games.Score
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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


    // List<Coordinate> converters

    @TypeConverter
    @JvmStatic
    fun fromCoordinateList(coordinates: List<Coordinate>?): String? {
        return Gson().toJson(coordinates)
    }

    @TypeConverter
    @JvmStatic
    fun toCoordinateList(coordinatesString: String?): List<Coordinate>? {
        return coordinatesString?.let {
            val type = object : TypeToken<Set<Pair<Int, Int>>>() {}.type
            Gson().fromJson(it, type)
        }
    }


    // Set<Pair<Int, Int>> converters

    @TypeConverter
    fun fromPairSet(set: Set<Pair<Int, Int>>?): String? {
        return Gson().toJson(set)
    }

    @TypeConverter
    fun toPairSet(setString: String?): Set<Pair<Int, Int>>? {
        return setString?.let {
            val type = object : TypeToken<Set<Pair<Int, Int>>>() {}.type
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


    // Score converters

    @TypeConverter
    fun fromScore(score: Score?): Int? {
        return score?.get()
    }

    @TypeConverter
    fun toScore(score: Int?): Score? {
        return score?.let {
            Score(it)
        }
    }


    // GameType converters

    @TypeConverter
    fun fromGameType(gameType: GameType): String {
        return Gson().toJson(gameType)
    }

    @TypeConverter
    fun toGameType(value: String): GameType {
        val type = object : TypeToken<GameType>() {}.type
        return customGson().fromJson(value, type)
    }

    private fun customGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(GameType::class.java, GameTypeDeserializer())
            .create()
    }
}