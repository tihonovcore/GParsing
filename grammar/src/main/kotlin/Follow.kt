//TODO: make private (uses in checkLL1)
//TODO: solve problem with !! by wrapper for getFOLLOW
//TODO: add changed() to wrapper
val FOLLOW = mutableMapOf<Char, MutableSet<Char>>()

@Early
fun buildFollow(grammar: Grammar): Map<Char, Set<Char>> {
    grammar.nonterminals.forEach { FOLLOW[it] = mutableSetOf() }
    FOLLOW[grammar.nonterminals[0]]!! += '$' //TODO: find another way

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

@Experimental
fun buildFollowWithWrapper(grammar: Grammar): Map<Char, Set<Char>> {
    val followWrapper = FirstFollowWrapper(grammar.nonterminals)
    followWrapper[grammar.nonterminals[0]] += '$'

    do {
        for (rule in grammar.rules) {
            rule.right.forEachIndexed { index, a ->
                if (a in grammar.nonterminals) {
                    val gamma = getFirst(rule.right.drop(index + 1), grammar)
                    followWrapper[a] += (gamma - '_').toSet()
                    if ('_' in gamma) followWrapper[a] += followWrapper[rule.left]
                }
            }
        }
    } while (followWrapper.changed())

    return followWrapper.getMap()
}
