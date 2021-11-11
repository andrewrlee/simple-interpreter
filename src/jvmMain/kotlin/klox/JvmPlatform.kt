package klox

class JvmPlatform {
    fun currentTime() = System.currentTimeMillis()
}

actual typealias Platform = JvmPlatform
