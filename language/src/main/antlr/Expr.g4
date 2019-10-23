grammar Expr;

@header {
import java.util.*;
}

@members {
public Map<String, String> idToType = new HashMap<>();

static Map<String, String> ti = new HashMap<>();
static {
//дабл в приоритете, затем левый тип в приоритете (авто расширение инта в лонг?)
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
    
    ti.put("D+I", "D");
    ti.put("D+D", "D");
    ti.put("I+D", "D");
    ti.put("D-I", "D");
    ti.put("D-D", "D");
    ti.put("I-D", "D");
    ti.put("D*I", "D");
    ti.put("D*D", "D");
    ti.put("I*D", "D");
    ti.put("D/I", "D");
    ti.put("D/D", "D");
    ti.put("I/D", "D");
    ti.put("D>I", "B"); //?
    ti.put("D>D", "B");
    ti.put("I>D", "B"); //?
    ti.put("D<I", "B"); //?
    ti.put("D<D", "B");
    ti.put("I<D", "B"); //?
    ti.put("D>=I", "B"); //?
    ti.put("D>=D", "B");
    ti.put("I>=D", "B"); //?
    ti.put("D<=I", "B"); //?
    ti.put("D<=D", "B");
    ti.put("I<=D", "B"); //?
    ti.put("D==I", "B"); //?
    ti.put("D==D", "B");
    ti.put("I==D", "B"); //?
    ti.put("D!=I", "B"); //?
    ti.put("D!=D", "B");
    ti.put("I!=D", "B"); //?
    ti.put("+D", "D");
    ti.put("-D", "D");
        
    ti.put("L+L", "L");
    ti.put("L+I", "L");
    ti.put("L+D", "D");
    ti.put("I+L", "L"); //?
    ti.put("D+L", "D");
    ti.put("L-L", "L");
    ti.put("L-I", "L");
    ti.put("L-D", "D");
    ti.put("I-L", "L"); //?
    ti.put("D-L", "D");
    ti.put("L/L", "L");
    ti.put("L/I", "L");
    ti.put("I/L", "L"); //?
    ti.put("L%L", "L");
    ti.put("L%I", "L");
    ti.put("I%L", "L"); //?
    ti.put("L*L", "L");
    ti.put("L*I", "L");
    ti.put("L*D", "D");
    ti.put("I*L", "L"); //?
    ti.put("D*L", "D");
    ti.put("L>L", "B");
    ti.put("L>I", "B"); //?
    ti.put("L>D", "B"); //?
    ti.put("I>L", "B"); //?
    ti.put("D>L", "B"); //?
    ti.put("L<L", "B");
    ti.put("L<I", "B"); //?
    ti.put("L<D", "B"); //?
    ti.put("I<L", "B"); //?
    ti.put("D<L", "B"); //?
    ti.put("L>=L", "B");
    ti.put("L>=I", "B"); //?
    ti.put("L>=D", "B"); //?
    ti.put("I>=L", "B"); //?
    ti.put("D>=L", "B");   //?
    ti.put("L<=L", "B");
    ti.put("L<=I", "B"); //?
    ti.put("L<=D", "B"); //?
    ti.put("I<=L", "B"); //?
    ti.put("D<=L", "B"); //?
    
 
    ti.put("C+C", "C");
    ti.put("C+D", "D"); //?
    ti.put("C+L", "C");
    ti.put("C+I", "C");
    ti.put("D+C", "D");
    ti.put("L+C", "L");
    ti.put("I+C", "I");
    
    ti.put("C-C", "C");
    ti.put("C-D", "D");
    ti.put("C-L", "C");
    ti.put("C-I", "C");
    ti.put("D-C", "D");
    ti.put("L-C", "L");
    ti.put("I-C", "I");
    
    ti.put("C*C", "C");
    ti.put("C*D", "D");
    ti.put("C*L", "C");
    ti.put("C*I", "C");
    ti.put("D*C", "D");
    ti.put("L*C", "L");
    ti.put("I*C", "I");
    
    ti.put("C/C", "C");
    ti.put("C/D", "D");
    ti.put("C/L", "C");
    ti.put("C/I", "C");
    ti.put("D/C", "D");
    ti.put("L/C", "L");
    ti.put("I/C", "I");
    
    ti.put("C%C", "C");
    ti.put("C%L", "C");
    ti.put("C%I", "C");
    ti.put("L%C", "L");
    ti.put("I%C", "I");
    
    ti.put("C<C", "B");
    ti.put("C<D", "B"); //?
    ti.put("C<L", "B"); //?
    ti.put("C<I", "B"); //?
    ti.put("D<C", "B"); //?
    ti.put("L<C", "B"); //?
    ti.put("I<C", "B"); //?
    
    ti.put("C<=C", "B");
    ti.put("C<=D", "B"); //?
    ti.put("C<=L", "B"); //?
    ti.put("C<=I", "B"); //?
    ti.put("D<=C", "B"); //?
    ti.put("L<=C", "B"); //?
    ti.put("I<=C", "B");   //?
    
    ti.put("C>C", "B");
    ti.put("C>D", "B"); //?
    ti.put("C>L", "B"); //?
    ti.put("C>I", "B"); //?
    ti.put("D>C", "B"); //?
    ti.put("L>C", "B"); //?
    ti.put("I>C", "B"); //?

    ti.put("C>=C", "B");
    ti.put("C>=D", "B"); //?
    ti.put("C>=L", "B"); //?
    ti.put("C>=I", "B"); //?
    ti.put("D>=C", "B"); //?
    ti.put("L>=C", "B"); //?
    ti.put("I>=C", "B"); //?
}
}

