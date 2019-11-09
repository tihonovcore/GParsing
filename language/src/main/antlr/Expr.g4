grammar Expr;

@header {
import java.util.*;
import org.expr.GParseException;
}

@members {

public String mySource = "";
void error(String message, ParserRuleContext _localctx) {
    throw new GParseException(
        message,
        _localctx.start.getLine(),
        _localctx.start.getCharPositionInLine(),
        mySource
    );
}

static boolean NOTEQ(String a, String b) {
    return !a.equals(b);
}

int head = 0;
private List<Map<String, String>> parent = new ArrayList<>() {{
    add(new HashMap<>());
}};
public List<Map<String, String>> current = new ArrayList<>() {{
    add(new HashMap<>());
}};
public List<Map.Entry<String, String>> declarations = new ArrayList<>();

String getType(String id) {
    Map<String, String> myParent = parent.get(head);
    Map<String, String> myCurrent = current.get(head);

    if (myCurrent.containsKey(id)) {
        return myCurrent.get(id);
    }

    if (myParent.containsKey(id)) {
        return myParent.get(id);
    }

    throw new IllegalArgumentException("Undefined variable: " + id);
}

void setType(String id, String type) {
    Map<String, String> myCurrent = current.get(head);
    if (myCurrent.containsKey(id)) {
        throw new IllegalArgumentException("Redefinition variable: " + id);
    }

    myCurrent.put(id, type);
    declarations.add(Map.entry(id, type));
}

void newScope() {
    Map<String, String> newParent = new HashMap<>(parent.get(head));
    for (String key : current.get(head).keySet()) {
        newParent.put(key, current.get(head).get(key));
    }

    head++;
    current.add(new HashMap<>());
    parent.add(newParent);
}

void newFunctionScope() {
    head++;
    current.add(new HashMap<>());
    parent.add(new HashMap<>());
}

void outOfScope() {
    current.remove(head);
    parent.remove(head);
    head--;
}

public Map<String, String> idToType = new HashMap<>();
private Map<String, String> definedFunctions = new HashMap<>();

private String currentReturnType = "U";
private boolean returnExistsFlag = false;
private int insideWhileBlock = 0;

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
    ti.put("L==L", "B");
    ti.put("L==I", "B"); //?
    ti.put("L==D", "B"); //?
    ti.put("I==L", "B"); //?
    ti.put("D==L", "B"); //?
    ti.put("L!=L", "B");
    ti.put("L!=I", "B"); //?
    ti.put("L!=D", "B"); //?
    ti.put("I!=L", "B"); //?
    ti.put("D!=L", "B"); //?
    
 
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

    ti.put("C==C", "B");
    ti.put("C==D", "B"); //?
    ti.put("C==L", "B"); //?
    ti.put("C==I", "B"); //?
    ti.put("D==C", "B"); //?
    ti.put("L==C", "B"); //?
    ti.put("I==C", "B"); //?

    ti.put("C!=C", "B");
    ti.put("C!=D", "B"); //?
    ti.put("C!=L", "B"); //?
    ti.put("C!=I", "B"); //?
    ti.put("D!=C", "B"); //?
    ti.put("L!=C", "B"); //?
    ti.put("I!=C", "B"); //?

}
}

file :
    (statement | function)*
    ;

statement :
    (
        declaration
        |
        assingmnet
        |
        ioStatement
        |
        jumpStatement
        |
        returnStatement[currentReturnType]
        |
        id_call
    ) SEMICOLON
    |
    ifStatement
    |
    whileStatement
    ;

ioStatement : print | println | read;

general returns [String type] :
    cast { $type = $cast.type; }
    ;

cast returns [String type] :
    orExpr {
        $type = $orExpr.type;
    }
    (
        OR orExpr {
            if (NOTEQ($type, "B") || NOTEQ($orExpr.type, "B"))
                error("Expected Bool arguments", _localctx);
            $type = ti.get($type + "||" + $orExpr.type);
        }
    )*
    (
        AS
        typeID {
            List<String> numbers = new ArrayList<>() {{ add("I"); add("C"); add("L"); add("D"); }};

            if (
                $orExpr.type.equals($typeID.type)
                ||
                $typeID.type.equals("S")
                ||
                numbers.contains($typeID.type) && numbers.contains($orExpr.type)
            ) {
                $type = $typeID.type;
            } else {
                error("Impossible cast " + $orExpr.type + " to " + $typeID.type, _localctx);
            }
        }
    )?
    ;

