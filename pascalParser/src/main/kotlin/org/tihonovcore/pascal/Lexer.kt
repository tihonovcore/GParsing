package org.tihonovcore.pascal

import org.tihonovcore.utils.Early

import java.lang.IllegalArgumentException
import java.lang.StringBuilder

@Early
class Lexer {
    fun getTokens(string: String): List<Token> {
        var current = 0

        fun get() = string[current]
        fun shift() {
            current++
        }

        fun shift(n: Int) {
            current += n
        }

        fun beginOf(value: String) = string.substring(current).startsWith(value)

        val tokens = mutableListOf<Token>()
        while (current < string.length) {
            while (get().isWhitespace()) shift()

            var value: Any? = null
            val tokenType = when (val c = get()) {
                '(' -> TokenType.LBRACKET.also { shift() }
                ')' -> TokenType.RBRACKET.also { shift() }
                ':' -> TokenType.COLON.also { shift() }
                ',' -> TokenType.COMMA.also { shift() }
                else -> {
                    if (beginOf("function")) {
                        shift(8)
                        TokenType.FUNCTION
                    } else if (beginOf("procedure")) {
                        shift(9)
                        TokenType.PROCEDURE
                    } else if (c.isLetterOrDigit()) {
                        val start = current
                        while (current < string.length && get().isLetterOrDigit()) shift()
                        value = string.substring(start, current)
                        TokenType.STRING
                    } else {
                        throw IllegalArgumentException("Undefined symbol: $c at")
                    }
                }
            }

            tokens += Token(tokenType, value)
        }

        return tokens + Token(TokenType.EOF)
    }
}

@Early
fun List<Token>.render(): String {
    val result = StringBuilder()
    for (t in this) {
        result.append(t.type)
        if (t.data != null) result.append("<${t.data}>")
        result.append(" ")
    }
    return result.toString()
}
