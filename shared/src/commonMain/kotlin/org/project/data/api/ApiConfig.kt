package org.project.data.api

object ApiConfig {
    var baseUrl: String = "https://illicitly-chaste-disband.ngrok-free.dev/api"

    const val DEFAULT_BASE_URL = "https://illicitly-chaste-disband.ngrok-free.dev/api"

    private var authToken: String? = null

    fun configure(url: String) {
        baseUrl = url.trimEnd('/')
    }

    fun reset() {
        baseUrl = DEFAULT_BASE_URL
        authToken = null
    }

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun getAuthToken(): String? = authToken
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