import org.tihonovcore.pascal.*

import org.junit.Test
import junit.framework.TestCase

class TestParser : TestCase() {

    @Test
    fun testFunctionWithoutArguments() {
        doTest("function f(): integer;")
    }

    @Test
    fun testProcedureWithoutArguments() {
        doTest("procedure f();")
    }

    @Test
    fun testFunctionWithOneArgument() {
        doTest("function f(a: real): string;")
    }

    @Test
    fun testFunctionWithTwoArgument() {
        doTest("function nameOo(a: string, b: char): integer;")
    }

    @Test
    fun testFunctionWithThreeArgument() {
        doTest("function nameOo(a: integer, b: real, third: real): string;")
    }

    @Test
    fun testFunctionWithoutReturnType() {
        doTestWithException("function missedReturnType(a: char, b: real);")
    }

    @Test
    fun testSameParameters() {
        doTestWithException("function badParams(a: char, a: char);")
    }

    @Test
    fun testProcedureWithReturnType() {
        doTestWithException("procedure extraReturnType(a: integer, b: char): string;")
    }

    @Test
    fun testProcedure() {
        doTest("procedure as1234567dff23456(z2: real);")
    }

    @Test
    fun testNoColonBetweenNameAndType() {
        doTestWithException("procedure missedCOLON(z2 char);")
    }

    @Test
    fun testProblemWithCaseInKeyword() {
        doTestWithException("pRoCeDure joke();")
    }

    @Test
    fun testNameStartWithNumber() {
        doTestWithException("procedure 2nameStartWithNumber(z2: char);")
    }

    @Test
    fun testArgumentWithoutType() {
        doTestWithException("function helloFromJS(x, y, z);")
    }

    @Test
    fun testIdWithKeyAsSubstring() {
        doTest("function functionfunction(): integer;")
    }

    @Test
    fun testExtraToken() {
        doTestWithException("function foo(): integer boolean;")
    }

    @Test
    fun testUnexpectedSymbol() {
        doTestWithException("function λfun(): real;")
    }

    @Test
    fun testUnexpectedType() {
        doTestWithException("function fun(): intERNATIONAL;")
    }

    @Test
    fun testWhiteSpace() {
        doTest("  function   fun   (   a :  real  ) :  real   ;")
    }

    private fun doTest(input: String) {
        var render = ""
        try {
            val tokens = Lexer().getTokens(input)
            val parseResult = PascalParser(tokens).parse()
            render = RenderVisitor(parseResult).visit()
        } catch (e: Exception) {
            render = e.message ?: "unexpected exception"
            throw e
        } finally {
            println(input)
            println(render)
            println()
        }
    }

    private fun doTestWithException(input: String) {
        try {
            doTest(input)
        } catch (e: Exception) {
            return
        }

        assert(false)
    }
}
