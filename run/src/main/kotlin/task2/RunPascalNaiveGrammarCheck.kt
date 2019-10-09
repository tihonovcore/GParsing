package task2

import org.tihonovcore.grammar.*
import org.tihonovcore.utils.Early

/**
 * Q file
 * F function
 * P procedure
 * N name
 * A arguments
 * T type
 *
 * 0 function
 * 1 procedure
 * () brackets for arguments
 * :
 * , arguments separator
 * * string (e.g. declaration name)
 * _ empty stirng (epsilon)
 */
@Early
fun main() {

    val x = Grammar(
        "QFPSNAT".toList(),
        "01():,*_".toList(),
        listOf(
            Rule('Q', "F"),
            Rule('Q', "P"),
            Rule('F', "0S:T"),
            Rule('P', "1S"),
            Rule('S', "N(A)"),
            Rule('N', "*"),
            Rule('T', "*"),
            Rule('A', "_"),
            Rule('A', "N:T"),
            Rule('A', "N:T,A")
        )
    ).removeUselessNonterminals().removeLeftRecursion().unsafeRemoveRightBranching()

    println(x)

    buildFirst(x).also { println(it) }
    buildFollow(x).also { println(it) }

    val checkLL1Result = detailCheckLL1(x)
    println("IS LL(1): " + checkLL1Result.isLL1)
    if (!checkLL1Result.isLL1) {
        println(checkLL1Result.description)
    }
}
