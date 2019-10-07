package org.tihonovcore.pascal

import org.tihonovcore.utils.Early

@Early
enum class TokenType {
    FUNCTION, PROCEDURE, LBRACKET, RBRACKET, COLON, COMMA, STRING, EOF
}
