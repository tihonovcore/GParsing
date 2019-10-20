package org.expr

import org.antlr.v4.runtime.tree.TerminalNode
import org.expr.gen.ExprBaseVisitor
import org.expr.gen.ExprParser
import java.lang.StringBuilder

class CodeGenerator(
    private val parser: ExprParser,
    private val node: ExprParser.StatementContext
) : ExprBaseVisitor<Unit>() {
    private val result = StringBuilder()
    private var indent = StringBuilder()

    private var indentFlag = true

    private fun <T> add(v: T) {
        setIndent()
        result.append(v)
    }

    private fun <T> addln(v: T) {
        setIndent()
        result.append(v).append(System.lineSeparator())
        indentFlag = true
    }

    private fun addln() { addln("") }

    private fun setIndent() {
        if (indentFlag) {
            indentFlag = false
            result.append(indent)
        }
    }

    fun gen(): String {
        addln("#include <stdio.h>")
        addln("#include <stdbool.h>")
        addln()
        addln("""
            |int readInt() {
            |   int t;
            |   scanf("%d", &t);
            |   return t;
            |}
        """.trimMargin())
        addln()
        addln("""
            |bool readBool() {
            |   int t;
            |   scanf("%d", &t);
            |   return t;
            |}
        """.trimMargin())
        addln()
        addln("int main() {")

        indent.append("    ")
        node.accept(this)
        indent = StringBuilder(indent.dropLast(4))

        addln("}")

        return result.toString()
    }

    override fun visitRead(ctx: ExprParser.ReadContext?) {
        require(ctx != null)

        when (ExprParser.idToType[ctx.ID().text]) {
            "I" -> {
                add("scanf(\"%d\", &")
                visit(ctx.ID())
                addln(");")
            }
            "B" -> {
                val name = ctx.ID().text + "_"
                add("{ int $name; scanf(\"%d\", &$name); ")
                visit(ctx.ID())
                addln(" = $name; }")
            }
        }
    }

    override fun visitReadWithType(ctx: ExprParser.ReadWithTypeContext?) {
        super.visitReadWithType(ctx)
        add("()")
    }

    override fun visitPrintln(ctx: ExprParser.PrintlnContext?) {
        require(ctx != null)

        add("printf(")
        when (ctx.general().type) {
            "I" -> {
                add("\"%d\n\", ")
                visit(ctx.general())
            }
            "B" -> {
                add("(")
                visit(ctx.general())
                add(") ? \"true\" : \"false\"\n")
            }
        }
        addln(");")

    }

    override fun visitPrint(ctx: ExprParser.PrintContext?) {
        require(ctx != null)

        add("printf(")
        when (ctx.general().type) {
            "I" -> {
                add("\"%d\", ")
                visit(ctx.general())
            }
            "B" -> {
                add("(")
                visit(ctx.general())
                add(") ? \"true\" : \"false\"")
            }
        }
        addln(");")
    }

    //HACK
    override fun visitTerminal(node: TerminalNode?) {
        if (node?.text != ";") add(node!!.text)
    }

    override fun visitTypeID(ctx: ExprParser.TypeIDContext?) {
        require(ctx != null)

        add(primitiveTypeMapper[ctx.type])
    }

    override fun visitAssingmnet(ctx: ExprParser.AssingmnetContext?) {
        require(ctx != null)

        val left = ctx.children[0].text //TODO: visit
        val right = ctx.children[2].text //TODO: visit

        add(left)
        add(" = ")
        add(right)
        addln(";")
    }

    override fun visitDeclaration(ctx: ExprParser.DeclarationContext?) {
        require(ctx != null)

        val name = ctx.children[1].text
        val type = primitiveTypeMapper[ExprParser.idToType[name]]

        add(type)
        add(" ")
        add(name)

        when (ctx.children[2].text) {
            "=" -> {
                val value = ctx.children[3].text
                add(" = ")
                add(value)
                addln(";")
            }
            ":" -> {
                addln(";")
            }
        }
    }

    private val primitiveTypeMapper = mapOf(
        "I" to "int",
        "B" to "bool"
    )
}
