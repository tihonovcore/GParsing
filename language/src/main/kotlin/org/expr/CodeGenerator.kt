package org.expr

import org.antlr.v4.runtime.tree.TerminalNode
import org.expr.gen.ExprBaseVisitor
import org.expr.gen.ExprParser
import java.lang.StringBuilder
import java.util.HashMap

class CodeGenerator(
    private val parser: ExprParser,
    private val node: ExprParser.FileContext
) : ExprBaseVisitor<Unit>() {
    private val result = StringBuilder()
    private val functionResult = StringBuilder()
    private var defaultOut = result
    private var indent = StringBuilder()

    private val functionGenerator = FunctionGenerator()

    private var currentVariable = ""

    private var indentFlag = true

    private fun <T> add(v: T) {
        setIndent()
        defaultOut.append(v)
    }

    private fun <T> addln(v: T) {
        setIndent()
        defaultOut.append(v).append(System.lineSeparator())
        indentFlag = true
    }

    private fun addln() { addln("") }

    private fun setIndent() {
        if (indentFlag) {
            indentFlag = false
            defaultOut.append(indent)
        }
    }

    //////////////////////////////////////////////////////

    private var head = 0
    private val parent = mutableListOf<MutableMap<String, String>>(mutableMapOf())
    private val current = mutableListOf<MutableMap<String, String>>(mutableMapOf())

    private fun getType(id: String): String {
        val myParent = parent[head]
        val myCurrent = current[head]

        if (myCurrent.containsKey(id)) {
            return myCurrent[id]!!
        }

        if (myParent.containsKey(id)) {
            return myParent[id]!!
        }

        throw IllegalArgumentException("Undefined variable: $id")
    }

    private fun setType(id: String, type: String) {
        val myCurrent = current[head]
        require(!myCurrent.containsKey(id)) { "Redefinition variable: $id" }

        myCurrent[id] = type
    }

    private fun newScope() {
        val newParent = HashMap(parent[head])
        for (key in current[head].keys) {
            newParent[key] = current[head][key]
        }

        head++
        current.add(mutableMapOf())
        parent.add(newParent)
    }

    private fun outOfScope() {
        current.removeAt(head)
        parent.removeAt(head)
        head--
    }

    private var nextDeclarationPosition = 0
    private fun addNewDeclaration() {
        val pair = parser.declarations[nextDeclarationPosition]
        nextDeclarationPosition++
        setType(pair.key, pair.value)
    }

    //////////////////////////////////////////////////////

    fun gen(): String {
        indent.append("    ")
        //main code
        node.accept(this)

        addln()
        current[head].keys.filter {
            val type = getType(it)
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

        resultWithLibraries.append(functionResult)
        resultWithLibraries.append(System.lineSeparator())

        resultWithLibraries.append("int main() {")
        resultWithLibraries.append(System.lineSeparator())

        current[head].keys.filter { getType(it).startsWith("A") }.forEach {
            resultWithLibraries.append("    int ${it}_size = 0;")
            resultWithLibraries.append(System.lineSeparator())
        }
        resultWithLibraries.append(System.lineSeparator())

        resultWithLibraries.append(result.toString())
        resultWithLibraries.append(functionGenerator.definitions)

        return resultWithLibraries.toString()
    }

    override fun visitCast(ctx: ExprParser.CastContext?) {
        require(ctx != null)

        if (ctx.typeID != null) { //cast exists
            if (ctx.type != "S") {
                add("((")
                visit(ctx.typeID)
                add(") ")
                ctx.orExpr().forEach { visit(it) }
                add(")")
            } else { //toString
                add(primitiveTypeMapper[ctx.orExpr.type])
                add("_to_string(")
                visit(ctx.orExpr)
                add(")")

                functionGenerator.castToString(primitiveTypeMapper[ctx.orExpr.type]!!)
            }
        } else {
            super.visitCast(ctx)
        }
    }

    override fun visitRead(ctx: ExprParser.ReadContext?) {
        require(ctx != null)

        visit(ctx.ID())

        if (ctx.get != null) {
            visit(ctx.get)
        }

        add(" = ")

        val type = ctx.type
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

        val type = primitiveTypeMapper[ctx.type]
        add(type ?: ctx.type.arrayTypeMapper())
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
            } else if (value is TerminalNode && value.text.startsWith("\"")) { //new constant string like "str"
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

        addNewDeclaration()

        val name = ctx.children[1].text.also { currentVariable = it }
        val typeTemplate = getType(name)
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
                    } else if (getType(name).startsWith("A")) { //copy array
                        add("assignArray($name, ").also { functionGenerator.arrayAssign(getType(name).arrayTypeMapper()) }
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

    override fun visitFunction(ctx: ExprParser.FunctionContext?) {
        require(ctx != null)

        newScope()

        defaultOut = functionResult

        val prevIndent = indent.length
        indent.clear()

        val name = ctx.ID()
        val args = ctx.functionArguments
        val body = ctx.body()
        val returnType = ctx.returnType

        visit(returnType)
        add(" ")
        visit(name)
        add("(")
        visit(args)
        add(") ")
        visit(body)

        defaultOut = result
        repeat(prevIndent) { indent.append(" ") }

        outOfScope()
    }

    override fun visitFunctionArguments(ctx: ExprParser.FunctionArgumentsContext?) {
        require(ctx != null)

        if (ctx.childCount == 0) return

        for (i in ctx.children.indices step 4) {
            val name = ctx.children[i]
            val type = ctx.children[i + 2]

            visit(type)
            add(" ")
            visit(name)

            if (type.text.startsWith("A")) {
                add(", ")
                add("int ")
                visit(name)
                add("_size")
            }

            if (i + 4 < ctx.childCount) add(", ") //has next argument

            addNewDeclaration()
        }
    }

    override fun visitReturnType(ctx: ExprParser.ReturnTypeContext?) {
        require(ctx != null)

        if (ctx.childCount == 0) add("void")
        else visit(ctx.typeID)
    }

    override fun visitReturnStatement(ctx: ExprParser.ReturnStatementContext?) {
        require(ctx != null)

        visit(ctx.RETURN())

        if (ctx.general != null) {
            add(" ")
            visit(ctx.general)
        } else {
            add(" 0") //TODO: check
        }

        addln(";")
    }

    override fun visitId_call(ctx: ExprParser.Id_callContext?) {
        super.visitId_call(ctx)
        add(";")
    }

    override fun visitCallArguments(ctx: ExprParser.CallArgumentsContext?) {
        require(ctx != null)

        if (ctx.childCount == 0) return

        ctx.children.forEach {
            if (it is TerminalNode) {
                visit(it)
                return@forEach
            }

            val type = (it as ExprParser.GeneralContext).type
            if (type.startsWith("A")) {
                functionGenerator.copyArray(type.arrayTypeMapper())

                val typeParameter = primitiveTypeMapper[type.drop(1)]
                add("copy_${typeParameter}_array(")
                visit(it)
                add(", ")
                visit(it)
                add("_size")
                add(")")
                add(", ")
                visit(it)
                add("_size")
            } else if (type == "S") {
                add("strdup(")
                visit(it)
                add(")")
            } else {
                visit(it)
            }
        }
    }

    override fun visitIfStatement(ctx: ExprParser.IfStatementContext?) {
        require(ctx != null)

        newScope()

        visit(ctx.IF())
        add(" ")
        visit(ctx.LBRACKET())
        add(" ")
        visit(ctx.general())
        add(" ")
        visit(ctx.RBRACKET())
        add(" ")
        visit(ctx.children[4]) //if body

        outOfScope()

        if (ctx.childCount == 7) { //else case exists
            newScope()

            visit(ctx.ELSE())
            add(" ")
            visit(ctx.children[6]) //else body

            outOfScope()
        }
    }

    override fun visitBody(ctx: ExprParser.BodyContext?) {
        require(ctx != null)

        visit(ctx.OpenBlockBrace())
        addln()

        indent.append("    ")
        ctx.statement().forEach { visit(it) }
        cleanMemory()
        indent.delete(indent.length - 4, indent.length)

        visit(ctx.CloseBlockBrace())
        addln()
    }

    override fun visitWhileStatement(ctx: ExprParser.WhileStatementContext?) {
        require(ctx != null)

        newScope()

        visit(ctx.WHILE())
        add(" ")
        visit(ctx.LBRACKET())
        add(" ")
        visit(ctx.general())
        add(" ")
        visit(ctx.RBRACKET())
        add(" ")
        visit(ctx.children[4])

        outOfScope()
    }

    override fun visitJumpStatement(ctx: ExprParser.JumpStatementContext?) {
        super.visitJumpStatement(ctx)
        addln(";")
    }

    private fun cleanMemory() {
        current[head].keys.filter {
            val type = getType(it)
            type.startsWith("A") || type == "S"
        }.forEach {
            addln("free($it);")
        }
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
