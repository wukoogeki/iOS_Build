package org.project

import android.content.Context

object AndroidAppContext {
    private var context: Context? = null

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    fun get(): Context {
        return context ?: throw IllegalStateException("AndroidAppContext not initialized")
    }
}
