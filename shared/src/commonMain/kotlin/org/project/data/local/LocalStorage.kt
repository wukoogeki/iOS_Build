package org.project.data.local

expect object LocalStorage {
    fun saveString(key: String, value: String)
    fun getString(key: String): String?
    fun remove(key: String)
    fun clear()
}

object StorageKeys {
    const val AUTH_TOKEN = "auth_token"
    const val USERNAME = "username"
    const val IS_LOGGED_IN = "is_logged_in"
}
