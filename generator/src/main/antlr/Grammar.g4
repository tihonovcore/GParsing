grammar Grammar;

@header {
}

@members {
}

file :
    rule_decl* token_decl*
    ;

rule_decl :
    RULE_ID attributes COLON codeblock description SEMICOLON
    ;

attributes :
    inherited synthesized
    ;

inherited :
    ('[' declaration (',' declaration)* ']')?
    ;

synthesized :
    ('returns' '[' declaration (',' declaration)* ']')?
    ;

declaration :
    RULE_ID COLON TYPE
    ;

description :
    and (OR and)*
    ;

and :
    (factor codeblock)+
    ;

factor :
    (ruleIdWithPass | TOKEN_ID) (PLUS | STAR | QUESTION)?
    |
    LB description RB (PLUS | STAR | QUESTION)?
    |
    EPSILON
    ;

ruleIdWithPass :
    RULE_ID PASS?
    ;

token_decl :
    TOKEN_ID COLON REGEX SEMICOLON
    ;

codeblock : CODE_BLOCK*;

REGEX : '\'' (~('\'' | '\n' | '\\') | '\\\\' | '\\\'' | '\\.')+ '\'';

TYPE : [A-Z][a-z]*;
RULE_ID : [a-z][A-Za-z0-9]*; //TODO: rename (reason: uses in attributes)
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

CODE_BLOCK : '\\{' .*? '\\}';
PASS : '\\[' .*? '\\]';

EPSILON : '_';

WS: [ \n\t\r]+ -> skip;
