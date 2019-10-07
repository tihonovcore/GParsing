import org.tihonovcore.grammar.*
import org.tihonovcore.utils.Early

/**
 * F file
 * D declaration
 * N name
 * A arguments
 * T type
 * P suffix of arguments (,T)*
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
        "FDNATP".toList(),
        "01():,*_".toList(),
        listOf(
            Rule('F', "D"),
            Rule('D', "0N(A):T"),
            Rule('D', "1N(A)"),
            Rule('N', "*"),
            Rule('T', "*"),
            Rule('A', "_"),
            Rule('A', "N:TP"),
            Rule('P', ",N:TP"),
            Rule('P', "_")
        )
    ).removeUselessNonterminals().removeLeftRecursion()

    println(x)

    buildFirst(x).also { println(it) }
    buildFollow(x).also { println(it) }

    println("IS LL(1): " + checkLL1(x))
}
