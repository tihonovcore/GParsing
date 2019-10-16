package org.expr;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.expr.gen.*;

import java.util.Scanner;

public class Main {
    public static void main( String[] args) throws Exception
    {
        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < 100; i++) {
            String line = scanner.nextLine();

            ExprLexer lexer = new ExprLexer(CharStreams.fromString(line));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ExprParser parser = new ExprParser(tokens);
            ExprParser.GeneralContext result = parser.general();
            System.out.println("-> " + result.value + " [" + result.type + "]");
        }

//        walker.walk(new ExprWalker(), tree);
    }
}
