import org.tihonovcore.pascal.Lexer
import org.tihonovcore.pascal.PascalParser
import org.tihonovcore.pascal.RenderVisitor
import org.tihonovcore.pascal.render

fun main() {
    val lines = listOf(
        "function f(a: Integer): Integer",
        "function asdff23456(a: Integer, b: Type): String",
        "procedure as1234567dff23456(z2: Type)",
        "procedure 2nameStartWithNumber(z2: Type)"

    )

    for (string in lines) {
        val tokens = Lexer().getTokens(string)
        val parseResult = PascalParser(tokens).parse()
        println(string)
        println()
        println(tokens.render())
        println()
        println(RenderVisitor(parseResult).visit())
    }
}
