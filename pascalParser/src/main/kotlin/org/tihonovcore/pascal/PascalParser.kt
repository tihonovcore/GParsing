package org.tihonovcore.pascal

import org.tihonovcore.utils.Early
import java.lang.IllegalArgumentException

@Early
data class Node(val type: String, val children: List<Node> = emptyList())

@Early
class PascalParser(private val tokens: List<Token>) {
    var current = 0

    private fun get(): Token = tokens[current]
    private fun getType(): TokenType = tokens[current].type
    private fun shift() { current++ }
    private fun expected(type: TokenType) { assert(getType() == type) }

    fun parse(): Node {
        return parseFile()
    }

    private fun parseFile(): Node {
        return when (getType()) {
            TokenType.FUNCTION -> parseFunction()
            TokenType.PROCEDURE -> parseProcedure()
            else -> throw IllegalArgumentException("Unexpected token")
        }
    }

    private fun parseFunction(): Node {
        expected(TokenType.FUNCTION)
        shift()

        val signature = parseSignature()
        expected(TokenType.COLON)
        shift()

        val type = parseType()
        expected(TokenType.EOF)

        return Node("FUNCTION", listOf(signature, type))
    }

    private fun parseProcedure(): Node {
        expected(TokenType.PROCEDURE)
        shift()

        val signature = parseSignature()
        expected(TokenType.EOF)

        return Node("PROCEDURE", listOf(signature))
    }

    private fun parseSignature(): Node {
        val name = parseName()
        expected(TokenType.LBRACKET)
        shift()

        val arguments = parseArguments()
        expected(TokenType.RBRACKET)
        shift()

        return Node("SIGNATURE", listOf(name, arguments))
    }

    private fun parseName(): Node {
        expected(TokenType.STRING)

        val name = get().data as String
        shift()

        return Node("NAME", listOf(Node(name)))
    }

    private fun parseType(): Node {
        expected(TokenType.STRING)

        val name = get().data as String
        shift()

        return Node("TYPE", listOf(Node(name)))
    }

    private fun parseArguments(): Node {
        return when (getType()) {
            TokenType.STRING -> {
                val declaration = parseDeclaration()
                val argumentsSuffix = parseArgumentsSuffix()

                Node("AT LEAST 1 ARGUMENT", listOf(declaration, argumentsSuffix))
            }
            TokenType.RBRACKET -> {
                Node("0 ARGUMENT")
            }
            else -> throw IllegalArgumentException("Unexpected token")
        }
    }

    private fun parseDeclaration(): Node {
        val name = parseName()
        expected(TokenType.COLON)
        shift()

        val type = parseType()

        return Node("DECLARATION", listOf(name, type))
    }

    private fun parseArgumentsSuffix(): Node {
        return when (getType()) {
            TokenType.RBRACKET -> {
                Node("EMPTY SUFFIX")
            }
            TokenType.COMMA -> {
                expected(TokenType.COMMA)
                shift()

                val declaration = parseDeclaration()
                val suffix = parseArgumentsSuffix()

                Node("ARGUMENT'S SUFFIX", listOf(declaration, suffix))
            }
            else -> throw IllegalArgumentException("Unexpected token")
        }
    }
}
