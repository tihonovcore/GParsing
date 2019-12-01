import gen.GrammarLexer
import gen.GrammarParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.tihonovcore.grammar.Grammar
import org.tihonovcore.grammar.detailCheckLL1
import org.tihonovcore.utils.Early
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    preprocess("calculator")
    preprocess("funcDeclr")
    preprocess("inhAttr")
    preprocess("functionLanguage")
}

@Early
fun preprocess(name: String) {
    val source = readFile(name)

    val lexer = GrammarLexer(CharStreams.fromString(source))
    val tokens = CommonTokenStream(lexer)
    val parser = GrammarParser(tokens)

    val generator = GrammarGenerator()
    generator.visit(parser.file())

    val grammar = with(generator) {
        Grammar(
            nonterminals.toList(),
            terminals.toList(),
            rules,
            lexerRules,
            codeBlocks,
            synthesized,
            inherited
        )
    }

    val check = detailCheckLL1(grammar)
    require(check.isLL1) { check.description }

    generateFiles(grammar, "/home/tihonovcore/work/GParser/generator/src/main/kotlin/gen", name)
}

@Early
private fun readFile(name: String): String {
    //TODO: make not abstract path
    val path = Paths.get("/home/tihonovcore/work/GParser/generator/src/test/resources/$name")

    val result = StringBuilder()
    Files.readAllLines(path).forEach { result.append(it).append(System.lineSeparator()) }

    return result.toString()
}
