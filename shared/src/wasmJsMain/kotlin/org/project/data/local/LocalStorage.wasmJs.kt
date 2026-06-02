package org.project.data.local

import kotlinx.browser.localStorage

actual object LocalStorage {
    actual fun saveString(key: String, value: String) {
        localStorage.setItem(key, value)
    }

    actual fun getString(key: String): String? {
        return localStorage.getItem(key)
    }

    actual fun remove(key: String) {
        localStorage.removeItem(key)
    }

    actual fun clear() {
        localStorage.clear()
    }
}
