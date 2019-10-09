package org.tihonovcore.pascal

import java.lang.StringBuilder

class RenderVisitor(private val node: Node) {
    private var indent = 0
    private val result = StringBuilder()

    fun visit(): String {
        result.clear()
        visit(node)
        return result.toString()
    }

    private fun visit(node: Node) {
        indentRender()
        render(node.type)
        render(System.lineSeparator())
        upIndent()
        node.children.forEach { visit(it) }
        downIndent()
    }

    private fun indentRender() {
        repeat(indent / 4) { result.append("|   ") }
        result.append("@")
    }

    private fun <T> render(value: T) {
        result.append(value)
    }

    private fun upIndent() { indent += 4 }
    private fun downIndent() { indent -= 4 }
}