package org.project

interface Platform {
    val name: String
    val supportsPullToRefresh: Boolean
}

expect fun getPlatform(): Platform