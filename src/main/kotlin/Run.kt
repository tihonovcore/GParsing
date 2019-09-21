import java.io.File

fun main() {
    val path = "/home/tihonovcore/work/GParser/src/main/resources/hardExpr"
    val lines = File(path).readLines()
    val grammar = readGrammar(lines)

    val cleanGrammar = removeUselessNonterminals(grammar)
    println(cleanGrammar)
    println("FIRST: " + buildFirst(cleanGrammar))
    println("FOLLOW: " + buildFollow(cleanGrammar))
    //TODO: устранение левой рекурсии
    //TODO: устранение правого ветвелния
    print("Is LL(1)-grammar: " + checkLL1(cleanGrammar))
    //TODO: tests
}
