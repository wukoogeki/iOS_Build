package org.project.data.api

import org.project.data.local.LocalStorage
import org.project.data.local.StorageKeys

object ApiConfig {
    const val BASE_URL = "https://illicitly-chaste-disband.ngrok-free.dev/api"

    private var currentBaseUrl: String = BASE_URL
    private var authToken: String? = null

    init {
        // Load saved token on init
        val savedToken = LocalStorage.getString(StorageKeys.AUTH_TOKEN)
        if (savedToken != null) {
            authToken = savedToken
        }
    }

    fun getBaseUrl(): String = currentBaseUrl

    fun configure(url: String) {
        currentBaseUrl = url.trimEnd('/')
    }

    fun reset() {
        currentBaseUrl = BASE_URL
        authToken = null
        LocalStorage.remove(StorageKeys.AUTH_TOKEN)
        LocalStorage.remove(StorageKeys.USERNAME)
        LocalStorage.remove(StorageKeys.IS_LOGGED_IN)
    }

    fun setAuthToken(token: String?) {
        authToken = token
        if (token != null) {
            LocalStorage.saveString(StorageKeys.AUTH_TOKEN, token)
            LocalStorage.saveString(StorageKeys.IS_LOGGED_IN, "true")
        } else {
            LocalStorage.remove(StorageKeys.AUTH_TOKEN)
            LocalStorage.remove(StorageKeys.IS_LOGGED_IN)
        }
    }

    fun getAuthToken(): String? = authToken

    fun isLoggedIn(): Boolean {
        return LocalStorage.getString(StorageKeys.IS_LOGGED_IN) == "true" && authToken != null
    }

    fun saveUsername(username: String) {
        LocalStorage.saveString(StorageKeys.USERNAME, username)
    }

    fun getUsername(): String? {
        return LocalStorage.getString(StorageKeys.USERNAME)
    }
}

object ApiRoutes {
    const val LOGIN = "/auth/login"
    const val LOGOUT = "/auth/logout"
    const val DEVICES = "/devices"
    const val DEVICE_BY_ID = "/devices/{id}"
    const val DEVICE_DATA = "/devices/{id}/data"
    const val DEVICE_HISTORY = "/devices/{id}/history"
    const val DEVICE_CONTROL = "/devices/{id}/control"
    const val SYSTEM_STATUS = "/system/status"
}
