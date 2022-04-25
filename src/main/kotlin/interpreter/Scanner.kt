package klox.simple.interpreter

import klox.simple.interpreter.Token.Companion.toToken
import klox.simple.interpreter.Token.EndOfFile.EOF

class Scanner {
    fun scanTokens(source: String) = source
        .replace("(\\D)".toRegex(), " $1 ")  // " 1 + 2*4"      => "  1  +  2 * 4"
        .split("\\s+".toRegex())             // "  1  +  2 * 4" => ["1", "+", "2", "*", "4"]
        .map { toToken(it) } + EOF           // [1.0, ADD, 2.0, MULTIPLY, 4.0, EOF]
}