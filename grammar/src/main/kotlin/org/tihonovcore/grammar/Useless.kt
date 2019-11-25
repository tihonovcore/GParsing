package org.tihonovcore.grammar

/**
 * Build new Grammar from <code>grammar</code> without non-generative
 * and unreachable nonterminals, remove useless rules
 */
fun Grammar.removeUselessNonterminals(): Grammar {
    return this
        .removeRulesFromNonGeneratingNonterminals()
        .removeRulesFromUnreachableNonterminals()
        .removeUnusedTerminalsAndNonterminals()
}

/**
 * Find generating nonterminals, build new Grammar from <code>grammar</code>
 * without rules from these nonterminals
 */
internal fun Grammar.removeRulesFromNonGeneratingNonterminals(): Grammar {
    val generating = mutableSetOf<String>()

    fun Rule.fromGenerating(): Boolean {
        return this.right.all { it in generating || it in terminals }
    }

    var changed: Boolean
    do {
        changed = false
        for (rule in rules) {
            if (rule.left in generating) continue

            if (rule.fromGenerating()) {
                generating += rule.left
                changed = true
            }
        }
    } while (changed)

    return filterRules { it.fromGenerating() }
}

internal fun Grammar.removeRulesFromUnreachableNonterminals(): Grammar {
    val reachable = mutableMapOf<String, Boolean>()
    nonterminals.forEach { reachable[it] = false }

    findReachableNodes(this, reachable)

    return filterRules { reachable[it.left]!! }
}

internal fun Grammar.filterRules(body: (Rule) -> Boolean): Grammar {
    return copy(rules = rules.filter(body))
}

internal fun findReachableNodes(
    grammar: Grammar,
    visited: MutableMap<String, Boolean>,
    current: String = visited.keys.first()
) {
    if (visited[current]!!) return

    visited[current] = true

    for (rule in grammar.rules) {
        if (rule.left == current) {
            rule.right.filter { it in grammar.nonterminals }.forEach {
                findReachableNodes(grammar, visited, it)
            }
        }
    }
}

internal fun Grammar.removeUnusedTerminalsAndNonterminals(): Grammar {
    fun List<String>.findUsable() = this.filter {
        rules.any { r -> r.left == it || r.right.contains(it) }
    }

    return copy(
        nonterminals = nonterminals.findUsable(),
        terminals = terminals.findUsable()
    )
}
