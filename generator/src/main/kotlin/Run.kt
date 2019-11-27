import gen.GrammarLexer
import gen.GrammarParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.tihonovcore.grammar.*
import org.tihonovcore.utils.Early
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Paths

@Early
fun main() {
//    val source = readFile("simpleLang") //not ll1
//    val source = readFile("myGrammar") //ll1
    val source = readFile("calculator") //ll1
//    val source = readFile("funcDeclr") //ll1

    val lexer = GrammarLexer(CharStreams.fromString(source))
    val tokens = CommonTokenStream(lexer)
    val parser = GrammarParser(tokens)

    val generator = GrammarGenerator()
    generator.visit(parser.file())

    print("Nonterminals: ")
    println(generator.nonterminals.toList())

    print("Terminals: ")
    println(generator.terminals.toList())

    println("Rules: ")
    generator.rules.forEach { println(it) }


    val grammar = Grammar(
        generator.nonterminals.toList(),
        generator.terminals.toList(),
        generator.rules
    )


    println()
    println()
    println()
    println(detailCheckLL1(grammar))
    println()
    println()
    println()

//    println("FIRST':")
//    grammar.rules.forEach {
//        val first = getFirst(grammar.first, it.right, grammar)
//        val _first = (first - "_") + if ("_" in first) grammar.follow[it.left]!! else emptySet()
//        println("$it ### $_first")
//    }
}

//TODO: move to utils
@Early
private fun readFile(name: String): String {
    //TODO: make not abstract path
    val path = Paths.get("/home/tihonovcore/work/GParser/generator/src/test/resources/$name")

    val result = StringBuilder()
    Files.readAllLines(path).forEach { result.append(it) }

    return result.toString()
}
