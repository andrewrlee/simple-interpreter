package klox

import klox.interpreter.Interpreter.RuntimeError
import klox.interpreter.Token
import klox.interpreter.TokenType.EOF

class Context(private val platform: Platform, private val handler: Handler) {
    var hadError = false
    var hadRuntimeError = false

    fun reset() {
        hadError = false
    }

    fun error(line: Int, message: String) = report(line, "", message)

    fun error(token: Token, message: String) =
        report(token.line, if (token.type == EOF) "at end" else "at '${token.lexeme}'", message)

    private fun report(line: Int, where: String, message: String) {
        handler.onError("[line $line] Error $where: $message")
        hadError = true
    }

    fun runtimeError(e: RuntimeError) {
        handler.onError("${e.message}\n[line ${e.token.line}]")
        hadRuntimeError = true
    }

    fun currentTime() = platform.currentTime()

    fun println(message: String) = handler.println(message)
}
