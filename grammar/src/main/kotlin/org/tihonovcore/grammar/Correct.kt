package org.tihonovcore.grammar

import org.tihonovcore.utils.Early

@Early //TODO: now removes only direct recursion
fun Grammar.removeLeftRecursion(): Grammar {
    val updatedRules = mutableListOf<Rule>()
    val newNonterminals = mutableListOf<String>()

    nonterminals.forEach { thisNonterminal ->
        val recursiveRules = rules.filter { r ->
            r.left == thisNonterminal && r.right.first() == thisNonterminal
        }
        val other = rules.filter { r ->
            r.left == thisNonterminal && r.right.first() != thisNonterminal
        }

        if (recursiveRules.isNotEmpty()) {
            val newNonterminal = findFreeName(thisNonterminal, nonterminals)
            newNonterminals += newNonterminal

            updatedRules += recursiveRules.map { Rule(newNonterminal, it.right.drop(1) + newNonterminal) }
            updatedRules += other.map { Rule(thisNonterminal, it.right + newNonterminal) }
            updatedRules += Rule(newNonterminal, listOf("_"))
        } else {
            updatedRules += other
        }
    }

    //HACK: if right == "_", then right + newNonterminal == "_q", but expected "q"
    for (i in updatedRules.indices) {
        if (updatedRules[i].right.dropLast(1).contains("_")) {
            updatedRules[i] = Rule(updatedRules[i].left, updatedRules[i].right.map { if (it == "_") "" else it })
        }
    }

    return this.copy(nonterminals = nonterminals + newNonterminals, rules = updatedRules)
}

private fun findFreeName(previous: String, nonterminals: List<String>): String {
    var suggest = "_$previous"
    while (suggest in nonterminals) {
        suggest += "'"
    }
    return suggest
}

@Early
fun Grammar.unsafeRemoveRightBranching(): Grammar {
    val newNonterminals = mutableListOf<String>()
    val newRules = mutableListOf<Rule>()
    val removedRules = mutableListOf<Rule>()

    val possibleNonterminals = (mutableSetOf<Char>() + ('A'..'Z') - nonterminals).map { it.toString() }.toMutableSet()

    rules.forEachPair { a, b ->
        if (a.left == b.left) {
            val lcp = findLcp(a.right, b.right)

            if (lcp != 0) {
                val newNT = possibleNonterminals.random()
                possibleNonterminals.remove(newNT)

                newNonterminals += newNT
                newRules += Rule(a.left, a.right.subList(0, lcp) + newNT)
                newRules += Rule(newNT, a.right.drop(lcp)) //TODO: string after drop may be empty (expected '_')
                newRules += Rule(newNT, b.right.drop(lcp)) //TODO: string after drop may be empty (expected '_')

                removedRules += a
                removedRules += b
            }
        }
    }

    return Grammar(nonterminals + newNonterminals, terminals, newRules + rules - removedRules)
}

private fun findLcp(a: List<String>, b: List<String>): Int {
    var prefixLength = 0
    while (prefixLength < a.size && prefixLength < b.size) {
        if (a[prefixLength] != b[prefixLength]) break
        prefixLength++
    }
    return prefixLength
}
