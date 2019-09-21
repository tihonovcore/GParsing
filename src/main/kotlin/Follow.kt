//TODO: make private (uses in checkLL1)
//TODO: solve problem with !! by wrapper for FOLLOW
//TODO: add changed() to wrapper
val FOLLOW = mutableMapOf<Char, MutableSet<Char>>()

@Early
fun buildFollow(grammar: Grammar): Map<Char, Set<Char>> {
    grammar.nonterminals.forEach { FOLLOW[it] = mutableSetOf() }
    FOLLOW[grammar.nonterminals[0]]!! += '$'

    var changed: Boolean
    do {
        changed = false
        for (rule in grammar.rules) {
            rule.right.forEachIndexed { index, a ->
                if (a in grammar.nonterminals) {
                    val size = FOLLOW[a]!!.size
                    val gamma = getFirst(rule.right.drop(index + 1), grammar)
                    FOLLOW[a]!! += (gamma - '_').toList()
                    if ('_' in gamma) FOLLOW[a]!! += FOLLOW[rule.left]!!
                    changed = changed || (size != FOLLOW[a]!!.size)
                }
            }
        }
    } while (changed)

    return FOLLOW
}
