grammar Grammar;

@header {
}

@members {
}

file :
    rule_decl* token_decl*
    ;

rule_decl :
    RULE_ID attributes COLON codeblock rule1 SEMICOLON //TODO: rename `rule1`
    ;

attributes :
    //[inherited]? (returns [synthesized])?
    inherited synthesized
//    ('returns' '[' declaration (',' declaration)* ']')?
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

rule1 :
    and (OR and)*
    ;

and :
    (factor codeblock)+
    ;

factor :
    (RULE_ID | TOKEN_ID) (PLUS | STAR | QUESTION)?
    |
    LB rule1 RB (PLUS | STAR | QUESTION)?
    |
    EPSILON
    ;

token_decl :
    TOKEN_ID COLON REGEX SEMICOLON //TODO: support rules like `DOUBLE : NUMBER DOT NUMBER`
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

EPSILON : '_';

WS: [ \n\t\r]+ -> skip;
