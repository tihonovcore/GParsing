import java.io.File

fun main() {
    val path = "/home/tihonovcore/work/GParser/src/main/resources/naiveExpr"
    val lines = File(path).readLines()
    val userGrammar = readGrammar(lines)

    val grammar = userGrammar
        .removeUselessNonterminals().also { println("Useless removed: \n$it") }
        .removeLeftRecursion().also { println("Recursion removed: \n$it") }

    println("FIRST: " + buildFirst(grammar))
    println("FOLLOW: " + buildFollow(grammar))
    //TODO: устранение правого ветвелния
    print("Is LL(1)-grammar: " + checkLL1(grammar))
    //TODO: tests
}
