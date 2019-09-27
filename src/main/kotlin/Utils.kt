fun readGrammar(lines: List<String>): Grammar {
    require(lines.size > 2)
    require(lines.drop(2).all { it.contains("->") }) //TODO: check rule format better

    val nonterminals = lines[0].toList()
    val terminals = lines[1].toList()

    val rules = lines.subList(2, lines.size).map {
        val args = it.split("->")
        Rule(args[0].single(), args[1])
    }
    return Grammar(nonterminals, terminals, rules)
}

/**
 * @return <code>true</code> if <code>grammar</code> is LL(1)-grammar
 */
fun checkLL1(grammar: Grammar): Boolean { //TODO: return more info
    grammar.rules.forEachPair { a, b ->
        if (a.left != b.left) return@forEachPair

        val firstA = getFirst(a.right, grammar)
        val firstB = getFirst(b.right, grammar)

        if (firstA.cross(firstB)) return@checkLL1 false

        if ('_' in firstA && firstB.cross(FOLLOW[a.left]!!.toList())) {
            return@checkLL1 false
        }
    }

    return true
}

inline fun List<Rule>.forEachPair(body: (a: Rule, b: Rule) -> Unit) {
    for (i in indices) {
        for (j in indices) {
            if (i < j) body(this[i], this[j])
        }
    }
}

private fun List<Char>.cross(other: List<Char>): Boolean {
    return this.any { it in other }
}