statement : { idToType.clear(); } ((declaration | assingmnet | ioStatement) SEMICOLON)+;

ioStatement : print | println | read;

general returns [String type] :
    orExpr {
        $type = $orExpr.type;
    }
    (
        OR orExpr {
            if ($type != "B" || $orExpr.type != "B")
                throw new IllegalArgumentException("Expected Bool arguments");
            $type = ti.get($type + "||" + $orExpr.type);
        }
    )*
    ;

orExpr returns [String type] :
    andExpr {
        $type = $andExpr.type;
    }
    (
        AND andExpr {
            if ($type != "B" || $andExpr.type != "B")
                throw new IllegalArgumentException("Expected Bool arguments");
            $type = ti.get($type + "&&" + $andExpr.type);
        }
    )*
    ;

andExpr returns [String type] :
    compExpr {
        $type = $compExpr.type;
    }
    (
        EQUALSEQUALS compExpr {
            //TODO: check types
            $type = ti.get($type + "==" + $compExpr.type);
        }
        |
        NOTEQUALS compExpr {
            //TODO: check types
            $type = ti.get($type + "!=" + $compExpr.type);
        }
    )*
    ;

compExpr returns [String type] :
    NOT general {
        if ($general.type != "B")
            throw new IllegalArgumentException("Expected Bool argument");

        $type = ti.get("!" + $general.type);
    }
    |
    LBRACKET general RBRACKET {
        $type = $general.type;
    }
    |
    TRUE {
        $type = "B";
    }
    |
    FALSE {
        $type = "B";
    }
    |
    ID {
        $type = idToType.get($ID.getText());
    }
    |
    arithExpr {
        $type = $arithExpr.type;
    }
    (
        LESS_OR_EQUALS arithExpr1 {
            $type = ti.get($type + "<=" + $arithExpr1.type);
        }
        |
        GREATER_OR_EQUALS arithExpr1 {
            $type = ti.get($type + ">=" + $arithExpr1.type);
        }
        |
        LESS arithExpr1 {
            $type = ti.get($type + "<" + $arithExpr1.type);
        }
        |
        GREATER arithExpr1 {
            $type = ti.get($type + ">" + $arithExpr1.type);
        }
    )?
