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
