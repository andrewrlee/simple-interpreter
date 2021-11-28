package klox

import klox.interpreter.Interpreter
import klox.interpreter.Parser
import klox.interpreter.Resolver
import klox.interpreter.Scanner
import kotlin.js.JsName

class Lox(handler: Handler) {

    val context = Context(Platform(), handler)
    private val interpreter = Interpreter(context)

    @JsName("run")
    fun run(text: String) {
        val tokens = Scanner(context, text).scanTokens()
        val statements = Parser(context, tokens).parse()

        if (context.hadError) return

        val resolver = Resolver(context, interpreter)
        resolver.resolve(statements)

        if (context.hadError) return
        interpreter.interpret(statements)
    }

    @JsName("reset")
    fun reset() {
        context.reset()
    }
}
