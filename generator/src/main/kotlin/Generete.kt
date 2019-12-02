import org.tihonovcore.grammar.Grammar
import org.tihonovcore.grammar.Rule
import org.tihonovcore.grammar.getFIRST1
import org.tihonovcore.utils.Early
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/**
 * Creates files with lexer, tokens, parsers and classes
 *
 * [packageName] - prefix for lexer and parser name
 */
@Early
fun generateFiles(
    grammar: Grammar,
    path: String,
    packageName: String
) {
    visited.clear()

    with(grammar) {
        generateTokens(path, packageName)
        generateLexer(path, packageName)

        generateClasses(path, packageName)
        generateParser(path, packageName)
    }
}

@Early
private fun Grammar.generateTokens(path: String, packageName: String) {
    val tokenType = StringBuilder()

    fun <T> add(value: T) = tokenType.append(value)

    add("enum class ${packageName}TokenType {")
    add(System.lineSeparator())
    add("    ")

    terminals.forEach { add("$it, ") }
    add("EOF")

    add(System.lineSeparator())
    add("}")

    val token = "data class ${packageName}Token(val type: ${packageName}TokenType, val data: Any? = null)"

    ///////////////////////////////
    val file = Paths.get("$path/$packageName/${packageName}Token.kt")
    Files.write(file, tokenType.toString().toByteArray())
    newLine(file)
    newLine(file)
    Files.write(file, token.toByteArray(), StandardOpenOption.APPEND)
    newLine(file)
}

@Early
private fun Grammar.generateLexer(path: String, packageName: String) {
    val lexer = StringBuilder()

    fun <T> add(value: T) = lexer.append(value)

    add(
        """
       |import ${packageName}TokenType.*
       |
       |fun get${packageName}Tokens(string: String): List<${packageName}Token> {
       |    var current = 0
       |    
       |    fun get() = string[current]
       |    fun shift(n: Int) { current += n }
       |    
       |    var data: Any? = null
       |    fun shift() = shift((data as String).length)
       |    
       |    fun find(value: String): Boolean {
       |        val regex = value.toRegex()
       |        val match = regex.find(string, current)
       |
       |        data = match?.value
       |
       |        return match != null && match.range.first == current
       |    }
       |        
       |    val tokens = mutableListOf<${packageName}Token>()
       |    while (current < string.length) {
       |        while (get().isWhitespace()) shift(1) //TODO: remove!!
       |
       |        val tokenType = when {
       |
        """.trimMargin()
    )

    lexerRules.forEach {
        add("            find(${it.first}) -> ${it.second}")
        add(System.lineSeparator())
    }

    add(
        """
       |            else -> throw IllegalStateException("Unexpected input: position ${'$'}current")
       |        }
       |        
       |        shift()
       |        tokens += ${packageName}Token(tokenType, data)
       |    }
       |    
       |    return tokens + ${packageName}Token(EOF)
       |}
       |
        """.trimMargin()
    )

    ///////////////////////////////
    val file = Paths.get("$path/$packageName/${packageName}Lexer.kt")
    Files.write(file, lexer.toString().toByteArray())
}

@Early
private fun Grammar.generateClasses(path: String, packageName: String) {
    val tree =
        """
        |abstract class Tree {
        |    val children = mutableListOf<Tree>() //TODO: make list<Tree>()
        |}
        """.trimMargin()

    val classes = mutableListOf<String>()
    nonterminals
        .filter { !it.startsWith("generated_") }
        .filter { !it.startsWith("code_") }
        .forEach {
            val header = "class ${cap(it)} : Tree()"

            var body = " {\n"
            synthesized[it]?.forEach { attr ->
                body += "    "
                body += when (attr.split(":")[1]) {
                    "Boolean" -> "var $attr = false"
                    "Int" -> "var $attr = 0" //TODO: support other primitives
                    else -> "lateinit var $attr"
                }
                body += System.lineSeparator()
            }
            body += "}\n"
            if (body == " {\n}\n") body = ""

            classes += header + body
        }

    classes += "data class Terminal(val data: Any?) : Tree()"

    ///////////////////////////////
    val file = Paths.get("$path/$packageName/${packageName}Classes.kt")
    Files.write(file, tree.toByteArray())
    newLine(file)
    newLine(file)
    classes.forEach {
        Files.write(file, it.toByteArray(), StandardOpenOption.APPEND)
        newLine(file)
        newLine(file)
    }
}

