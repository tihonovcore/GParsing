package org.tihonovcore.grammar

import org.tihonovcore.utils.Early

@Early //TODO: remove only direct recursion
fun Grammar.removeLeftRecursion(): Grammar {
    val updatedRules = mutableListOf<Rule>()
    val newNonterminals = mutableListOf<Char>()

    nonterminals.forEach { thisNonterminal ->
        val recursiveRules = rules.filter { r ->
            r.left == thisNonterminal && r.right.first() == thisNonterminal
        }
        val other = rules.filter { r ->
            r.left == thisNonterminal && r.right.first() != thisNonterminal
        }

        if (recursiveRules.isNotEmpty()) {
            val newNonterminal = thisNonterminal.toLowerCase() //TODO: find better way to creating new name
            newNonterminals += newNonterminal

            updatedRules += recursiveRules.map { Rule(newNonterminal, it.right.drop(1) + newNonterminal) }
            updatedRules += other.map { Rule(thisNonterminal, it.right + newNonterminal) }
            updatedRules += Rule(newNonterminal, "_")
        } else {
            updatedRules += other
        }
    }

    //HACK: if right == "_", then right + newNonterminal == "_q", but expected "q"
    for (i in updatedRules.indices) {
        if (updatedRules[i].right.dropLast(1).contains('_')) {
            updatedRules[i] = Rule(updatedRules[i].left, updatedRules[i].right.replace("_", ""))
        }
    }

    return this.copy(nonterminals = nonterminals + newNonterminals, rules = updatedRules)
}

@Early
fun Grammar.unsafeRemoveRightBranching(): Grammar {
    val newNonterminals = mutableListOf<Char>()
    val newRules = mutableListOf<Rule>()
    val removedRules = mutableListOf<Rule>()

    val possibleNonterminals = (mutableSetOf<Char>() + ('A'..'Z') - nonterminals).toMutableSet()

    rules.forEachPair { a, b ->
        if (a.left == b.left) {
            val lcp = findLcp(a.right, b.right)
            println("a: ${a.right} b: ${b.right} lcp: $lcp")

            if (lcp != 0) {
                val newNT = possibleNonterminals.random()
                possibleNonterminals.remove(newNT)

                newNonterminals += newNT
                newRules += Rule(a.left, a.right.substring(0, lcp) + newNT)
                newRules += Rule(newNT, a.right.drop(lcp)) //TODO: string after drop may be empty (expected '_')
                newRules += Rule(newNT, b.right.drop(lcp)) //TODO: string after drop may be empty (expected '_')

                removedRules += a
                removedRules += b
            }
        }
    }

    return Grammar(nonterminals + newNonterminals, terminals, newRules + rules - removedRules)
}

private fun findLcp(a: String, b: String): Int {
    var prefixLength = 0
    while (prefixLength < a.length && prefixLength < b.length) {
        if (a[prefixLength] != b[prefixLength]) break
        prefixLength++
    }
    return prefixLength
}
