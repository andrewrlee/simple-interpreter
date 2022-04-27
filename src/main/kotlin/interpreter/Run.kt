package interpreter

import klox.simple.interpreter.Interpreter
import klox.simple.interpreter.Parser
import klox.simple.interpreter.Scanner

fun main() {
    val tokens = Scanner().scanTokens("1 + 2 * 3 / 4")

    println(tokens)
    // [1.0, PLUS, 2.0, STAR, 3.0, SLASH, 4.0, EOF]

    val ast = Parser().parse(tokens)

    println(AstPrinter().print(ast))
    // 1.0 + ((2.0 * 3.0) / 4.0)

    val result = Interpreter().evaluate(ast)

    println(result)
    // 2.5
}