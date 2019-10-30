grammar Expr;

@header {
import java.util.*;
}

@members {
static boolean NOTEQ(String a, String b) {
    return !a.equals(b);
}

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

statement : ((declaration | assingmnet | ioStatement) SEMICOLON)+;

ioStatement : print | println | read;

general returns [String type] :
    orExpr {
        $type = $orExpr.type;
    }
    (
        OR orExpr {
            if (NOTEQ($type, "B") || NOTEQ($orExpr.type, "B"))
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
            if (NOTEQ($type, "B") || NOTEQ($andExpr.type, "B"))
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
        if (NOTEQ($general.type, "B"))
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
        //NOTE: it can be String
        if (idToType.get($ID.getText()) == "B") //char? //TODO: EQEQ
            throw new IllegalStateException("Wrong type"); //точно?
        $type = idToType.get($ID.getText());
    }
    (
        get[$type] {
            String recieverType = idToType.get($ID.getText());
            if (recieverType == "S") { //TODO: EQEQ
                $type = "C";
            } else if (recieverType.startsWith("A")) {
                $type = recieverType.substring(1, recieverType.length());
            } else {
                throw new IllegalStateException("Expected Iterable type");
            }
        }
    )?
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
    |
    STRINGVALUE { $type = "S"; }
    ;

get [String recieverType] returns [String type] :
    SqLB general SqRB {
        if ($general.type != "I")
            throw new IllegalArgumentException("Excpected Int");

        if ($recieverType == "S") {
            $type = "C";
        } else if ($recieverType.startsWith("A")) {
            $type = $recieverType.substring(1, $recieverType.length());
        }
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
            |
            STRINGVALUE {
                idToType.put($ID.getText(), "S");
            }
            |
            array {
                idToType.put($ID.getText(), $array.type);
            }
            |
            concat {
                idToType.put($ID.getText(), $concat.type);
            }
        )
    )
    ;

assingmnet :
    ID
    (
        get[idToType.get($ID.getText())] {
            String recieverType = idToType.get($ID.getText());
            if (NOTEQ(recieverType, "S") && !recieverType.startsWith("A"))
                throw new IllegalStateException("Expected Iterable type");
        }
    )?
    ASSIGN
    (
        general {
            if (_localctx.get != null) {
                if (NOTEQ($get.type, $general.type))
                    throw new IllegalStateException("Wrong types");
            } else if (NOTEQ(idToType.get($ID.getText()), $general.type)) {
                throw new IllegalStateException("Wrong types");
            }
        }
        |
        readWithType {
            if (idToType.get($ID.getText()) != $readWithType.type)
                throw new IllegalStateException("Wrong types");
        }
        |
        STRINGVALUE {
            if (NOTEQ(idToType.get($ID.getText()), "S"))
                throw new IllegalStateException("Wrong types");
        }
        |
        array {
            if (NOTEQ(idToType.get($ID.getText()), $array.type))
                throw new IllegalStateException("Wrong types");
        }
        |
        concat {
            if (NOTEQ(idToType.get($ID.getText()), $concat.type))
                throw new IllegalStateException("Wrong types");
        }
    )
    ;

array returns [String type] :
    typeID LBRACKET general RBRACKET {
        if (!$typeID.type.startsWith("A"))
            throw new IllegalArgumentException("Expected array");
        $type = $typeID.type;
    }
    ;

concat returns [String ltype, String rtype, String type]:
    CONCAT
    LBRACKET
        (ID { $ltype = idToType.get($ID.getText()); } | STRINGVALUE { $ltype = "S"; } | array { $ltype = $array.type; })
        COMMA
        (ID { $rtype = idToType.get($ID.getText()); } | STRINGVALUE { $rtype = "S"; } | array { $rtype = $array.type; })
    RBRACKET {
        String result = "";

        if ($ltype.equals($rtype)) {
            $type = $ltype;
            result = "OK";
        }

        if ($ltype.equals("C") && $rtype.equals("S") || $ltype.equals("S") && $rtype.equals("C")) {
            $type = "S";
            result = "OK";
        }

        if ($ltype.startsWith("A")) {
            String ltypeParameter = $ltype.substring(1, $ltype.length());
            if (ltypeParameter.equals($rtype)) {
                $type = $ltype;
                result = "OK";
            }
        }

        if ($rtype.startsWith("A")) {
            String rtypeParameter = $rtype.substring(1, $rtype.length());
            if (rtypeParameter.equals($rtype)) {
                $type = $rtype;
                result = "OK";
            }
        }

        if (NOTEQ(result, "OK")) throw new IllegalArgumentException("Incompatible type in `concat`");
    }
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
    |
    READSTRING { $type = "S"; }
    |
    READLINE { $type = "S"; }
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
    |
    STRING { $type = "S"; }
    |
    ARRAY SqLB typeID SqRB { $type = "A" + $typeID.type; }
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
SqLB : '[';
SqRB : ']';

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
COMMA : ',';
QUOTE : '\'';

SLASHN : '\\n';
SLASHSLASH : '\\\\';
SLASHDOLLAR : '\\$';

STRINGVALUE : '"' (~('\\' | '$' |'"') | SLASHN | SLASHSLASH | SLASHDOLLAR)* '"';

INT : 'Int';
BOOL : 'Bool';
CHAR : 'Char';
LONG : 'Long';
DOUBLE : 'Double';
STRING : 'String';
ARRAY : 'Array';

DEF : 'def';

CONCAT : 'concat';

READINT : 'readInt';
READBOOL : 'readBool';
READCHAR : 'readChar';
READLONG : 'readLong';
READDOUBLE : 'readDouble';
READSTRING : 'readString';
READLINE : 'readLine';

READ : 'read';
PRINTLN : 'println';
PRINT : 'print';
ID : [A-Za-z][A-Za-z0-9]*;

SEMICOLON : ';';

WS: [ \n\t\r]+ -> skip;
