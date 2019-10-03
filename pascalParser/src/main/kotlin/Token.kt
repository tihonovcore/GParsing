@Early
data class Token(
    val type: TokenType,
    val data: Any? = null //e.g. function name for function
)
