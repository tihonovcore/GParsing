import org.tihonovcore.pascal.Lexer
import org.tihonovcore.pascal.PascalParser
import org.tihonovcore.pascal.RenderVisitor
import org.tihonovcore.pascal.render

fun main() {
    val lines = listOf(
        "function f(a: Integer): Integer",
        "function asdff23456(a: Integer, b: Type): String",
        "procedure as1234567dff23456(z2: Type)",
        "function functionfunction(): Integer"
    )

    for (string in lines) {
        println(string)
        println()

        val tokens = Lexer().getTokens(string)
        println(tokens.render())
        println()

        val parseResult = PascalParser(tokens).parse()
        println(RenderVisitor(parseResult).visit())
    }
}
