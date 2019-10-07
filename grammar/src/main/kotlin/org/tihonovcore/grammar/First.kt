package org.tihonovcore.grammar

import org.tihonovcore.utils.Early
import org.tihonovcore.utils.Experimental

//TODO: make private (uses in checkLL1)
//TODO: solve problem with !! by wrapper for getFIRST
//TODO: add changed() to wrapper
val FIRST = mutableMapOf<Char, MutableSet<Char>>()

@Early
fun getFirst(alpha: String, grammar: Grammar): List<Char> {
    if (alpha == "_" || alpha.isEmpty()) return listOf('_') //TODO: isEmpty is ok?

    if (alpha[0] in grammar.terminals) return listOf(alpha[0])

    val first = FIRST[alpha[0]]!!
    return (first - '_').toList() + if ('_' in first) getFirst(alpha.drop(1), grammar) else listOf()
}

@Early
fun buildFirst(grammar: Grammar): Map<Char, Set<Char>> {
    grammar.nonterminals.forEach { FIRST[it] = mutableSetOf() }

    var changed: Boolean
    do {
        changed = false
        for (rule in grammar.rules) {
            val size = FIRST[rule.left]!!.size
            FIRST[rule.left]!! += getFirst(rule.right, grammar)
            changed = changed || (size != FIRST[rule.left]!!.size)
        }
    } while (changed)

    return FIRST
}

@Experimental
fun buildFirstWithWrapper(grammar: Grammar): Map<Char, Set<Char>> {
    val firstWrapper = FirstFollowWrapper(grammar.nonterminals)

    do {
        grammar.rules.forEach { rule ->
            firstWrapper[rule.left] += getFirst(rule.right, grammar).toSet()
        }
    } while (firstWrapper.changed())

    return firstWrapper.getMap()
}
