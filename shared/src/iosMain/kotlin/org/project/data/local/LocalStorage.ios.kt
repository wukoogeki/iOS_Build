package org.project.data.local

import platform.Foundation.NSUserDefaults

actual object LocalStorage {
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults()

    actual fun saveString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
        defaults.synchronize()
    }

    actual fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }

    actual fun remove(key: String) {
        defaults.removeObjectForKey(key)
        defaults.synchronize()
    }

    actual fun clear() {
        val keys = defaults.dictionaryRepresentation().keys
        keys.forEach { key ->
            defaults.removeObjectForKey(key as String)
        }
        defaults.synchronize()
    }
}
