package org.tihonovcore.grammar

fun getFirst(FIRST: MutableMap<String, MutableSet<String>>, alpha: List<String>, grammar: Grammar): List<String> {
    if (alpha.isEmpty() || alpha[0] == "_") return listOf("_") //TODO: isEmpty is ok?

    if (alpha[0] in grammar.terminals) return listOf(alpha[0])

    val first = FIRST[alpha[0]]!!
    return (first - "_").toList() + if ("_" in first) getFirst(FIRST, alpha.drop(1), grammar) else listOf()
}

fun buildFirst(grammar: Grammar): MutableMap<String, MutableSet<String>> {
    val FIRST = mutableMapOf<String, MutableSet<String>>()
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
