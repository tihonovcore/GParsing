package org.tihonovcore.grammar

fun getFirst(FIRST: MutableMap<Char, MutableSet<Char>>, alpha: String, grammar: Grammar): List<Char> {
    if (alpha == "_" || alpha.isEmpty()) return listOf('_') //TODO: isEmpty is ok?

    if (alpha[0] in grammar.terminals) return listOf(alpha[0])

    val first = FIRST[alpha[0]]!!
    return (first - '_').toList() + if ('_' in first) getFirst(FIRST, alpha.drop(1), grammar) else listOf()
}

fun buildFirst(grammar: Grammar): MutableMap<Char, MutableSet<Char>> {
    val FIRST = mutableMapOf<Char, MutableSet<Char>>()
    grammar.nonterminals.forEach { FIRST[it] = mutableSetOf() }

    var changed: Boolean
    do {
        changed = false
        for (rule in grammar.rules) {
            val size = FIRST[rule.left]!!.size
            FIRST[rule.left]!! += getFirst(FIRST, rule.right, grammar)
            changed = changed || (size != FIRST[rule.left]!!.size)
        }
    } while (changed)

    return FIRST
}
