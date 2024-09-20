package com.example.tfg.data

import android.content.ContextWrapper
import android.content.SharedPreferences

class FakeContext : ContextWrapper(null) {
    private val fakeSharedPreferences = FakeSharedPreferences()

    override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
        return fakeSharedPreferences
    }
}

class FakeSharedPreferences : SharedPreferences {
    private val data = mutableMapOf<String, Any>()

    override fun edit(): SharedPreferences.Editor {
        return object : SharedPreferences.Editor {
            override fun putLong(key: String, value: Long): SharedPreferences.Editor {
                data[key] = value
                return this
            }
            override fun apply() {}
            override fun commit(): Boolean = true
            // Implement other methods as no-op or minimal functionality
            override fun clear(): SharedPreferences.Editor = this
            override fun remove(key: String): SharedPreferences.Editor = this
            override fun putString(key: String, value: String?): SharedPreferences.Editor = this
            override fun putStringSet(
                key: String?,
                values: MutableSet<String>?
            ): SharedPreferences.Editor {
                TODO("Not necessary")
            }

            override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = this
            override fun putFloat(key: String, value: Float): SharedPreferences.Editor = this
            override fun putInt(key: String, value: Int): SharedPreferences.Editor = this
            // etc.
        }
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    override fun getLong(key: String, defValue: Long): Long {
        return data[key] as? Long ?: defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return 0F
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return false
    }

    // Implement other required methods similarly
    override fun contains(key: String): Boolean = data.containsKey(key)
    override fun getAll(): Map<String, *> = data
    override fun getString(key: String?, defValue: String?): String? {
        return null
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        return null
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return 0
    }
}