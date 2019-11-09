package org.tihonovcore.pascal

import org.tihonovcore.pascal.TokenType.*

data class Node(val description: String, val children: List<Node> = emptyList())

class PascalParser(private val tokens: List<Token>) {
    private var possibleTypes = setOf("integer", "string", "real", "char", "boolean")
    private var parameterNames = mutableSetOf<String>()
    private var current = 0

    private fun get(): Token = tokens[current]
    private fun getType(): TokenType = tokens[current].type
    private fun shift() { current++ }
    private fun expected(type: TokenType) {
        require(getType() == type) { "expected: $type, but was ${get()}" }
    }

    fun parse(): Node {
        current = 0
        parameterNames.clear()

        return parseFile()
    }

    private fun parseFile(): Node {
        return when (getType()) {
            FUNCTION -> parseFunction()
            PROCEDURE -> parseProcedure()
            else -> throw IllegalStateException("Expected 'function' or 'procedure', but was ${get()}")
        }.also { expected(SEMICOLON) }
    }

    private fun parseFunction(): Node {
        expected(FUNCTION)
        shift()

        val signature = parseSignature()
        expected(COLON)
        shift()

        val type = parseType()
        return Node("FUNCTION", listOf(signature, type))
    }

    private fun parseProcedure(): Node {
        expected(PROCEDURE)
        shift()

        val signature = parseSignature()
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

        require(name !in parameterNames) { "Redeclaration: parameter `$name` appeared twice" }
        parameterNames.add(name)
        return Node("NAME", listOf(Node(name)))
    }

    private fun parseType(): Node {
        expected(STRING)

        val name = get().data as String
        shift()

        require(name in possibleTypes) { "Unexpected type: $name" }
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
