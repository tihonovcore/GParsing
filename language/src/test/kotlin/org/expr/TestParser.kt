package org.expr

import org.tihonovcore.utils.Early

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

abstract class TestParser : TestCase()

class TestEvaluateExpression : TestParser() {
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

//        doTestWithException("5 / 0")
//        doTestWithException("8 % 0")
    }

    @Test
    fun testUnaryOperators() {
        doTest("+118", 118, "I")
        doTest("+(+112)", 112, "I")

        doTest("-23", -23, "I")
        doTest("-(-199)", 199, "I")
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
        doTest("(1 + 2) % 3 * 4 - 5 * -6 + 7/221 > 100 && false != true", false, "B")
    }

    private fun doTest(input: String, expectedResult: Any, expectedType: String?) {
        val compiler = MPLCompiler()

        val sourceCode = """
            |def x = $input;
            |print x;
        """.trimMargin()

        Assert.assertEquals(expectedResult.toString() + "\n", compiler.evaluate(sourceCode))
        Assert.assertEquals(expectedType, compiler.lastTypeMap["x"])
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

class TestTypes : TestParser() {
    @Test
    fun testDouble() {
        doTest("4.5", "4.500000", "D")
        doTest("4 + 0.5", "4.500000", "D")
        doTest("100 * 4.5", "450.000000", "D")
    }

    @Test
    fun testChar() {
        doTest("'a'", "a", "C")
        doTest("'a' + 5", "f", "C")
    }

    @Test
    fun testLong() {
        doTest("56789071123456789", "56789071123456789", "L")
        doTest("56789071123456789 + 1", "56789071123456790", "L")
        doTest("56789071123456789 / 1000000", "56789071123", "L")
    }

    @Early
    private fun doTest(input: String, expectedResult: Any, expectedType: String?) {
        val compiler = MPLCompiler()

        val sourceCode = """
            |def x = $input;
            |print x;
        """.trimMargin()

        Assert.assertEquals(expectedResult.toString() + "\n", compiler.evaluate(sourceCode))
        Assert.assertEquals(expectedType, compiler.lastTypeMap["x"])
    }
}

class TestVariables : TestParser() {
    @Test
    fun testDefineInt_TI() {
        val code = """
                def x = 10;
                print x;
            """

        val expectedType = listOf("x" to "I")
        val expectedResult = "10\n"

        doTest(code, expectedType, expectedResult)
    }

    @Test
    fun testDefineBool_TI() {
        val code = """
                def x = false;
                print x;
            """

        val expectedType = listOf("x" to "B")
        val expectedResult = "false\n"

        doTest(code, expectedType, expectedResult)
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

        doTest(code, expectedType, expectedResult)
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

        doTest(code, expectedType, expectedResult)
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

        doTest(code, expectedType, expectedResult)
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

        doTest(code, expectedType, expectedResult)
    }

    @Test
    fun testSmth() {
        val code = """
                def printfdsf: Int;
                printfdsf = 10;
                print printfdsf;
            """

        val expectedType = listOf("printfdsf" to "I")
        val expectedResult = "10\n"

        doTest(code, expectedType, expectedResult)
    }

    private fun doTest(
        sourceCode: String,
        expectedTypesByName: List<Pair<String, String?>>,
        expectedResult: String
    ) {
        val compiler = MPLCompiler()

        Assert.assertEquals(expectedResult, compiler.evaluate(sourceCode))
        for (nameTypePair in expectedTypesByName) {
            Assert.assertEquals(
                compiler.lastTypeMap.toString(),
                nameTypePair.second,
                compiler.lastTypeMap[nameTypePair.first]
            )
        }
    }
}

@Early
class TestIO : TestParser() {
    @Test
    fun testIO1() {
        doTest(Paths.get("./src/test/kotlin/org/expr/io/$name"))
    }

    @Test
    fun testIoWithTi() {
        doTest(Paths.get("./src/test/kotlin/org/expr/io/$name"))
    }

    @Test
    fun testIoWithTi_2() {
        doTest(Paths.get("./src/test/kotlin/org/expr/io/$name"))
    }

    @Early
    private fun doTest(path: Path) {
        val lines = Files.readAllLines(path)
        val c = lines.indexOf("@code")
        val t = lines.indexOf("@types")
        val i = lines.indexOf("@input")
        val r = lines.indexOf("@result")
        val e = lines.indexOf("@end")

        val code = lines.subList(c + 1, t).joinToString(System.lineSeparator()) { it }
        val expectedType = lines.subList(t + 1, i).filter { it.isNotEmpty() }.map {
            val arr = it.split(" to ")
            Pair(arr[0], arr[1])
        }
        val input = lines.subList(i + 1, r)
        val expectedResult = lines.subList(r + 1, e).joinToString(System.lineSeparator()) { it }

        doTest(code, input, expectedType, expectedResult)
    }

    @Early
    private fun doTest(
        sourceCode: String,
        input: List<Any>,
        expectedTypesByName: List<Pair<String, String?>>,
        expectedResult: String
    ) {
        val compiler = MPLCompiler()

        Assert.assertEquals(expectedResult, compiler.evaluate(sourceCode, input))
        for (nameTypePair in expectedTypesByName) {
            Assert.assertEquals(
                compiler.lastTypeMap.toString(),
                nameTypePair.second,
                compiler.lastTypeMap[nameTypePair.first]
            )
        }
    }
}
