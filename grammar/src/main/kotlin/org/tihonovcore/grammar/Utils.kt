package org.tihonovcore.grammar

import org.tihonovcore.utils.Early

fun readGrammar(lines: List<String>): Grammar {
    require(lines.size > 2)
    require(lines.drop(2).all { it.contains("->") }) //TODO: check rule format better

    val nonterminals = lines[0].split(" ").toList()
    val terminals = lines[1].split(" ").toList()

    val rules = lines.subList(2, lines.size).map {
        val args = it.split("->")
        Rule(args[0], args[1].split(" "))
    }
    return Grammar(nonterminals, terminals, rules)
}

data class CheckLL1Result(val isLL1: Boolean, val description: String = "It is LL(1)-grammar")

fun detailCheckLL1(grammar: Grammar): CheckLL1Result {
    grammar.rules.forEachPair { a, b ->
        if (a.left != b.left) return@forEachPair

        val firstA = getFirst(grammar.first, a.right, grammar)
        val firstB = getFirst(grammar.first, b.right, grammar)

        if (firstA.cross(firstB)) {
            return CheckLL1Result(
                isLL1 = false,
                description = "Problem with rule A: $a and rule B: $b " + System.lineSeparator() +
                        "FIRST[${a.right}] intersect FIRST[${b.right}]:" + System.lineSeparator() +
                        "FIRST[${a.right}] = $firstA " + System.lineSeparator() +
                        "FIRST[${b.right}] = $firstB "
            )
        }

        if ("_" in firstA && firstB.cross(grammar.follow[a.left]!!.toList())) {
            return CheckLL1Result(
                isLL1 = false,
                description = "Problem with rule A: $a and rule B: $b " + System.lineSeparator() +
                        "'_' in FIRST[${a.right}] and FIRST[${b.right}] intersect FOLLOW[${a.left}]" + System.lineSeparator() +
                        "FIRST[${a.right}] = $firstA " + System.lineSeparator() +
                        "FIRST[${b.right}] = $firstB " + System.lineSeparator() +
                        "FOLLOW[${a.left}] = ${grammar.follow[a.left]!!.toList()}"
            )
        }
    }

    return CheckLL1Result(true)
}

@Early
fun Grammar.getFIRST1(): Map<Rule, List<String>> {
    val result = mutableMapOf<Rule, List<String>>()

    rules.forEach {
        val first = getFirst(first, it.right, this)
        val _first = (first - "_") + if ("_" in first) follow[it.left]!! else emptySet()
        result[it] = _first
    }

    return result
}

internal inline fun List<Rule>.forEachPair(body: (a: Rule, b: Rule) -> Unit) {
    for (i in indices) {
        for (j in indices) {
            if (i < j) body(this[i], this[j])
        }
    }
}

private fun List<String>.cross(other: List<String>): Boolean {
    return this.any { it in other }
}
