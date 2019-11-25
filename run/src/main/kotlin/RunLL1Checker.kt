import org.tihonovcore.grammar.*
import java.io.File

fun main() {
    val path = "run/src/main/resources/naiveLambda"
    val lines = File(path).readLines()
    val userGrammar = readGrammar(lines)

    val grammar = userGrammar
        .removeUselessNonterminals().also { println("Useless removed: \n$it") }
        .removeLeftRecursion().also { println("Recursion removed: \n$it") }
        .unsafeRemoveRightBranching().also { println("Right branching removed: \n$it") }

    println("getFIRST: " + buildFirst(grammar))
    println("getFOLLOW: " + buildFollow(grammar))
    print("Is LL(1)-grammar: " + detailCheckLL1(grammar))
    println()
    println("FIRST':")
    grammar.rules.forEach {
        val first = getFirst(grammar.first, it.right, grammar)
        val _first = (first - "_") + if ("_" in first) grammar.follow[it.left]!! else emptySet()
        println("$it ### $_first")
    }
}
