package org.tihonovcore.grammar

import org.tihonovcore.utils.Early

data class Grammar(
    val nonterminals: List<String>,
    val terminals: List<String>,
    val rules: List<Rule>,
    @Early val lexerRules: MutableList<Pair<String, String>> = mutableListOf(),
    @Early val codeBlocks: MutableMap<String, String> = mutableMapOf()
) {
    val first: MutableMap<String, MutableSet<String>> = buildFirst(this)
    val follow: MutableMap<String, MutableSet<String>> = buildFollow(this)

    override fun toString(): String {
        return """
            |Grammar render
            | * Non-terminals: $nonterminals
            | * Terminals:     $terminals
            | * Rules:         $rules 
        """.trimMargin()
    }
}

data class Rule(val left: String, val right: List<String>) {
    constructor(left: String, right: String) : this(left, listOf(right))

    override fun toString(): String {
        return "$left -> $right"
    }
}
