package org.tihonovcore.pascal

import org.tihonovcore.pascal.TokenType.*
import org.tihonovcore.utils.Early

data class Node(val description: String, val children: List<Node> = emptyList())

@Early
class PascalParser(private val tokens: List<Token>) {
    private var current = 0

    private fun get(): Token = tokens[current]
    private fun getType(): TokenType = tokens[current].type
    private fun shift() { current++ }
    private fun expected(type: TokenType) { require(getType() == type) { "expected: $type, but was ${get()}" } }

    fun parse(): Node {
        return parseFile()
    }

    private fun parseFile(): Node {
        return when (getType()) {
            FUNCTION -> parseFunction()
            PROCEDURE -> parseProcedure()
            else -> throw IllegalStateException("Expected 'function' or 'procedure', but was ${get()}")
        }
    }

    private fun parseFunction(): Node {
        expected(FUNCTION)
        shift()

        val signature = parseSignature()
        expected(COLON)
        shift()

        val type = parseType()
        expected(EOF)

        return Node("FUNCTION", listOf(signature, type))
    }

    private fun parseProcedure(): Node {
        expected(PROCEDURE)
        shift()

        val signature = parseSignature()
        expected(EOF)

        return Node("PROCEDURE", listOf(signature))
    }

    private fun parseSignature(): Node {
        val name = parseName()
        expected(LBRACKET)
        shift()

        val arguments = parseArguments()
        expected(RBRACKET)
        shift()

        return Node("SIGNATURE", listOf(name, arguments))
    }

    private fun parseName(): Node {
        expected(STRING)

        val name = get().data as String
        shift()

        return Node("NAME", listOf(Node(name)))
    }

    private fun parseType(): Node {
        expected(STRING)

        val name = get().data as String
        shift()

        return Node("TYPE", listOf(Node(name)))
    }

    private fun parseArguments(): Node {
        return when (getType()) {
            STRING -> {
                val declaration = parseDeclaration()
                val argumentsSuffix = parseArgumentsSuffix()

                Node("AT LEAST 1 ARGUMENT", listOf(declaration, argumentsSuffix))
            }
            RBRACKET -> {
                Node("ZERO ARGUMENTS")
            }
            else -> throw IllegalStateException("Expected ')' or name of argument, but was ${get()}")
        }
    }

    private fun parseDeclaration(): Node {
        val name = parseName()
        expected(COLON)
        shift()

        val type = parseType()

        return Node("DECLARATION", listOf(name, type))
    }

    private fun parseArgumentsSuffix(): Node {
        return when (getType()) {
            RBRACKET -> {
                Node("EMPTY SUFFIX")
            }
            COMMA -> {
                expected(COMMA)
                shift()

                val declaration = parseDeclaration()
                val suffix = parseArgumentsSuffix()

                Node("ARGUMENT'S SUFFIX", listOf(declaration, suffix))
            }
            else -> throw IllegalStateException("Expected ')' or ',' and other arguments, but was ${get()}")
        }
    }
}
