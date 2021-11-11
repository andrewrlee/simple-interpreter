package klox

import kotlin.js.Date

class JsPlatform {
    fun currentTime() = Date().getTime().toLong()
}

actual typealias Platform = JsPlatform
