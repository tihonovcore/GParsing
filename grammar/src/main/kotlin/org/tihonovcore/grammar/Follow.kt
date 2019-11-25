package org.tihonovcore.grammar

fun buildFollow(grammar: Grammar): MutableMap<String, MutableSet<String>> {
    val FOLLOW = mutableMapOf<String, MutableSet<String>>()
    grammar.nonterminals.forEach { FOLLOW[it] = mutableSetOf() }
    FOLLOW[grammar.nonterminals[0]]!! += "$" //TODO: find another way

    var changed: Boolean
    do {
        changed = false
        for (rule in grammar.rules) {
            rule.right.forEachIndexed { index, a ->
                if (a in grammar.nonterminals) {
                    val size = FOLLOW[a]!!.size
                    val gamma = getFirst(grammar.first, rule.right.drop(index + 1), grammar)
                    FOLLOW[a]!! += (gamma - "_").toList()
                    if ("_" in gamma) FOLLOW[a]!! += FOLLOW[rule.left]!!
                    changed = changed || (size != FOLLOW[a]!!.size)
                }
            }
        }
    } while (changed)

    return FOLLOW
}