@Early
private fun Grammar.generateParser(path: String, packageName: String) {
    val FIRST1 = getFIRST1()

    val parser = StringBuilder()
    fun <T> addln(value: T) = parser.append(value).append(System.lineSeparator())

    addln(
        """
        |import ${packageName}TokenType.*
        |
        |class ${packageName}Parser(private val tokens: List<${packageName}Token>) {
        |    private var current = 0
        |
        |    private fun get() = tokens[current]
        |    private fun getType() = get().type
        |
        |    private fun expected(token: ${packageName}TokenType) {
        |        require(token == getType()) { "Expected ${'$'}token, but was ${'$'}{getType()}" }
        |    }
        |
        |    private fun currentIn(vararg tokens: ${packageName}TokenType) = getType() in tokens
        |
        """.trimMargin()
    )

    val graph = rules //graph[x] = y === go from `x` by rules `y`
        .filter { !it.left.startsWith("code_") }
        .groupBy { it.left }

    addln(
        """
       |    fun parse(): Tree {
       |        return parse${cap(graph.keys.first { !it.startsWith("generated_") }) }()
       |    }
       |
        """.trimMargin()
    )

    graph
        .filter { !it.key.startsWith("generated_") }
        .forEach { (left, _) -> addln(generateFunction(graph, FIRST1, left)) }


    addln(
        """
        |    fun parseTerminal(): Terminal {
        |        return Terminal(get().data).also { current++ }
        |    }
        """.trimMargin()
    )

    addln("}")

    ///////////////////////////////
    val file = Paths.get("$path/$packageName/${packageName}Parser.kt")
    Files.write(file, parser.toString().toByteArray())
    newLine(file)
}

private val visited = mutableSetOf<String>()

private fun Grammar.generateFunction(
    graph: Map<String, List<Rule>>,
    FIRST1: Map<Rule, List<String>>,
    left: String,
    parent: String = ""
): String {
    if (left in visited) return ""
    visited += left

    val result = StringBuilder()
    fun addln(value: String) = result.append(value).append(System.lineSeparator())

    val name = cap(left)
    val returnType: String
    var args: String

    if (left.startsWith("generated_")) {
        returnType = "List<Tree>"
        args = "$parent: ${cap(parent)}"
    } else {
        returnType = name
        args = ""
    }

    inherited[if (parent == "") left else parent]?.forEach {
        if (args != "") args += ", "
        args += "${it.first}: ${it.second}"
    }

    addln(
        """
           |    fun parse$name($args): $returnType {
           |        ${if (parent == "") "val $left = $name()" else ""}
           |        val children = mutableListOf<Tree>()
           |
           |        when {
            """.trimMargin()
    )

    val addLater = mutableListOf<String>()
    graph[left]!!.forEach { rule ->
        val tokens = convertToCode(FIRST1[rule]!!)
        addln("            currentIn($tokens) -> {")

        rule.right.forEach {
            when {
                it.startsWith("code_") -> {
                    if (!codeBlocks[it].isNullOrEmpty()) {
                        addln("   " + codeBlocks[it])
                    }
                }
                it.startsWith("generated_") -> {
                    //NOTE: if we are `generated_` we have parent yet
                    var nextParent = if (left.startsWith("generated_")) parent else left
                    if (it !in visited) {
                        addLater += generateFunction(graph, FIRST1, it, nextParent)
                    }

                    inherited[nextParent]?.forEach { attr ->
                        nextParent += ", ${attr.first}"
                    }

                    addln("                children += parse${cap(it)}($nextParent)")
                }
                it != "_" -> {
                    if (it !in terminals) {
                        val functionName = cap(it)
                        var functionArgs = if (it.startsWith("generated_")) parent else ""

                        if (it in inherited.keys) {
                            if (functionArgs.isNotEmpty()) functionArgs += ", "
                            functionArgs += rule.calls.first() //TODO: remove first after using
                        }

                        addln(
                            """
                           |                val $it = parse$functionName($functionArgs)
                           |                children += $it
                            """.trimMargin()
                        )
                    } else {
                        addln(
                            """
                           |                expected($it)
                           |                val $it = parseTerminal()
                           |                children += $it
                            """.trimMargin()
                        )
                    }
                }
            }
        }

        addln("            }")
    }

    val returnValue =
        if (left.startsWith("generated_")) "children"
        else "$left.also { it.children += children }"

    addln(
        """
       |            else -> throw IllegalStateException("Unexpected token: ${'$'}{getType()}")
       |        }
       |
       |        return $returnValue
       |    }
       |
        """.trimMargin()
    )

    addLater.forEach { result.append(it) }
    return result.toString()
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

@Early
fun newLine(file: Path) = Files.write(file, System.lineSeparator().toByteArray(), StandardOpenOption.APPEND)
