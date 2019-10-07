package org.tihonovcore.grammar

data class Grammar(
    val nonterminals: List<Char>,
    val terminals: List<Char>,
    val rules: List<Rule>
) {
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
