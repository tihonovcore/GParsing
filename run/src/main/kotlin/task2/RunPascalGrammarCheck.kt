package task2

import org.tihonovcore.grammar.*

fun main() {
    val x = Grammar(
        "file function procedure signature name arguments type declaration suffixOfArguments".split(" ").toList(),
        "FUNCTION PROCEDURE LB RB COLON COMMA ID EPS SEMICOLON".split(" ").toList(),
        listOf(
            Rule("file", "function SEMICOLON".split(" ")),
            Rule("file", "procedure SEMICOLON".split(" ")),
            Rule("function", "FUNCTION signature COLON type".split(" ")),
            Rule("procedure", "PROCEDURE signature".split(" ")),
            Rule("signature", "name LB arguments RB".split(" ")),
            Rule("name", "ID".split(" ")),
            Rule("type", "ID".split(" ")),
            Rule("arguments", "EPS".split(" ")),
            Rule("arguments", "declaration suffixOfArguments".split(" ")),
            Rule("declaration", "name COLON type".split(" ")),
            Rule("suffixOfArguments", "EPS".split(" ")),
            Rule("suffixOfArguments", "COMMA declaration suffixOfArguments".split(" "))
        )
    )//.removeUselessNonterminals().removeLeftRecursion().unsafeRemoveRightBranching()

    println(x)

    buildFirst(x).also { println(it) }
    buildFollow(x).also { println(it) }

    val checkLL1Result = detailCheckLL1(x)
    println("IS LL(1): " + checkLL1Result.isLL1)
    if (!checkLL1Result.isLL1) {
        println(checkLL1Result.description)
    }

    println()
    println("FIRST':")
    x.rules.forEach {
        val first = getFirst(x.first, it.right, x)
        val _first = (first - "_") + if ("_" in first) x.follow[it.left]!! else emptySet()
        println("$it ### $_first")
    }
}