;

arithExpr1 returns [String type] :
    arithExpr {
        $type = $arithExpr.type;
    }
    ;

arithExpr returns [String type] :
    expression {
        $type = $expression.type;
    }
    ;

expression returns [String type] :
    term {
        $type = $term.type;
    }
    (
        PLUS term {
            $type = ti.get($type + "+" + $term.type);
        }
        |
        MINUS term {
            $type = ti.get($type + "-" + $term.type);
        }
    )*
    ;

term returns [String type] :
    factor {
        $type = $factor.type;
    }
    (
        STAR factor {
            $type = ti.get($type + "*" + $factor.type);
        }
        |
        SLASH factor {
            $type = ti.get($type + "/" + $factor.type);
        }
        |
        PERCENT factor {
            $type = ti.get($type + "%" + $factor.type);
        }
    )*
    ;

factor returns [String type] :
    ID {
        if (idToType.get($ID.getText()) == "B") //char?
            throw new IllegalStateException("Wrong type");
        $type = idToType.get($ID.getText());
    }
    |
    NUMBER {
        long value = Long.parseLong($NUMBER.getText());
        if (Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE) {
            $type = "I";
        } else {
            $type = "L";
        }
    }
    |
    DOUBLE_NUMBER {
        $type = "D";
    }
    |
    SYMBOL {
        $type = "C";
    }
    |
    CHAR {
        $type = "C";
    }
    |
    LBRACKET expression RBRACKET {
        $type = $expression.type;
    }
    |
    PLUS factor {
        $type = ti.get("+" + $factor.type);
    }
    |
    MINUS factor {
        $type = ti.get("-" + $factor.type);
    }
    ;

declaration :
    DEF ID {
        if (idToType.containsKey($ID.getText())) throw new IllegalStateException("Redeclaration");
    }
    (
        COLON typeID {
            idToType.put($ID.getText(), $typeID.type);
        }
        |
        ASSIGN
        (
            general {
                idToType.put($ID.getText(), $general.type);
            }
            |
            readWithType {
                idToType.put($ID.getText(), $readWithType.type);
            }
        )
    )
    ;

assingmnet :
    ID ASSIGN
        (
            general {
                if (idToType.get($ID.getText()) != $general.type)
                    throw new IllegalStateException("Wrong types");
            }
            |
            readWithType {
                if (idToType.get($ID.getText()) != $readWithType.type)
                    throw new IllegalStateException("Wrong types");
            }
        )
    ;

readWithType returns [String type] :
    READINT { $type = "I"; }
    |
    READBOOL { $type = "B"; }
    |
    READCHAR { $type = "C"; }
    |
    READLONG { $type = "L"; }
    |
    READDOUBLE { $type = "D"; }
    ;

read : READ ID;
print : PRINT general;
println : PRINTLN general;

typeID returns [String type] :
    INT { $type = "I"; }
    |
    BOOL { $type = "B"; }
    |
    CHAR { $type = "C"; }
    |
    LONG { $type = "L"; }
    |
    DOUBLE { $type = "D"; }
    ;



NUMBER : [0-9]+;
DOUBLE_NUMBER : NUMBER DOT NUMBER;
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
SYMBOL : QUOTE . QUOTE;

COLON : ':';
ASSIGN : '=';
DOT : '.';
QUOTE : '\'';
DOUBLEQUOTE : '"';

INT : 'Int';
BOOL : 'Bool';
CHAR : 'Char';
LONG : 'Long';
DOUBLE : 'Double';

DEF : 'def';
READINT : 'readInt';
READBOOL : 'readBool';
READCHAR : 'readChar';
READLONG : 'readLong';
READDOUBLE : 'readDouble';
READ : 'read';
PRINTLN : 'println';
PRINT : 'print';
ID : [A-Za-z][A-Za-z0-9]*;

SEMICOLON : ';';

WS: [ \n\t\r]+ -> skip;
