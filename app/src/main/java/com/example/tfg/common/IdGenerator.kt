package com.example.tfg.common

import android.content.Context
import android.content.SharedPreferences

object IdGenerator {
    private const val PREFS_NAME = "app_ids"
    private const val KEY_NEXT_ID = "next_id"

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun generateId(entityType: String): Long {
        val key = "$entityType-$KEY_NEXT_ID"
        var nextId = sharedPreferences.getLong(key, 1L)
        val id = nextId++
        sharedPreferences.edit().putLong(key, nextId).apply()
        return id
    }
}