orExpr returns [String type] :
    andExpr {
        $type = $andExpr.type;
    }
    (
        AND andExpr {
            if (NOTEQ($type, "B") || NOTEQ($andExpr.type, "B"))
                error("Expected Bool arguments", _localctx);
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
            error("Expected Bool argument", _localctx);

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
        $type = getType($ID.getText());
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
    ID
    (
        get[getType($ID.getText())] {
            String recieverType = getType($ID.getText());
            if (recieverType == "S") { //TODO: EQEQ
                $type = "C";
            } else if (recieverType.startsWith("A")) {
                $type = recieverType.substring(1, recieverType.length());
            } else {
                error("Expected Iterable type", _localctx);
            }
        }
        |
        call[$ID.getText()] {
            $type = $call.type;
        }
    )? {
        if (_localctx.call() == null && _localctx.get() == null) {
            $type = getType($ID.getText());
        }
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
    |
    STRINGVALUE { $type = "S"; }
    |
    iterableSize { $type = "I"; }
    ;

get [String recieverType] returns [String type] :
    SqLB general SqRB {
        if ($general.type != "I")
            error("Index should be Int", _localctx);

        if ($recieverType == "S") {
            $type = "C";
        } else if ($recieverType.startsWith("A")) {
            $type = $recieverType.substring(1, $recieverType.length());
        }
    }
    ;

declaration :
    DEF ID
    (
        COLON typeID {
            setType($ID.getText(), $typeID.type);
        }
        |
        ASSIGN
        (
            general {
                setType($ID.getText(), $general.type);
            }
            |
            readWithType {
                setType($ID.getText(), $readWithType.type);
            }
            |
            STRINGVALUE {
                setType($ID.getText(), "S");
            }
            |
            array {
                setType($ID.getText(), $array.type);
            }
            |
            concat {
                setType($ID.getText(), $concat.type);
            }
        )
    )
    ;

assingmnet :
    ID
    (
        get[getType($ID.getText())] {
            String recieverType = getType($ID.getText());
            if (NOTEQ(recieverType, "S") && !recieverType.startsWith("A"))
                error("Expected Iterable type", _localctx);
        }
    )?
    ASSIGN
    (
        general {
            if (_localctx.get != null) {
                if (NOTEQ($get.type, $general.type))
                    error("Wrong types: expected " + $get.type + ", but was " + $general.type, _localctx);
            } else if (NOTEQ(getType($ID.getText()), $general.type)) {
                error("Wrong types: expected " + getType($ID.getText()) + ", but was " + $general.type, _localctx);
            }
        }
        |
        readWithType {
            if (_localctx.get != null) {
                if (NOTEQ($get.type, $readWithType.type))
                    error("Wrong types: expected " + $get.type, _localctx);
            } else if (NOTEQ(getType($ID.getText()), $readWithType.type)) {
                error("Wrong types: expected " + getType($ID.getText()), _localctx);
            }
        }
        |
        STRINGVALUE {
            if (NOTEQ(getType($ID.getText()), "S"))
                error("Wrong types: expected " + getType($ID.getText()) + ", but was String", _localctx);
        }
        |
        array {
            if (NOTEQ(getType($ID.getText()), $array.type))
                error("Wrong types: expected " + getType($ID.getText()) + ", but was " + $array.type, _localctx);
        }
        |
        concat {
            if (NOTEQ(getType($ID.getText()), $concat.type))
                error("Wrong types: expected " + getType($ID.getText()) + ", but was " + $concat.type, _localctx);
        }
    )
    ;

array returns [String type] :
    arrayType LBRACKET general RBRACKET {
        $type = $arrayType.type;
    }
    ;

iterableSize :
    ID DOT SIZE {
        String type = getType($ID.getText());
        if (NOTEQ(type, "S") && !type.startsWith("A"))
            error("Expected Array or String, but was: " + getType($ID.getText()), _localctx);
    }
    ;

concat returns [String ltype, String rtype, String type]:
    CONCAT
    LBRACKET
        (ID { $ltype = getType($ID.getText()); } | STRINGVALUE { $ltype = "S"; })
        COMMA
        (ID { $rtype = getType($ID.getText()); } | STRINGVALUE { $rtype = "S"; })
    RBRACKET {
        if ($ltype.equals($rtype) && ($ltype.equals("S") || $ltype.startsWith("A"))) {
            $type = $ltype;
        } else {
            error("Both arguments should have the same Iterable type, but was: " + $ltype + " and " + $rtype, _localctx); //TODO: поправтиь тоже
        }
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

print : PRINT general;
println : PRINTLN general;
read returns [String type]: READ ID ( get[getType($ID.getText())] )?
    {
        if (_localctx.get != null) $type = $get.type;
        else $type = getType($ID.getText());
    }
    ;

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
    arrayType { $type = $arrayType.type; }
    ;

arrayType returns [String type] :
    ARRAY SqLB typeID SqRB { $type = "A" + $typeID.type; }
    ;

ifStatement :
    IF LBRACKET general RBRACKET { newScope(); } (statement | body) { outOfScope(); } {
        if (NOTEQ($general.type, "B"))
            error("Condition should be Bool, but was " + $general.type, _localctx);
    }
    (
        ELSE { newScope(); } (statement | body) { outOfScope(); }
    )?
    ;

body :
    OpenBlockBrace statement* CloseBlockBrace
    ;

whileStatement :
    WHILE LBRACKET general RBRACKET { insideWhileBlock++; } { newScope(); } (statement | body) { outOfScope(); } {
        insideWhileBlock--;

        if (NOTEQ($general.type, "B"))
            error("Condition should be Bool, but was " + $general.type, _localctx);
        }
    ;

function :
    FUN ID LBRACKET { newFunctionScope(); } functionArguments RBRACKET
    returnType {
        currentReturnType = $returnType.type;

        String signature = $ID.getText() + "#" + $functionArguments.types;

        if (definedFunctions.containsKey(signature)) {
            error("Redefinition function", _localctx);
        }

        if ($returnType.type.startsWith("A") || $returnType.type.equals("S")) {
            error("Return type not supported yet: " + $returnType.type, _localctx);
        }

        definedFunctions.put(signature, $returnType.type);
    }
    body {
        if (NOTEQ($returnType.type, "U") && !returnExistsFlag) {
            error("Return missed", _localctx);
        }

        returnExistsFlag = false;
        currentReturnType = "U";
    } { outOfScope(); }
    ;

returnType returns [String type]:
    (COLON typeID)? {
        if (_localctx.typeID == null) {
            $type = "U";
        } else {
            $type = $typeID.type;
        }
    }
    ;

functionArguments returns [String types]:
    (
        ID COLON typeID { $types = $typeID.type; setType($ID.getText(), $typeID.type); }
        (COMMA ID COLON typeID { $types += "_" + $typeID.type; setType($ID.getText(), $typeID.type); })*
    )?
    ;

returnStatement [String expectedType] :
    RETURN
    (
        general
    )? {
        returnExistsFlag = true;

        if (_localctx.general == null) {
            if (NOTEQ($expectedType, "U")) {
                error("Expected return expression", _localctx);
            }
        } else {
            if (NOTEQ($expectedType, $general.type)) {
                error("Unexpected type of return expression: " + $general.type + ", should be " + $expectedType, _localctx);
            }
        }
    }
    ;

id_call :
    ID call[$ID.getText()]
    ;

call [String id] returns [String type] :
    LBRACKET callArguments RBRACKET {
        String signature = id + "#" + $callArguments.types;
        String returnType = definedFunctions.get(signature);

        if (returnType == null) {
            error("Undefined function: " + signature, _localctx);
        }

        $type = returnType;
    }
    ;

callArguments returns [String types]:
    (
        general { $types = $general.type; }
        (COMMA general { $types += "_" + $general.type; })*
    )?
    ;

jumpStatement :
    CONTINUE | BREAK {
        if (insideWhileBlock == 0) {
            error("Unexpected jump statement", _localctx);
        }
    }
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
OpenBlockBrace : '{';
CloseBlockBrace : '}';

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

SIZE : 'size';

AS : 'as';

IF : 'if';
ELSE : 'else';
WHILE : 'while';

DEF : 'def';
FUN : 'fun';

BREAK : 'break';
CONTINUE : 'continue';
RETURN : 'return';

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
