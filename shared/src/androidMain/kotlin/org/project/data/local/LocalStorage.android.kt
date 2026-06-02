package org.project.data.local

import android.content.Context
import android.content.SharedPreferences
import org.project.AndroidAppContext

actual object LocalStorage {
    private const val PREFS_NAME = "app_prefs"
    
    private val prefs: SharedPreferences
        get() = AndroidAppContext.get().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    actual fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    actual fun clear() {
        prefs.edit().clear().apply()
    }
}
