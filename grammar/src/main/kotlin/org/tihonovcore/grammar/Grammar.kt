package org.tihonovcore.grammar

data class Grammar(
    val nonterminals: List<String>,
    val terminals: List<String>,
    val rules: List<Rule>
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
    override fun toString(): String {
        return "$left -> $right"
    }
}
