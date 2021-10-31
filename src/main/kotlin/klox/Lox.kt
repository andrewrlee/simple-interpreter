package klox

import klox.Interpreter.RuntimeError
import klox.TokenType.EOF
import java.io.File
import java.nio.charset.Charset
import kotlin.system.exitProcess

class Lox {

    fun runFile(file: String) {
        run(File(file).readText(Charset.defaultCharset()))
        if (hadError) exitProcess(65)
        if (hadRuntimeError) exitProcess(70)
    }

    fun runPrompt() {
        val reader = System.`in`.bufferedReader(Charset.defaultCharset())

        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            hadError = false
        }
    }

    private fun run(text: String) {
        val tokens = Scanner(text).scanTokens()
        val statements = Parser(tokens).parse()

        if (hadError) return

        val resolver = Resolver(interpreter)
        resolver.resolve(statements)

        if (hadError) return
        interpreter.interpret(statements)
    }

    companion object {
        var hadError = false
        var hadRuntimeError = false
        val interpreter = Interpreter()

        fun error(line: Int, message: String) = report(line, "", message)

        fun error(token: Token, message: String) =
            report(token.line, if (token.type == EOF) "at end" else "at '${token.lexeme}'", message)

        private fun report(line: Int, where: String, message: String) {
            System.err.println("[line $line] Error $where: $message")
            hadError = true
        }

        fun runtimeError(e: RuntimeError) {
            System.err.println("${e.message}\n[line ${e.token.line}]")
            hadRuntimeError = true
        }
    }
}

fun main(args: Array<String>) {
    val lox = Lox()

    when {
        args.size > 1 -> {
            println("Usage: klox [script]")
            exitProcess(64)
        }
        args.size == 1 -> lox.runFile(args[0])
        args.isEmpty() -> lox.runPrompt()
    }
}