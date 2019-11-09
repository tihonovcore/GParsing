package task2

import org.tihonovcore.grammar.*

fun main() {
    val x = Grammar(
        "QFPSNATDZ".toList(),
        "01():,*_;".toList(),
        listOf(
            Rule('Q', "F;"),
            Rule('Q', "P;"),
            Rule('F', "0S:T"),
            Rule('P', "1S"),
            Rule('S', "N(A)"),
            Rule('N', "*"),
            Rule('T', "*"),
            Rule('A', "_"),
            Rule('A', "DZ"),
            Rule('D', "N:T"),
            Rule('Z', "_"),
            Rule('Z', ",DZ")
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
        val _first = (first - '_') + if ('_' in first) x.follow[it.left]!! else emptySet()
        println("$it ### $_first")
    }
}
