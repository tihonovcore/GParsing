@Early
data class Grammar(
    val nonterminals: List<Char>, //TODO: add mapping Char -> (index number)
    val terminals: List<Char>,
    val rules: List<Rule>
) {
    override fun toString(): String {
        return """
            |Grammar
            |Non-terminals: $nonterminals
            |Terminals:     $terminals
            |Rules:         $rules 
        """.trimMargin()
    }
}

@Early
data class Rule(val left: Char, val right: String) {
    override fun toString(): String {
        return "$left -> $right"
    }
}
