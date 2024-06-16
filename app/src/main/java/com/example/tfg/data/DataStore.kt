package com.example.tfg.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

object DataStorePreferences {
    val CHECK_ERRORS_AUTOMATICALLY = booleanPreferencesKey("check_errors_automatically")
    val SNAPSHOTS_ALLOWED = booleanPreferencesKey("snapshots_allowed")
    val LAST_PLAYED_GAME = longPreferencesKey("last_played_game")
    val THEME = stringPreferencesKey("theme")
}

object DataStoreManager {
    val Context.dataStore by preferencesDataStore(name = "settings")
}