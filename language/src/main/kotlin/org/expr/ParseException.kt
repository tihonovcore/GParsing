package org.expr

import org.tihonovcore.utils.Early
import java.lang.IllegalStateException

@Early
class GParseException(
    private val myMessage: String,
    private val line: Int,
    private val position: Int,
    private val source: String
) : IllegalStateException() {
    override val message: String?
        get() =
            """
            |$myMessage 
            |in line $line, position $position
            |${getSourceLine()}
            |${getPointer()}
            """.trimMargin()

    private fun getSourceLine(): String {
        return source.split(System.lineSeparator())[line - 1]
    }

    private fun getPointer(): String {
        return " ".repeat(position) + "^"
    }
}
