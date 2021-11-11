package klox

import kotlin.js.JsName

expect class Platform() {
    fun currentTime(): Long
}

interface Handler {
    @JsName("println")
    fun println(message: Any?)

    @JsName("onError")
    fun onError(message: Any?)
}
