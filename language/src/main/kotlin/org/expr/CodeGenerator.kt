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
        addln("#include <stdlib.h>")
        addln("#include <string.h>")
        addln()
        addln("""
            |int readInt() {
            |    int t;
            |    scanf("%d", &t);
            |    return t;
            |}
        """.trimMargin())
        addln()
        addln("""
            |bool readBool() {
            |    int t;
            |    scanf("%d", &t);
            |    return t;
            |}
        """.trimMargin())
        addln()
        addln("""
            |char readChar() {
            |    char t;
            |    scanf("%c", &t);
            |    return t;
            |}
        """.trimMargin())
        addln()
        addln("""
            |long long readLong() {
            |    long long t;
            |    scanf("%lld", &t);
            |    return t;
            |}
        """.trimMargin())
        addln()
        addln("""
            |double readDouble() {
            |    double t;
            |    scanf("%lf", &t);
            |    return t;
            |}
        """.trimMargin())
        addln()
        addln("""
            |char* readLine() {
            |    static char t[256];
            |    fgets(t, 256, stdin);
            |    return t;
            |}
        """.trimMargin())
        addln()
        addln("""
            |char* readString() {
            |    static char t[256];
            |    scanf("%s", &t);
            |    return t;
            |}
        """.trimMargin())
        addln()
        addln("""
            |char* concat(char* x, char* y) {
            |    int len = strlen(x) + strlen(y) + 1;
            |    char* t = malloc(len);
            |    strcat(t, x);
            |    strcat(t, y);
            |    return t;
            |}
        """.trimMargin())
        addln()
        addln("""
            |char* assign(char* x, char* y) {
            |    free(x);
            |    return strdup(y);
            |}
        """.trimMargin())
        addln()
        addln("""
            |int* assignArray(int* x, int* y, int* x_size, int* y_size) {
            |    free(x);
            |    *x_size = *y_size;
            |    x = malloc(*y_size * sizeof(int));
            |    for (int i = 0; i < *y_size; i++) {
            |         x[i] = y[i];
            |    }
            |    return x;
            |}
        """.trimMargin())
        addln()
        addln("int main() {")
        indent.append("    ")

        parser.idToType.keys.filter { parser.idToType[it]?.startsWith("A") ?: false }.forEach {
            addln("int ${it}_size;")
        }
        addln()

        //main code
        node.accept(this)

        addln()
        parser.idToType.keys.filter {
            val type = parser.idToType[it].orEmpty()
            type.startsWith("A") || it == "S"
        }.forEach {
            addln("free($it);")
        }

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
            "S" -> add("readString()")
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
            "S" -> {
                add("\"%s\\n\", ")
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
            "S" -> {
                add("\"%s\", ")
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

    //TODO: clear code
    override fun visitAssingmnet(ctx: ExprParser.AssingmnetContext?) {
        require(ctx != null)

        visit(ctx.children[0])

        if (ctx.children[1] is ExprParser.GetContext) {
            visit(ctx.children[1])
            add(" = ")

            visit(ctx.children[3])
            addln(";")
        } else {
            add(" = ")

            if (ctx.children[2] is ExprParser.StringContext || (ctx.children[2] as ExprParser.GeneralContext).type == "S") { //is String
                if (ctx.children[2] is ExprParser.GeneralContext) { //если это не general, то "asd"
                    add("assign(")
                    visit(ctx.children[0])
                    add(", ")
                    visit(ctx.children[2])
                    add(")")
                } else { //"sdf"
                    add("strdup(")
                    visit(ctx.children[2])
                    add(")")
                }
            } else {
                if (ctx.general.type.startsWith("A")) {
                    add("assignArray(")
                    visit(ctx.children[0])
                    add(", ")
                    visit(ctx.children[2])
                    add(", &")
                    visit(ctx.children[0])
                    add("_size")
                    add(", &")
                    visit(ctx.children[2])
                    add("_size")
                    add(")")
                } else {
                    visit(ctx.children[2])
                }
            }
            addln(";")
        }
    }

    override fun visitConcat(ctx: ExprParser.ConcatContext?) {
        require(ctx != null)

        add("concat(")
        visit(ctx.children[2]) //first
        add(", ")
        visit(ctx.children[4]) //second
        add(")")
    }

    //TODO: clear code
    override fun visitDeclaration(ctx: ExprParser.DeclarationContext?) {
        require(ctx != null)

        val name = ctx.children[1].text
        val typeTemplate = parser.idToType[name].orEmpty() //TODO: what if type == null?
        val type = primitiveTypeMapper[typeTemplate] ?: typeTemplate.arrayTypeMapper()

        add(type)
        add(" ")
        add(name)

        when (ctx.children[2].text) {
            "=" -> {
                add(" = ")
                if (type == "char*") {
                    if (ctx.children[3] is TerminalNode) { //terminal
                        val string = ctx.children[3].text.drop(1).dropLast(1) //rm '"'
                        addln("malloc(${string.length} + 1);")
                        add("strcpy($name, \"$string\")")
                    } else if (ctx.children[3] is ExprParser.ConcatContext?) {
                        visit(ctx.children[3])
                    } else { //nonterminal
                        add("strdup(")
                        visit(ctx.children[3])
                        add(")")
                    }
                } else {
                    visit(ctx.children[3])
                    if (ctx.array != null) {
                        addln(";")
                        add("${name}_size = ")
                        visit(ctx.array.children[2])
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
