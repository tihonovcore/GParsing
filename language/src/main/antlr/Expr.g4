grammar Expr;

@header {
import java.util.*;
}

@members {
static Map<String, String> ti = new HashMap<>();
static {
    ti.put("I+I", "I");
    ti.put("I-I", "I");
    ti.put("I*I", "I");
    ti.put("I/I", "I");
    ti.put("I%I", "I");
    ti.put("I<I", "B");
    ti.put("I<=I", "B");
    ti.put("I>I", "B");
    ti.put("I>=I", "B");
    ti.put("I==I", "B");
    ti.put("I!=I", "B");
    ti.put("-I", "I");
    ti.put("+I", "I");

    ti.put("B&&B", "B");
    ti.put("B||B", "B");
    ti.put("!B", "B");
    ti.put("B==B", "B");
    ti.put("B!=B", "B");
}
}


general returns [Object value, String type] :
    orExpr { $value = $orExpr.value; $type = $orExpr.type; } (OR orExpr { if (!($value instanceof Boolean) || !($orExpr.value instanceof Boolean)) throw new IllegalArgumentException("or"); $value = (Boolean) $value || (Boolean) $orExpr.value; $type = ti.get($type + "||" + $orExpr.type); } )*
    ;

orExpr returns [Object value, String type] :
    andExpr { $value = $andExpr.value; $type = $andExpr.type; } (AND andExpr { if (!($value instanceof Boolean) || !($andExpr.value instanceof Boolean)) throw new IllegalArgumentException("and"); $value = ((Boolean) $value) && ((Boolean) $andExpr.value); $type = ti.get($type + "||" + $andExpr.type); })*
    ;

andExpr returns [Object value, String type] :
    compExpr { $value = $compExpr.value; $type = $compExpr.type; }
        (
            EQUALSEQUALS compExpr { $value = ($value == ($compExpr.value)); $type = ti.get($type + "==" + $compExpr.type); }
            |
            NOTEQUALS compExpr { $value = ($value != ($compExpr.value));  $type = ti.get($type + "!=" + $compExpr.type); }
        )*
    ;

compExpr returns [Object value, String type] :
    NOT general { if (!($general.value instanceof Boolean)) throw new IllegalArgumentException("not"); $value = !((Boolean) $general.value); $type = ti.get("!" + $general.type); }
    |
    LBRACKET general RBRACKET { $value = $general.value; $type = $general.type; }
    |
    TRUE { $value = true; $type = "B"; }
    |
    FALSE { $value = false; $type = "B"; }
    |
    arithExpr { $value = $arithExpr.value; $type = $arithExpr.type; }
            (
                LESS_OR_EQUALS arithExpr1 { $value = $arithExpr.value <= $arithExpr1.value; $type = ti.get($type + "<=" + $arithExpr1.type); }
                |
                GREATER_OR_EQUALS arithExpr1 { $value = $arithExpr.value >= $arithExpr1.value; $type = ti.get($type + ">=" + $arithExpr1.type); }
                |
                LESS arithExpr1 { $value = $arithExpr.value < $arithExpr1.value; $type = ti.get($type + "<" + $arithExpr1.type); }
                |
                GREATER arithExpr1 { $value = $arithExpr.value > $arithExpr1.value; $type = ti.get($type + ">" + $arithExpr1.type); }
            )?
    ;

arithExpr1 returns [int value, String type] : arithExpr { $value = $arithExpr.value; $type = $arithExpr.type; };

arithExpr returns [int value, String type] : expression { $value = $expression.value; $type = $expression.type; };

expression returns [int value, String type] :
    term { $value = $term.value; $type = $term.type; }
        (
            PLUS term { $value += $term.value; $type = ti.get($type + "+" + $term.type); }
            |
            MINUS term { $value -= $term.value; $type = ti.get($type + "-" + $term.type); }
        )*
    ;

term returns [int value, String type] :
    factor { $value = $factor.value; $type = $factor.type; }
        (
            STAR factor { $value *= $factor.value; $type = ti.get($type + "*" + $factor.type); }
            |
            SLASH factor { $value /= $factor.value; $type = ti.get($type + "/" + $factor.type); }
            |
            PERCENT factor { $value %= $factor.value; $type = ti.get($type + "%" + $factor.type); }
        )*
    ;

factor returns [int value, String type] :
    NUMBER { $value = Integer.parseInt($NUMBER.getText()); $type = "I"; }
    |
    LBRACKET expression RBRACKET { $value = $expression.value; $type = $expression.type; }
    |
    PLUS factor { $value = $factor.value; $type = $factor.type; }
    |
    MINUS factor { $value = -$factor.value; $type = $factor.type; }
    ;

NUMBER : [0-9]+;
PLUS   : '+';
MINUS   : '-';
STAR    : '*';
SLASH    : '/';
PERCENT    : '%';
LBRACKET     : '(';
RBRACKET     : ')';

EQUALSEQUALS : '==';
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
