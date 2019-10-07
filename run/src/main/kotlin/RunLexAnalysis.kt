import org.tihonovcore.pascal.Lexer
import org.tihonovcore.pascal.render
import org.tihonovcore.utils.Early

@Early
fun main() {
    val lines = listOf(
        "function f(a: Integer): Integer",
        "function asdff23456(a: Integer, b: Type): String",
        "procedure as1234567dff23456(z2: Type)",
        "procedure 2nameStartWithNumber(z2: Type)"

    )

    for (string in lines) {
        println(Lexer().getTokens(string).render())
    }
}
