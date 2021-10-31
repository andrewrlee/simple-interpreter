package klox

import klox.TokenType.BANG
import klox.TokenType.BANG_EQUAL
import klox.TokenType.COMMA
import klox.TokenType.DOT
import klox.TokenType.EOF
import klox.TokenType.EQUAL
import klox.TokenType.EQUAL_EQUAL
import klox.TokenType.GREATER
import klox.TokenType.GREATER_EQUAL
import klox.TokenType.IDENTIFIER
import klox.TokenType.LEFT_BRACE
import klox.TokenType.LEFT_PAREN
import klox.TokenType.LESS
import klox.TokenType.LESS_EQUAL
import klox.TokenType.MINUS
import klox.TokenType.NUMBER
import klox.TokenType.PLUS
import klox.TokenType.RIGHT_BRACE
import klox.TokenType.RIGHT_PAREN
import klox.TokenType.SEMICOLON
import klox.TokenType.SLASH
import klox.TokenType.STAR
import klox.TokenType.STRING

class Scanner(private val source: String) {
    private val tokens: MutableList<Token> = mutableListOf()
    private var start = 1
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '/' -> if (match('/')) comment() else addToken(SLASH)
            '"' -> string()
            '\n' -> line++
            ' ', '\r', '\t' -> Unit
            else -> {
                when {
                    isDigit(c) -> number()
                    isAlpha(c) -> identifier()
                    else -> Lox.error(line, "Unexpected character: $c.")
                }
            }
        }
    }

    private fun advance() = source[current++]

    private fun addToken(type: TokenType, literal: Any? = null) =
        tokens.add(Token(type, source.substring(start, current), literal, line))

    private fun match(expected: Char) = when {
        isAtEnd() -> false
        source[current] != expected -> false
        else -> {
            current++
            true
        }
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.")
            return
        }
        advance()
        addToken(STRING, source.substring(start + 1, current - 1))
    }

    private fun number() {
        while (isDigit(peek()) && !isAtEnd()) advance()
        if (peek() == '.' && isDigit(peekNext())) {
            advance()
            while (isDigit(peek())) advance()
        }
        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.substring(start, current)
        addToken(KEYWORDS.getOrDefault(text, IDENTIFIER))
    }

    private fun comment() {
        while (peek() != '\n' && !isAtEnd()) advance()
    }

    private fun peek() = if (isAtEnd()) null else source[current]
    private fun peekNext() = if (current + 1 >= source.length) null else source[current + 1]
    private fun isAtEnd() = current >= source.length

    private fun isDigit(c: Char? = Char.MAX_VALUE) = c in '0'..'9'
    private fun isAlpha(c: Char? = Char.MAX_VALUE) = c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    private fun isAlphaNumeric(c: Char? = Char.MAX_VALUE) = isAlpha(c) || isDigit(c)
}