/**
 * Build new Grammar from <code>grammar</code> without non-generative
 * and unreachable nonterminals, remove useless rules
 */
@Early
fun removeUselessNonterminals(grammar: Grammar): Grammar {
    return grammar
        .removeRulesFromNonGeneratingNonterminals()
        .removeRulesFromUnreachableNonterminals()
        .removeUnusedTerminalsAndNonterminals()
}

/**
 * Find generating nonterminals, build new Grammar from <code>grammar</code>
 * without rules from these nonterminals
 */
@Early
internal fun Grammar.removeRulesFromNonGeneratingNonterminals(): Grammar {
    val generating = mutableSetOf<Char>()

    fun Rule.leftIsGenerating(): Boolean {
        return this.right.all { it in generating || it in terminals }
    }

    var changed: Boolean
    do {
        changed = false
        for (rule in rules) {
            if (rule.left in generating) continue

            if (rule.leftIsGenerating()) {
                generating += rule.left
                changed = true
            }
        }
    } while (changed)

    return filterRules { it.leftIsGenerating() }
}

@Early
internal fun Grammar.removeRulesFromUnreachableNonterminals(): Grammar {
    val reachable = MutableList(nonterminals.size) { false }
    findReachableTerms(this, reachable)
    return filterRules { reachable[nonterminals.indexOf(it.left)] }
}

@Early
internal fun Grammar.filterRules(body: (Rule) -> Boolean): Grammar {
    return copy(rules = rules.filter(body))
}

@Early
internal fun findReachableTerms(
    grammar: Grammar,
    visited: MutableList<Boolean>,
    current: Int = 0
) {
    if (visited[current]) return

    fun Rule.fromCurrentNonterminal(): Boolean {
        return left == grammar.nonterminals[current]
    }

    visited[current] = true
    for (rule in grammar.rules) {
        if (rule.fromCurrentNonterminal()) {
            rule.right.filter { it in grammar.nonterminals }.forEach {
                findReachableTerms(grammar, visited, grammar.nonterminals.indexOf(it))
            }
        }
    }
}

@Early
internal fun Grammar.removeUnusedTerminalsAndNonterminals(): Grammar {
    fun List<Char>.findUsable() = this.filter {
        rules.any { r -> r.left == it || r.right.contains(it) }
    }

    return copy(
        nonterminals = nonterminals.findUsable(),
        terminals = terminals.findUsable()
    )
}
