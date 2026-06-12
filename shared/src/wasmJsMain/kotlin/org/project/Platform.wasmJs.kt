package org.project

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val supportsPullToRefresh: Boolean = false
}

actual fun getPlatform(): Platform = WasmPlatform()