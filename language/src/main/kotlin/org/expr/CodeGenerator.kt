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
        addln("""
            |char readChar() {
            |   char t;
            |   scanf("%c", &t);
            |   return t;
            |}
        """.trimMargin())
        addln()
        addln("""
            |long long readLong() {
            |   long long t;
            |   scanf("%lld", &t);
            |   return t;
            |}
        """.trimMargin())
        addln()
        addln("""
            |double readDouble() {
            |   double t;
            |   scanf("%lf", &t);
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

        visit(ctx.ID())
        add(" = ")
        when (parser.idToType[ctx.ID().text]) {
            "I" -> add("readInt()")
            "B" -> add("readBool()")
            "C" -> add("readChar()")
            "L" -> add("readLong()")
            "D" -> add("readDouble()")
        }
        addln(";")
    }

    override fun visitReadWithType(ctx: ExprParser.ReadWithTypeContext?) {
        require(ctx != null)
        super.visitReadWithType(ctx)
        add("()")
    }

    override fun visitPrintln(ctx: ExprParser.PrintlnContext?) {
        require(ctx != null)

        add("printf(")
        when (ctx.general().type) {
            "I" -> {
                add("\"%d\\n\", ")
                visit(ctx.general())
            }
            "B" -> {
                add("(")
                visit(ctx.general())
                add(") ? \"true\\n\" : \"false\\n\"")
            }
            "C" -> {
                add("\"%c\\n\", ")
                visit(ctx.general())
            }
            "D" -> {
                add("\"%lf\\n\", ")
                visit(ctx.general())
            }
            "L" -> {
                add("\"%lld\\n\", ")
                visit(ctx.general())
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
            "C" -> {
                add("\"%c\", ")
                visit(ctx.general())
            }
            "D" -> {
                add("\"%lf\", ")
                visit(ctx.general())
            }
            "L" -> {
                add("\"%lld\", ")
                visit(ctx.general())
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
        val type = primitiveTypeMapper[parser.idToType[name]]

        add(type)
        add(" ")
        add(name)

        when (ctx.children[2].text) {
            "=" -> {
                add(" = ")
                visit(ctx.children[3])
                addln(";")
            }
            ":" -> {
                addln(";")
            }
        }
    }

    private val primitiveTypeMapper = mapOf(
        "I" to "int",
        "B" to "bool",
        "D" to "double",
        "L" to "long long",
        "C" to "char"
    )
}
