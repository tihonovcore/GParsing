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

    private val functionGenerator = FunctionGenerator()

    private var currentVariable = ""

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
        addln("int main() {")
        indent.append("    ")

        parser.idToType.keys.filter { parser.idToType[it]?.startsWith("A") ?: false }.forEach {
            addln("int ${it}_size = 0;")
        }
        addln()

        //main code
        node.accept(this)

        addln()
        parser.idToType.keys.filter {
            val type = parser.idToType[it].orEmpty()
            type.startsWith("A") || type == "S"
        }.forEach {
            addln("free($it);")
        }

        indent = StringBuilder(indent.dropLast(4))

        addln("}")

        val resultWithLibraries = StringBuilder()
        resultWithLibraries.append("#include <stdio.h>").append(System.lineSeparator())
        resultWithLibraries.append("#include <stdbool.h>").append(System.lineSeparator())
        resultWithLibraries.append("#include <stdlib.h>").append(System.lineSeparator())
        resultWithLibraries.append("#include <string.h>").append(System.lineSeparator())

        resultWithLibraries.append(System.lineSeparator())
        functionGenerator.prototypes.forEach {
            resultWithLibraries.append(it).append(System.lineSeparator())
        }
        resultWithLibraries.append(System.lineSeparator())

        resultWithLibraries.append(result.toString())
        resultWithLibraries.append(functionGenerator.definitions)

        return resultWithLibraries.toString()
    }

    override fun visitRead(ctx: ExprParser.ReadContext?) {
        require(ctx != null)

        visit(ctx.ID())
        add(" = ")

        val type = parser.idToType[ctx.ID().text]
        when (type) {
            "I" -> add("readInt()")
            "B" -> add("readBool()")
            "C" -> add("readChar()")
            "L" -> add("readLong()")
            "D" -> add("readDouble()")
            "S" -> add("readString()")
        }

        if (type != "S") functionGenerator.simpleRead(primitiveTypeMapper[type]!!)
        else functionGenerator.stringRead(FunctionGenerator.StringType.STRING)

        addln(";")
    }

    override fun visitReadWithType(ctx: ExprParser.ReadWithTypeContext?) {
        require(ctx != null)
        super.visitReadWithType(ctx)
        add("()")

        val type = ctx.children.single().text.drop(4)
        when (type) {
            "Line" -> functionGenerator.stringRead(FunctionGenerator.StringType.LINE)
            "String" -> functionGenerator.stringRead(FunctionGenerator.StringType.STRING)
            else -> functionGenerator.simpleRead(primitiveTypeMapper[ctx.type]!!)
        }
    }

    private fun commonPrint(type: String, array: String = "", nl: String, visit: () -> Unit) {
        if (type.startsWith("A")) {
            val typeParameter = type.drop(1)
            val template = when(typeParameter) {
                "I" -> "\"%d\""
                "B" -> "\"%d\""
                "C" -> "\"%c\""
                "D" -> "%lf\""
                "L" -> "%lld\""
                else -> throw IllegalStateException("CODEGEN: unexpected type - $typeParameter")
            }

            addln("printf(\"[\");")
            addln("for (int i = 0; i < ${array}_size; i++) {")

            if (typeParameter != "B") addln("    printf($template, $array[i]);")
            else addln("    printf($template, $array[i] ? \"true\" : \"false\");")
            addln("    if (i + 1 != ${array}_size) printf(\", \");")
            addln("}")
            addln("printf(\"]$nl\");")
        } else {
            add("printf(")
            when (type) {
                "I" -> {
                    add("\"%d$nl\", ")
                    visit()
                }
                "B" -> {
                    add("(")
                    visit()
                    add(") ? \"true$nl\" : \"false$nl\"")
                }
                "C" -> {
                    add("\"%c$nl\", ")
                    visit()
                }
                "D" -> {
                    add("\"%lf$nl\", ")
                    visit()
                }
                "L" -> {
                    add("\"%lld$nl\", ")
                    visit()
                }
                "S" -> {
                    add("\"%s$nl\", ")
                    visit()
                }
            }
            addln(");")
        }
    }

    override fun visitPrintln(ctx: ExprParser.PrintlnContext?) {
        require(ctx != null)

        fun visit() { visit(ctx.general()) }
        commonPrint(ctx.general().type, ctx.general().text, "\\n", ::visit)
    }

    override fun visitPrint(ctx: ExprParser.PrintContext?) {
        require(ctx != null)

        fun visit() { visit(ctx.general()) }
        commonPrint(ctx.general().type, ctx.general().text, "", ::visit)
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

        val variable = ctx.children[0].also { currentVariable = it.text }
        visit(variable)

        if (ctx.children[1] is ExprParser.GetContext) {
            visit(ctx.children[1])
            add(" = ")

            visit(ctx.children[3])
            addln(";")
        } else {
            add(" = ")

            val value = ctx.children[2]
            if (value is ExprParser.GeneralContext && value.type == "S") { //variable with type String
                add("assign(").also { functionGenerator.stringAssign() }
                visit(variable)
                add(", ")
                visit(value)
                add(")")
            } else if (value is ExprParser.StringContext) { //new constant string like "str"
                add("strdup(")
                visit(value)
                add(")")
            } else if (value.text.startsWith("concat")) { //x = concat(a, b); //TODO: rm dirty hack
                visit(value)
            } else if (ctx.general.type.startsWith("A")) { //array
                add("assignArray(").also { functionGenerator.arrayAssign(ctx.general.type.arrayTypeMapper()) }
                visit(variable)
                add(", ")
                visit(value)
                add(", &")
                visit(variable)
                add("_size")
                add(", &")
                visit(value)
                add("_size")
                add(")")
            } else { //expression
                visit(value)
            }
            addln(";")
        }
    }

    override fun visitConcat(ctx: ExprParser.ConcatContext?) {
        require(ctx != null)

        val left = ctx.children[2]
        val ltype = ctx.ltype
        val right = ctx.children[4]
        val rtype = ctx.rtype

        if (ltype == rtype && ltype == "S") {
            add("concat(").also { functionGenerator.stringConcat() }
            visit(left)
            add(", ")
            visit(right)
            add(")")
        } else if (ltype == rtype) { //arrays
            add("concat(").also { functionGenerator.arrayConcat(ltype.arrayTypeMapper()) }
            visit(left)
            add(", ")
            visit(right)
            add(", $currentVariable")
            add(", ")
            visit(left)
            add("_size, ")
            visit(right)
            add("_size, &")
            add(currentVariable)
            add("_size")
            add(")")
        } else {
            //TODO: s + c, c + s, at + t, t + at
        }
    }

    override fun visitDeclaration(ctx: ExprParser.DeclarationContext?) {
        require(ctx != null)

        val name = ctx.children[1].text.also { currentVariable = it }
        val typeTemplate = parser.idToType[name].orEmpty() //TODO: what if type == null?
        val type = primitiveTypeMapper[typeTemplate] ?: typeTemplate.arrayTypeMapper()

        add(type)
        add(" ")
        add(name)

        val action = ctx.children[2].text
        val value = ctx.children[3]
        when (action) {
            "=" -> {
                add(" = ")
                if (type == "char*") { //string
                    if (value is TerminalNode) { //terminal
                        val string = value.text.drop(1).dropLast(1) //rm '"'
                        addln("malloc(${string.length} + 1);")
                        add("strcpy($name, \"$string\")")
                    } else if (value.text.startsWith("concat")) { //def x = concat(a, b); //TODO: rm dirty hack
                        visit(value)
                    } else { //nonterminal
                        add("strdup(")
                        visit(value)
                        add(")")
                    }
                } else {
                    if (ctx.array != null) { //new array
                        visit(value)
                        addln(";")
                        add("${name}_size = ")
                        visit(ctx.array.children[2])
                    } else if (value.text.startsWith("concat")) { //def x = concat(a, b); //TODO: rm dirty hack
                        visit(value)
                    } else if (parser.idToType[name]!!.startsWith("A")) { //copy array
                        add("assignArray($name, ").also { functionGenerator.arrayAssign(parser.idToType[name]!!.arrayTypeMapper()) }
                        visit(value)
                        add(", &${name}_size, &")
                        visit(value)
                        add("_size)")
                    } else { //value is not array
                        visit(value)
                    }
                }
                addln(";")
            }
            ":" -> {
                addln(";")
            }
        }
    }

    override fun visitArray(ctx: ExprParser.ArrayContext?) {
        require(ctx != null)

        val type = primitiveTypeMapper[ctx.type.drop(1)]
        add("malloc(")
        add("sizeof($type)")
        add(" * ")
        visit(ctx.children[2])
        add(")")
    }

    private val primitiveTypeMapper = mapOf(
        "I" to "int",
        "B" to "bool",
        "D" to "double",
        "L" to "long long",
        "C" to "char",
        "S" to "char*"
    )

    private fun String.arrayTypeMapper(): String {
        val type = primitiveTypeMapper[this.drop(1)]!! //TODO: что если это многомерный массив
        return "$type*"
    }
}
