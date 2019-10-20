package org.expr

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test

import org.tihonovcore.utils.Early

@Early
class TestParser : TestCase() {
    class TestEvaluateExpression : TestCase() {
        @Test
        fun testAdd() {
            doTestExpressions("1 + 2", 3, "I")
            doTestExpressions("4 + 8", 12, "I")
            doTestExpressions("449 + 783", 1232, "I")
            doTestExpressions("2 + 3 + 49 + 5", 59, "I")
        }

        @Test
        fun testSubtract() {
            doTestExpressions("1 - 1", 0, "I")
            doTestExpressions("1 - 100", -99, "I")
            doTestExpressions("889 - 435", 454, "I")
            doTestExpressions("10 - 2 - 3", 5, "I")
        }

        @Test
        fun testMultiply() {
            doTestExpressions("10 * 1", 10, "I")
            doTestExpressions("19 * (-2)", -38, "I")
            doTestExpressions("106 * 23", 2438, "I")
            doTestExpressions("7 * 12 * 3", 252, "I")
        }

        @Test
        fun testDivide() {
            doTestExpressions("12 / 4", 3, "I")
            doTestExpressions("99 / 14", 7, "I")
            doTestExpressions("17 / (-2)", -8, "I")
            doTestExpressions("105 / 5 / 7", 3, "I")
            doTestExpressions("9 % 2", 1, "I")
            doTestExpressions("19 % (-2)", 1, "I")
            doTestExpressions("1060 % 230", 140, "I")
            doTestExpressions("121 % 25 % 8", 5, "I")

            doTestWithException("5 / 0")
            doTestWithException("8 % 0")
        }

        @Test
        fun testUnaryOperators() {
            doTestExpressions("+118", 118, "I")
            doTestExpressions("+(+112)", 112, "I")

            doTestExpressions("-23", -23, "I")
            doTestExpressions("-(-199)", 199, "I")
        }

        @Test
        fun testPriority() {
            doTestExpressions("2 + 2 * 2", 6, "I")
            doTestExpressions("1 + 2 * 3 / 4 - 9", -7, "I")
            doTestExpressions("10 - 6 * -23", 148, "I")
            doTestExpressions("(17 - 14) * -12 / 3 + 4", -8, "I")
        }

        @Test
        fun testOr() {
            doTestExpressions("false || false", false, "B")
            doTestExpressions("false || true", true, "B")
            doTestExpressions("true || false", true, "B")
            doTestExpressions("true || true", true, "B")
        }

        @Test
        fun testAnd() {
            doTestExpressions("false && false", false, "B")
            doTestExpressions("false && true", false, "B")
            doTestExpressions("true && false", false, "B")
            doTestExpressions("true && true", true, "B")
        }

        @Test
        fun testEquals() {
            doTestExpressions("10 == 1", false, "B")
            doTestExpressions("7 * 4 * 3 == 2 * 3 * 14", true, "B")
            doTestExpressions("9 != -2", true, "B")
            doTestExpressions("true && false != true", true, "B")
        }

        @Test
        fun testComparing() {
            doTestExpressions("10 > 1", true, "B")
            doTestExpressions("8 > 123", false, "B")
            doTestExpressions("2 + 2 > 2", true, "B")
            doTestExpressions("8 * 10 > 107", false, "B")

            doTestExpressions("1 < 14", true, "B")
            doTestExpressions("19 < -2", false, "B")
            doTestExpressions("2 + 9 < 79 * 210 / 7", true, "B")
            doTestExpressions("11 * 99 < (-2)", false, "B")

            doTestExpressions("13 <= 14", true, "B")
            doTestExpressions("14 <= 14", true, "B")
            doTestExpressions("15 <= 14", false, "B")

            doTestExpressions("80 >= 79", true, "B")
            doTestExpressions("80 >= 80", true, "B")
            doTestExpressions("80 >= 81", false, "B")
        }

