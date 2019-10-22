package org.tihonovcore.pascal

import org.tihonovcore.pascal.TokenType.*
import org.tihonovcore.utils.Early

import java.lang.StringBuilder

@Early
class Lexer {
    @Early
    fun getTokens(string: String): List<Token> {
        var current = 0

        fun get() = string[current]

        fun shift(n: Int = 1) { current += n }
        fun shift(type: TokenType) { shift(tokenLength[type]!!) }

        fun Char.isLatinLetter() = this in 'a'..'z' || this in 'A'..'Z'
        fun Char.isDigitOrLatinLetter() = this.isDigit() || this.isLatinLetter()

        val tokens = mutableListOf<Token>()
        while (current < string.length) {
            while (get().isWhitespace()) shift()

            var value: Any? = null
            val tokenType = when (val c = get()) {
                '(' -> LBRACKET
                ')' -> RBRACKET
                ':' -> COLON
                ',' -> COMMA
                ';' -> SEMICOLON
                else -> {
                    if (c.isLatinLetter()) {
                        val start = current
                        while (current < string.length && get().isDigitOrLatinLetter()) shift()
                        value = string.substring(start, current)

                        when (value) {
                            "function" -> FUNCTION
                            "procedure" -> PROCEDURE
                            else -> STRING
                        }
                    }
                    else throw IllegalStateException("Undefined symbol: $c at position $current")
                }
            }

            shift(tokenType)
            tokens += Token(tokenType, value)
        }

        return tokens + Token(EOF)
    }

    @Early
    private val tokenLength = mapOf(
        LBRACKET to 1,
        RBRACKET to 1,
        COLON to 1,
        COMMA to 1,
        SEMICOLON to 1,
        FUNCTION to 0,
        PROCEDURE to 0,
        STRING to 0
    )
}

fun List<Token>.render(): String {
    val result = StringBuilder()
    for (t in this) {
        result.append(t.type)
        if (t.data != null) result.append("<${t.data}>")
        result.append(" ")
    }
    return result.toString()
}
