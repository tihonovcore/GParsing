import org.tihonovcore.grammar.Grammar
import org.tihonovcore.grammar.Rule
import org.tihonovcore.grammar.getFIRST1
import org.tihonovcore.utils.Early
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

@Early
fun generateFiles(
    grammar: Grammar,
    path: String
) {
    with(grammar) {
        generateMyTokens(path)
        generateLexer(path)

        generateClasses(path)
        generateParser(path)
    }
}

@Early
private fun Grammar.generateMyTokens(path: String) {
    val tokenType = StringBuilder()

    fun <T> add(value: T) = tokenType.append(value)

    add("enum class MyTokenType {")
    add(System.lineSeparator())
    add("    ")

    terminals.forEach { add("$it, ") }
    add("EOF")

    add(System.lineSeparator())
    add("}")

    val token = "data class MyToken(val type: MyTokenType, val data: Any? = null)"

    ///////////////////////////////
    val file = Paths.get("$path/MyToken.kt")

    Files.write(file, tokenType.toString().toByteArray())
    newLine(file)
    newLine(file)
    Files.write(file, token.toByteArray(), StandardOpenOption.APPEND)
    newLine(file)
}

@Early
private fun Grammar.generateLexer(path: String) {
    val lexer = StringBuilder()

    fun <T> add(value: T) = lexer.append(value)

    add(
        """
       |import MyTokenType.*
       |
       |fun getMyTokens(string: String): List<MyToken> {
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
       |    val tokens = mutableListOf<MyToken>()
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
       |        tokens += MyToken(tokenType, data)
       |    }
       |    
       |    return tokens + MyToken(EOF)
       |}
       |
        """.trimMargin()
    )

    ///////////////////////////////
    val file = Paths.get("$path/Lexer.kt")
    Files.write(file, lexer.toString().toByteArray())
}

@Early
private fun Grammar.generateClasses(path: String) {
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
            synthesized[it]?.forEach { attr -> body += "    $attr\n" } //TODO: default value
            body += "}\n"
            if (body == " {\n}\n") body = ""

            classes += header + body
        }

    classes += "data class Terminal(val data: Any?) : Tree()"

    ///////////////////////////////
    val file = Paths.get("$path/Classes.kt")
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
private fun Grammar.generateParser(path: String) {
    val FIRST1 = getFIRST1()

    val parser = StringBuilder()
    val indent = StringBuilder()

    fun <T> addln(value: T) = parser.append(value).append(System.lineSeparator()).append(indent)
    fun addln() = addln("")

    addln(
        """
        |import MyTokenType.*
        |
        |class MyParser(private val tokens: List<MyToken>) { //TODO: rename
        |    private var current = 0
        |
        |    fun get() = tokens[current]
        |    fun getType() = get().type
        |
        |fun currentIn(vararg tokens: MyTokenType) = getType() in tokens
        | 
        """.trimMargin()
    )

    val graph = rules //graph[x] = y === go from `x` by rules `y`
        .filter { !it.left.startsWith("code_") }
        .groupBy { it.left }

    graph
        .filter { !it.key.startsWith("generated_") }
        .forEach { (left, _) -> addln(generateFunction(graph, FIRST1, left)) }


    addln(
        """
        |   fun parseTerminal(): Terminal {
        |       return Terminal(get().data).also { current++ }
        |   }
        """.trimMargin()
    )

    addln("}")

    ///////////////////////////////
    val file = Paths.get("$path/Parser.kt")
    Files.write(file, parser.toString().toByteArray())
    newLine(file)
}

//TODO: clear set
private val visited = mutableSetOf<String>()

//TODO: set indent
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
    val args: String

    if (left.startsWith("generated_")) {
        returnType = "List<Tree>"
        args = "$parent: ${cap(parent)}"
    } else {
        returnType = name
        args = "" //TODO: put inherited attributes
    }

    addln(
        """
           |fun parse$name($args): $returnType {
           |    ${ if (parent == "") "val $left = $name()" else "" }
           |    val children = mutableListOf<Tree>()
           |
           |    when {
            """.trimMargin()
    )

    var epsilonRuleExists = false
    val addLater = mutableListOf<String>()
    graph[left]!!.forEach { rule ->
        val tokens = convertToCode(FIRST1[rule]!!)
        addln("currentIn($tokens) -> {")

        rule.right.forEach {
            when {
                it.startsWith("code_") -> addln("   " + codeBlocks[it])
                it.startsWith("generated_") -> {
                    //NOTE: if we are `generated_` we have parent yet
                    val nextParent = if (left.startsWith("generated_")) parent else left
                    if (it !in visited) {
                        addLater += generateFunction(graph, FIRST1, it, nextParent)
                    }
                    addln("children += parse${cap(it)}($nextParent)")
                }
                it != "_" -> {
                    if (it !in terminals) {
                        val functionName = cap(it)
                        val functionArgs =  if (it.startsWith("generated_")) parent else ""
                        addln("val $it = parse$functionName($functionArgs)")
                        addln("children += $it")
                    } else {
                        addln("val $it = parseTerminal()")
                        addln("children += $it")
                    }
                }
                else -> epsilonRuleExists = true
            }
        }

        addln("}")
    }

    if (epsilonRuleExists)
        addln("else -> { /* do nothing */ }")
    else
        addln("else -> throw IllegalStateException()")

    addln("}")

    if (left.startsWith("generated_"))
        addln("    return children")
    else
        addln("    return $left.also { it.children += children }")

    addln("}")
    addln("")

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
