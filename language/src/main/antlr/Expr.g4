grammar Expr;

@header {
//package org.expr;
//TODO: add global context
//int xxx() { return 100; }
}


eval : general { System.out.println($general.value); };

general returns [Object value]: orExpr { $value = $orExpr.value; } (OR orExpr { $value = (Boolean) $value || $orExpr.value; })*;
orExpr returns [boolean value]: andExpr { $value = $andExpr.value; } (AND andExpr { $value = $value && $andExpr.value; })*;
andExpr returns [boolean value]: compExpr { $value = (Boolean) $compExpr.value; } (EQUALS compExpr { $value = ($value == ((Boolean) $compExpr.value)); } | NOTEQUALS compExpr { $value = ($value != ((Boolean) $compExpr.value)); })+| arithExpr (LESS_OR_EQUALS arithExpr1 { $value = $arithExpr.value <= $arithExpr1.value; } | GREATER_OR_EQUALS arithExpr1 { $value = $arithExpr.value >= $arithExpr1.value; } | LESS arithExpr1 { $value = $arithExpr.value < $arithExpr1.value; } | GREATER arithExpr1 { $value = $arithExpr.value > $arithExpr1.value; });
compExpr returns [Object value]: NOT general { $value = !( (Boolean) $general.value); } | LBRACKET general RBRACKET { $value = $general.value; }| TRUE { $value = true; } | FALSE { $value = false; } | arithExpr { $value = $arithExpr.value; };

arithExpr1 returns [int value] : arithExpr { $value = $arithExpr.value; };
arithExpr returns [int value] : expression { $value = $expression.value; };
expression returns [int value] : term { $value = $term.value; } (PLUS term { $value += $term.value; } | MINUS term { $value -= $term.value; })*;
term returns [int value] : factor { $value = $factor.value; } (STAR factor { $value *= $factor.value;} | SLASH factor { $value /= $factor.value; } | PERCENT factor { $value %= $factor.value; } )*;
factor returns [int value] : NUMBER { $value = Integer.parseInt($NUMBER.getText()); } | LBRACKET expression RBRACKET { $value = $expression.value; } | PLUS NUMBER { $value = Integer.parseInt($NUMBER.getText()); } | MINUS NUMBER { $value = -Integer.parseInt($NUMBER.getText()); };

NUMBER : [0-9]+;
PLUS   : '+';
MINUS   : '-';
STAR    : '*';
SLASH    : '/';
PERCENT    : '%';
LBRACKET     : '(';
RBRACKET     : ')';

EQUALS : '==';
NOTEQUALS : '!=';
LESS_OR_EQUALS : '<=';
GREATER_OR_EQUALS : '>=';
LESS : '<';
GREATER : '>';

NOT: '!';
OR : '||';
AND : '&&';
FALSE : 'false';
TRUE : 'true';

WS: [ \n\t\r]+ -> skip;
