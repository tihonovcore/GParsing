package org.expr

import junit.framework.TestCase
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.expr.gen.ExprLexer
import org.expr.gen.ExprParser
import org.junit.Assert
import org.junit.Test

import org.tihonovcore.utils.Early

@Early
class TestParser : TestCase() {
    class TestEvaluateExpression : TestCase() {
        @Test
        fun testAdd() {
            doTest("1 + 2", 3, "I")
            doTest("4 + 8", 12, "I")
            doTest("449 + 783", 1232, "I")
            doTest("2 + 3 + 49 + 5", 59, "I")
        }

        @Test
        fun testSubtract() {
            doTest("1 - 1", 0, "I")
            doTest("1 - 100", -99, "I")
            doTest("889 - 435", 454, "I")
            doTest("10 - 2 - 3", 5, "I")
        }

        @Test
        fun testMultiply() {
            doTest("10 * 1", 10, "I")
            doTest("19 * (-2)", -38, "I")
            doTest("106 * 23", 2438, "I")
            doTest("7 * 12 * 3", 252, "I")
        }

        @Test
        fun testDivide() {
            doTest("12 / 4", 3, "I")
            doTest("99 / 14", 7, "I")
            doTest("17 / (-2)", -8, "I")
            doTest("105 / 5 / 7", 3, "I")
            doTest("9 % 2", 1, "I")
            doTest("19 % (-2)", 1, "I")
            doTest("1060 % 230", 140, "I")
            doTest("121 % 25 % 8", 5, "I")

            doTestWithException("5 / 0")
            doTestWithException("8 % 0")
        }

        @Test
        fun testUnaryOperators() {
            doTest("+118", 118, "I")
            doTest("+(+112)", 112, "I") //null

            doTest("-23", -23, "I")
            doTest("-(-199)", 199, "I") //null
        }

        @Test
        fun testPriority() {
            doTest("2 + 2 * 2", 6, "I")
            doTest("1 + 2 * 3 / 4 - 9", -7, "I")
            doTest("10 - 6 * -23", 148, "I")
            doTest("(17 - 14) * -12 / 3 + 4", -8, "I")
        }

        @Test
        fun testOr() {
            doTest("false || false", false, "B")
            doTest("false || true", true, "B")
            doTest("true || false", true, "B")
            doTest("true || true", true, "B")
        }

        @Test
        fun testAnd() {
            doTest("false && false", false, "B")
            doTest("false && true", false, "B")
            doTest("true && false", false, "B")
            doTest("true && true", true, "B")
        }

        @Test
        fun testEquals() {
            doTest("10 == 1", false, "B")
            doTest("7 * 4 * 3 == 2 * 3 * 14", true, "B")
            doTest("9 != -2", true, "B")
            doTest("true && false != true", true, "B")
        }

        @Test
        fun testComparing() {
            doTest("10 > 1", true, "B")
            doTest("8 > 123", false, "B")
            doTest("2 + 2 > 2", true, "B")
            doTest("8 * 10 > 107", false, "B")
            doTest("1 < 14", true, "B")
            doTest("19 < -2", false, "B")
            doTest("2 + 9 < 79 * 210 / 7", true, "B")
            doTest("11 * 99 < (-2)", false, "B")
            doTest("13 <= 14", true, "B")
            doTest("14 <= 14", true, "B")
            doTest("15 <= 14", false, "B")
            doTest("80 >= 79", true, "B")
            doTest("80 >= 80", true, "B")
            doTest("80 >= 81", false, "B")
        }

        @Test
        fun testRandomExpression() {
            doTest("(1+ 2) % 3 * 4 - 5 * -6 + 7/221 > 100 && false != true", false, "B")
        }

        @Test //NOTE: они работают(
        fun testBadExpression() {
//            doTestWithException("1 ++ 2")
//            doTestWithException("5 > /2")
//            doTestWithException("1 != false")
//            doTestWithException("3 || 4")
//            doTestWithException("false + true")
//            doTestWithException("false < true")
        }
    }
}

@Early
private fun doTest(input: String, expectedResult: Any, expectedType: String?) {
    val lexer = ExprLexer(CharStreams.fromString(input))
    val tokens = CommonTokenStream(lexer)
    val parser = ExprParser(tokens)
    val tree = parser.eval()

    Assert.assertEquals("Expression is '$input'", expectedType, tree.general.type)
    Assert.assertEquals("Expression is '$input'", expectedResult, tree.general.value)
}

@Early
private fun doTestWithException(input: String, expectedResult: Any = 0) {
    try {
        doTest(input, expectedResult, null)
    } catch (e: Exception) {
        return
    }

    assert(false)
}
