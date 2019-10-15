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
        fun beginOf(value: String) = string.startsWith(value, current)

        val tokens = mutableListOf<Token>()
        while (current < string.length) {
            while (get().isWhitespace()) shift()

            var value: Any? = null
            val tokenType = when (val c = get()) {
                '(' -> LBRACKET
                ')' -> RBRACKET
                ':' -> COLON
                ',' -> COMMA
                else -> {
                    when {
                        beginOf("function") -> FUNCTION
                        beginOf("procedure") -> PROCEDURE
                        c.isLetter() -> {
                            val start = current
                            while (current < string.length && get().isLetterOrDigit()) shift()
                            value = string.substring(start, current)
                            STRING
                        }
                        else -> throw IllegalStateException("Undefined symbol: $c at position $current")
                    }
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
        FUNCTION to 8,
        PROCEDURE to 9,
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
