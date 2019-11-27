grammar Grammar;

@header {
}

@members {
}

file :
    rule_decl* token_decl*
    ;

rule_decl :
    RULE_ID COLON rule1 SEMICOLON //TODO: rename `rule1`
    ;

rule1 : and (OR and)*;

and :
    factor+;

factor :
    (RULE_ID | TOKEN_ID) (PLUS | STAR | QUESTION)?
    |
    LB rule1 RB (PLUS | STAR | QUESTION)?
    ;

token_decl :
    TOKEN_ID COLON REGEX SEMICOLON //TODO: support rules like `DOUBLE : NUMBER DOT NUMBER`
    ;

REGEX : '\'' (~('\'' | '\n' | '\\') | '\\\\' | '\\\'' | '\\.')+ '\'';

RULE_ID : [a-z][A-Za-z0-9]*;
TOKEN_ID : [A-Z][A-Z0-9]*;

COLON : ':';
QUOTE : '\'';
SEMICOLON : ';';

STAR : '*';
PLUS : '+';
QUESTION : '?';
OR : '|';
LB : '(';
RB : ')';

WS: [ \n\t\r]+ -> skip;
