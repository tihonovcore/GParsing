import org.tihonovcore.grammar.Grammar
import org.tihonovcore.grammar.getFIRST1
import org.tihonovcore.utils.Early
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

@Early
fun generateFiles(grammar: Grammar, lexerRules: List<Pair<String, String>>) {
    with(grammar) {
        generateTokens()
        generateLexer(lexerRules)

        generateClasses()
        generateParser()
    }
}

@Early
private fun Grammar.generateTokens() {
    val tokenType = StringBuilder()

    fun <T> add(value: T) = tokenType.append(value)

    add("enum class TokenType {")
    add(System.lineSeparator())
    add("    ")

    terminals.forEach { add("$it, ") }
    add("EOF")

    add(System.lineSeparator())
    add("}")

    val token = "data class Token(val type: TokenType, val data: Any? = null)"

    ///////////////////////////////
    val path = Paths.get("/home/tihonovcore/work/GParser/generator/src/main/kotlin/gen/Token.kt")
    Files.write(path, tokenType.toString().toByteArray())
    Files.write(path, System.lineSeparator().toByteArray(), StandardOpenOption.APPEND)
    Files.write(path, System.lineSeparator().toByteArray(), StandardOpenOption.APPEND)
    Files.write(path, token.toByteArray(), StandardOpenOption.APPEND)
    Files.write(path, System.lineSeparator().toByteArray(), StandardOpenOption.APPEND)
}

@Early
private fun Grammar.generateLexer(lexerRules: List<Pair<String, String>>) {
    val lexer = StringBuilder()
    val indent = StringBuilder()

    fun <T> add(value: T) = lexer.append(value)
    fun <T> addln(value: T) = lexer.append(value).append(System.lineSeparator()).append(indent)
    fun addln() = addln("")

    addln("import TokenType.*")
    addln()

    indent.append("    ")
    addln("fun getTokens(string: String): List<Token> {")
    addln("var current = 0")
    addln()
    addln("fun get() = string[current]")
    addln("fun shift(n: Int) { current += n }")
    addln()
    addln("var data: Any? = null")
    addln("fun shift() = shift((data as String).length)")
    addln()
    addln(
        """fun find(value: String): Boolean {
        val regex = value.toRegex()
        val match = regex.find(string, current)

        data = match?.value

        return match != null && match.range.first == current
    }"""
    )
    addln()
    addln("val tokens = mutableListOf<Token>()")
    indent.append("    ")
    addln("while (current < string.length) {")
    addln("while (get().isWhitespace()) shift(1) //TODO: remove!!")
    addln()
    indent.append("    ")
    addln("val tokenType = when {")

    lexerRules.forEach {
        add("find(${it.first})")
        add(" -> ")
        addln(it.second)
    }
    indent.delete(indent.length - 4, indent.length)
    addln("else -> throw IllegalStateException()")
    addln("}")
    addln()

    addln("shift()")
    indent.delete(indent.length - 4, indent.length)
    addln("tokens += Token(tokenType, data)")

    addln("}")
    addln()
    indent.clear()
    addln("return tokens + Token(EOF)")
    addln("}")

    ///////////////////////////////
    val path = Paths.get("/home/tihonovcore/work/GParser/generator/src/main/kotlin/gen/Lexer.kt")
    Files.write(path, lexer.toString().toByteArray())
}

@Early
private fun Grammar.generateClasses() {
    val tree =
        """
        |abstract class Tree {
        |    val children = mutableListOf<Tree>() //TODO: make list<Tree>()
        |}
        """.trimMargin()

    val classes = mutableListOf<String>()
    nonterminals
        .filter { !it.startsWith("generated_") }
        .forEach {
            classes += "class ${cap(it)} : Tree()"
        }

    classes += "data class Terminal(val data: Any?) : Tree()"

    ///////////////////////////////
    val path = Paths.get("/home/tihonovcore/work/GParser/generator/src/main/kotlin/gen/Classes.kt")
    Files.write(path, tree.toByteArray())
    Files.write(path, System.lineSeparator().toByteArray(), StandardOpenOption.APPEND)
    Files.write(path, System.lineSeparator().toByteArray(), StandardOpenOption.APPEND)
    classes.forEach {
        Files.write(path, it.toByteArray(), StandardOpenOption.APPEND)
        Files.write(path, System.lineSeparator().toByteArray(), StandardOpenOption.APPEND)
        Files.write(path, System.lineSeparator().toByteArray(), StandardOpenOption.APPEND)
    }
}

@Early
private fun Grammar.generateParser() {
    val FIRST1 = getFIRST1()

    val parser = StringBuilder()
    val indent = StringBuilder()

    fun <T> add(value: T) = parser.append(value)
    fun <T> addln(value: T) = parser.append(value).append(System.lineSeparator()).append(indent)
    fun addln() = addln("")

    //TODO: add to `parser` get/getType/etc
    addln("import TokenType.*")
    addln()
    addln(
        """
        |class MyParser(private val tokens: List<Token>) { //TODO: rename
        |    private var current = 0
        |
        |    fun get() = tokens[current]
        |    fun getType() = get().type
        |
        |fun currentIn(vararg tokens: TokenType) = getType() in tokens 
        """.trimMargin()
    )
    addln()

    rules.groupBy { it.left }.forEach { (left, list) ->
        val name = cap(left)
        val returnType = if (left.startsWith("generated_")) "List<Tree>" else name

        indent.append("    ")
        addln("fun parse$name(): $returnType {")
        addln("val result = mutableListOf<Tree>()")
        addln()
        indent.append("    ")
        addln("when {")

        var epsilonRuleExists = false
        list.forEach { rule ->
            val tokens = convertToCode(FIRST1[rule]!!)
            addln("currentIn($tokens) -> {")
            rule.right.forEach {
                if (it != "_") {
                    if (it !in terminals)
                        addln("    result += parse${cap(it)}()")
                    else
                        addln("    result += parseTerminal()")
                } else {
                    epsilonRuleExists = true
                }
            }
            addln("}")
        }
        indent.delete(indent.length - 4, indent.length)

        if (epsilonRuleExists)
            addln("else -> { /* do nothing */ }")
        else
            addln("else -> throw IllegalStateException()")


        addln("}") //when
        addln()
        indent.clear()

        if (name.startsWith("generated_", true)) addln("return result")
        else addln("return $name().also { it.children += result }")

        addln("}") //fun
        addln()
    }

    addln(
        """
        |   fun parseTerminal(): List<Tree> {
        |       return listOf(Terminal(get().data)).also { current++ }
        |   }
        """.trimMargin()
    )

    addln("}") //class

    val path = Paths.get("/home/tihonovcore/work/GParser/generator/src/main/kotlin/gen/Parser.kt")
    Files.write(path, parser.toString().toByteArray())
    Files.write(path, System.lineSeparator().toByteArray(), StandardOpenOption.APPEND)
}

@Early
fun cap(old: String) = old.first().toUpperCase() + old.drop(1)

@Early
fun convertToCode(list: List<String>): String {
    val result = StringBuilder()

    list.forEachIndexed { index, s ->
        if (s != "$") result.append(s)
        else result.append("EOF")

        if (index + 1 != list.size) {
            result.append(", ")
        }
    }

    return result.toString()
}
