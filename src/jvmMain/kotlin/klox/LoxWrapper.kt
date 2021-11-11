package klox

import java.io.File
import java.nio.charset.Charset
import kotlin.system.exitProcess

val handler = object: Handler {
    override fun println(message: Any?) = kotlin.io.println(message)
    override fun onError(message: Any?)= System.err.println(message)
}

class LoxWrapper(val lox: Lox = Lox(handler)) {

    fun runFile(file: String) {
        lox.run(File(file).readText(Charset.defaultCharset()))
        if (lox.context.hadError) exitProcess(65)
        if (lox.context.hadRuntimeError) exitProcess(70)
    }

    fun runPrompt() {
        val reader = System.`in`.bufferedReader(Charset.defaultCharset())

        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            lox.run(line)
            lox.context.hadError = false
        }
    }
}

fun main(args: Array<String>) {
    val wrapper = LoxWrapper()

    when {
        args.size > 1 -> println("Usage: klox [script]").also {  exitProcess(64) }
        args.size == 1 -> wrapper.runFile(args[0])
        args.isEmpty() -> wrapper.runPrompt()
    }
}