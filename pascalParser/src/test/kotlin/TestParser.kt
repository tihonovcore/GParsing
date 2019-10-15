import org.tihonovcore.pascal.*
import org.tihonovcore.utils.Early

import org.junit.Test
import junit.framework.TestCase


@Early
class TestParser : TestCase() {

    @Test
    fun testFunctionWithoutArguments() {
        doTest("function f(): Integer")
    }

    @Test
    fun testProcedureWithoutArguments() {
        doTest("procedure f()")
    }

    @Test
    fun testFunctionWithOneArgument() {
        doTest("function f(a: Integer): Integer")
    }

    @Test
    fun testFunctionWithTwoArgument() {
        doTest("function nameOo(a: Integer, b: Type): String")
    }

    @Test
    fun testFunctionWithThreeArgument() {
        doTest("function nameOo(a: Integer, b: Type, third: Queue): String")
    }

    @Test
    fun testFunctionWithoutReturnType() {
        doTestWithException("function missedReturnType(a: Integer, b: Type)")
    }

    @Test
    fun testProcedureWithReturnType() {
        doTestWithException("procedure extraReturnType(a: Integer, b: Type): Integer")
    }

    @Test
    fun testProcedure() {
        doTest("procedure as1234567dff23456(z2: Type)")
    }

    @Test
    fun testNoColonBetweenNameAndType() {
        doTestWithException("procedure missedCOLON(z2 Type)")
    }

    @Test
    fun testProblemWithCaseInKeyword() {
        doTestWithException("pRoCeDure joke()")
    }

    @Test
    fun testNameStartWithNumber() {
        doTestWithException("procedure 2nameStartWithNumber(z2: Type)")
    }

    @Test
    fun testArgumentWithoutType() {
        doTestWithException("function helloFromJS(x, y, z)")
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