        @Test
        fun testRandomExpression() {
            doTestExpressions("(1 + 2) % 3 * 4 - 5 * -6 + 7/221 > 100 && false != true", false, "B")
        }

//        @Test //NOTE: они работают(
//        fun testBadExpression() {
//            doTestWithAnyResult("1 != false", null)
//            doTestWithException("3 || 4")

////            doTestWithAnyResult("false + true", null) //'+ true' dont parse
////            doTestWithAnyResult("false < true", null) //'< true' dont parse
////            doTestWithException("1 ++ 2") //second '+' is unary
////            doTestWithException("5 > /2") //line 1:4 extraneous input '/' expecting {NUMBER, '+', '-', '('}
//        }
    }

    class TestVariables : TestCase() {
        @Test
        fun testDefineInt_TI() {
            val code = """
                def x = 10;
                print x;
            """

            val expectedType = listOf("x" to "I")
            val expectedResult = "10\n"

            doTestVariables(code, expectedType, expectedResult)
        }

        @Test
        fun testDefineBool_TI() {
            val code = """
                def x = false;
                print x;
            """

            val expectedType = listOf("x" to "B")
            val expectedResult = "false\n"

            doTestVariables(code, expectedType, expectedResult)
        }

        @Test
        fun testDefineInt() {
            val code = """
                def x: Int;
                x = 71;
                print x;
            """

            val expectedType = listOf("x" to "I")
            val expectedResult = "71\n"

            doTestVariables(code, expectedType, expectedResult)
        }

        @Test
        fun testDefineBool() {
            val code = """
                def x: Bool;
                x = true;
                print x;
            """

            val expectedType = listOf("x" to "B")
            val expectedResult = "true\n"

            doTestVariables(code, expectedType, expectedResult)
        }

        @Test
        fun testAssignBool() {
            val code = """
                def x: Bool;
                def y: Bool;
                x = 5 * 7 + 4 - 2 == 200;
                y = 5 * 7 + 4 - 2 == 37;
                print x;
                print y;
            """

            val expectedType = listOf("x" to "B", "y" to "B")
            val expectedResult = "falsetrue\n"

            doTestVariables(code, expectedType, expectedResult)
        }

        @Test
        fun testAssignInt() {
            val code = """
                def x: Int;
                def y: Int;
                x = 14 / 7 * 5 + 10;
                y = 35 / 6 / 2 + 118;
                print x + y;
            """

            val expectedType = listOf("x" to "I", "y" to "I")
            val expectedResult = "140\n"

            doTestVariables(code, expectedType, expectedResult)
        }
    }
//
//    class TestIO : TestCase() {
//        @Test
//        fun testIO1() {
//            val code = """
//                def x: Int;
//                def y: Bool;
//                read x;
//                read y;
//                print x > 2 == y;
//
//                def z = readInt;
//                def q = readBool;
//                print z * z == z || q;
//            """
//
//            val expectedType = listOf<Pair<String, String>>()
//            val expectedResult = "140\n"
//
//            doTestVariables(code, expectedType, expectedResult)
//        }
//    }
}

@Early
private fun doTestVariables(sourceCode: String, expectedTypesByName: List<Pair<String, String?>>, expectedResult: String) {
    val compiler = MPLCompiler()

    Assert.assertEquals(expectedResult, compiler.evaluate(sourceCode))
    for (nt in expectedTypesByName) {
        Assert.assertEquals(compiler.lastTypeMap.toString(), nt.second, compiler.lastTypeMap[nt.first])
    }
}

@Early
private fun doTestExpressions(input: String, expectedResult: Any, expectedType: String?) {
    val compiler = MPLCompiler()

    val sourceCode = """
        def x = $input;
        print x;
    """.trimIndent()
    
    Assert.assertEquals(expectedResult.toString() + "\n", compiler.evaluate(sourceCode))
    Assert.assertEquals(expectedType, compiler.lastTypeMap["x"])
}

@Early
private fun doTestWithException(input: String, expectedResult: Any = 0) {
    try {
        doTestExpressions(input, expectedResult, null)
    } catch (e: Exception) {
        return
    }

    assert(false)
}
