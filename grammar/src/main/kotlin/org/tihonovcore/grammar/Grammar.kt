package org.tihonovcore.grammar

import org.tihonovcore.utils.Early

data class Grammar(
    val nonterminals: List<Char>,
    val terminals: List<Char>,
    val rules: List<Rule>
) {
    @Early val first: MutableMap<Char, MutableSet<Char>> = buildFirst(this)
    @Early val follow: MutableMap<Char, MutableSet<Char>> = buildFollow(this)

    override fun toString(): String {
        return """
            |Grammar render
            | * Non-terminals: $nonterminals
            | * Terminals:     $terminals
            | * Rules:         $rules 
        """.trimMargin()
    }
}

data class Rule(val left: Char, val right: String) {
    override fun toString(): String {
        return "$left -> $right"
    }
}
