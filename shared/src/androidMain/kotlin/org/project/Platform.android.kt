package org.project

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val supportsPullToRefresh: Boolean = true
}

actual fun getPlatform(): Platform = AndroidPlatform()