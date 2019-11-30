import gen.GrammarLexer
import gen.GrammarParser
import junit.framework.TestCase
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.Test
import org.tihonovcore.grammar.Grammar
import org.tihonovcore.grammar.detailCheckLL1
import org.tihonovcore.utils.Early
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Paths

@Early
class TestGenerator : TestCase() {
    @Test
    fun testExpressionGrammar() = doTest("calculator")

    @Test
    fun testFunctionDeclarationGrammar() = doTest("funcDeclr")

    @Test
    fun testLangGrammar() = doTest("simpleLang")

    @Test
    fun testGrammarGrammar() = doTest("myGrammar")

    @Early
    private fun doTest(name: String) {
        println("TEST $name")

        val source = readFile(name)

        val lexer = GrammarLexer(CharStreams.fromString(source))
        val tokens = CommonTokenStream(lexer)
        val parser = GrammarParser(tokens)

        val generator = GrammarGenerator()
        generator.visit(parser.file())

        val grammar = Grammar(
            generator.nonterminals.toList(),
            generator.terminals.toList(),
            generator.rules
        )

        grammar.rules.forEach { println(it) }
        println(detailCheckLL1(grammar))
        println()
    }
}

@Early
private fun readFile(name: String): String {
    //TODO: make not abstract path
    val path = Paths.get("/home/tihonovcore/work/GParser/generator/src/test/resources/$name")

    val result = StringBuilder()
    Files.readAllLines(path).forEach { result.append(it).append(System.lineSeparator()) }

    return result.toString()
}
