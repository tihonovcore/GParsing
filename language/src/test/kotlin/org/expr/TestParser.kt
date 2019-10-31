package org.expr

import org.tihonovcore.utils.Early

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

abstract class TestParser : TestCase() {
    protected fun doTest(path: Path) {
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

    protected fun doTest(
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

class TestEvaluateExpression : TestParser() {
    @Test
    fun testAdd() {
        doTestVariables("1 + 2", 3, "I")
        doTestVariables("4 + 8", 12, "I")
        doTestVariables("449 + 783", 1232, "I")
        doTestVariables("2 + 3 + 49 + 5", 59, "I")
    }

    @Test
    fun testSubtract() {
        doTestVariables("1 - 1", 0, "I")
        doTestVariables("1 - 100", -99, "I")
        doTestVariables("889 - 435", 454, "I")
        doTestVariables("10 - 2 - 3", 5, "I")
    }

    @Test
    fun testMultiply() {
        doTestVariables("10 * 1", 10, "I")
        doTestVariables("19 * (-2)", -38, "I")
        doTestVariables("106 * 23", 2438, "I")
        doTestVariables("7 * 12 * 3", 252, "I")
    }

    @Test
    fun testDivide() {
        doTestVariables("12 / 4", 3, "I")
        doTestVariables("99 / 14", 7, "I")
        doTestVariables("17 / (-2)", -8, "I")
        doTestVariables("105 / 5 / 7", 3, "I")
        doTestVariables("9 % 2", 1, "I")
        doTestVariables("19 % (-2)", 1, "I")
        doTestVariables("1060 % 230", 140, "I")
        doTestVariables("121 % 25 % 8", 5, "I")

//        doTestWithException("5 / 0")
//        doTestWithException("8 % 0")
    }

    @Test
    fun testUnaryOperators() {
        doTestVariables("+118", 118, "I")
        doTestVariables("+(+112)", 112, "I")

        doTestVariables("-23", -23, "I")
        doTestVariables("-(-199)", 199, "I")
    }

    @Test
    fun testPriority() {
        doTestVariables("2 + 2 * 2", 6, "I")
        doTestVariables("1 + 2 * 3 / 4 - 9", -7, "I")
        doTestVariables("10 - 6 * -23", 148, "I")
        doTestVariables("(17 - 14) * -12 / 3 + 4", -8, "I")
    }

    @Test
    fun testOr() {
        doTestVariables("false || false", false, "B")
        doTestVariables("false || true", true, "B")
        doTestVariables("true || false", true, "B")
        doTestVariables("true || true", true, "B")
    }

    @Test
    fun testAnd() {
        doTestVariables("false && false", false, "B")
        doTestVariables("false && true", false, "B")
        doTestVariables("true && false", false, "B")
        doTestVariables("true && true", true, "B")
    }

    @Test
    fun testEquals() {
        doTestVariables("10 == 1", false, "B")
        doTestVariables("7 * 4 * 3 == 2 * 3 * 14", true, "B")
        doTestVariables("9 != -2", true, "B")
        doTestVariables("true && false != true", true, "B")
    }

    @Test
    fun testComparing() {
        doTestVariables("10 > 1", true, "B")
        doTestVariables("8 > 123", false, "B")
        doTestVariables("2 + 2 > 2", true, "B")
        doTestVariables("8 * 10 > 107", false, "B")

        doTestVariables("1 < 14", true, "B")
        doTestVariables("19 < -2", false, "B")
        doTestVariables("2 + 9 < 79 * 210 / 7", true, "B")
        doTestVariables("11 * 99 < (-2)", false, "B")

        doTestVariables("13 <= 14", true, "B")
        doTestVariables("14 <= 14", true, "B")
        doTestVariables("15 <= 14", false, "B")

        doTestVariables("80 >= 79", true, "B")
        doTestVariables("80 >= 80", true, "B")
        doTestVariables("80 >= 81", false, "B")
    }

    @Test
    fun testRandomExpression() {
        doTestVariables("(1 + 2) % 3 * 4 - 5 * -6 + 7/221 > 100 && false != true", false, "B")
    }

    private fun doTestVariables(input: String, expectedResult: Any, expectedType: String?) {
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
        doTestTypes("4.5", "4.500000", "D")
        doTestTypes("4 + 0.5", "4.500000", "D")
        doTestTypes("100 * 4.5", "450.000000", "D")
    }

    @Test
    fun testChar() {
        doTestTypes("'a'", "a", "C")
        doTestTypes("'a' + 5", "f", "C")
    }

    @Test
    fun testLong() {
        doTestTypes("56789071123456789", "56789071123456789", "L")
        doTestTypes("56789071123456789 + 1", "56789071123456790", "L")
        doTestTypes("56789071123456789 / 1000000", "56789071123", "L")
    }

    @Early
    private fun doTestTypes(input: String, expectedResult: Any, expectedType: String?) {
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

    @Test
    fun testSmth() {
        val code = """
                def printfdsf: Int;
                printfdsf = 10;
                print printfdsf;
            """

        val expectedType = listOf("printfdsf" to "I")
        val expectedResult = "10\n"

        doTestVariables(code, expectedType, expectedResult)
    }

    private fun doTestVariables(
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

class TestIO : TestParser() {
    private fun doTest() {
        super.doTest(Paths.get("./src/test/kotlin/org/expr/io/$name"))
    }

    @Test
    fun testIO1() = doTest()

    @Test
    fun testIoWithTi() = doTest()

    @Test
    fun testIoWithTi_2() = doTest()

    @Test
    fun testStringIO() = doTest()

    @Test
    fun testStringIO2() = doTest()

    @Test
    fun testPrintArray() = doTest()
}

class TestString : TestParser() {
    private fun doTest() {
        super.doTest(Paths.get("./src/test/kotlin/org/expr/string/$name"))
    }

    @Test
    fun testGetAndAssign() = doTest()

    @Test
    fun testAssign() = doTest()

    @Test
    fun testConcat() = doTest()

    @Test
    fun testConstantString() = doTest()
}

class TestArray : TestParser() {
    private fun doTest() {
        super.doTest(Paths.get("./src/test/kotlin/org/expr/array/$name"))
    }

    @Test
    fun testDeclaration() = doTest()

    @Test
    fun testFewArrays() = doTest()

    @Test
    fun testEqEq() = doTest()

    @Test
    fun testArraysTypeParameter() = doTest()

    @Test
    fun testArrayAssign() = doTest()

    @Test
    fun testConcat() = doTest()
}

@Early
class TestConditions : TestParser() {
    private fun doTest() {
        super.doTest(Paths.get("./src/test/kotlin/org/expr/conditions/$name"))
    }

    @Test
    fun testIf() = doTest()

    @Test
    fun testWhile() = doTest()

    @Test
    fun testWhileWithIf() = doTest()

    @Test
    fun testContinue() = doTest()

    @Test
    fun testBreak() = doTest()

    @Test
    fun testScopes() = doTest()
}

@Early
class TestFunctions : TestParser() {
    private fun doTest() {
        super.doTest(Paths.get("./src/test/kotlin/org/expr/functions/$name"))
    }

    @Test //NOTE: Codegen Test
    fun testDeclaration() = doTest()

    @Test
    fun testCallSide() = doTest()

    @Test
    fun testRecursion() = doTest()

    @Test
    fun testWrongFunction() = doTest()

    @Test
    fun testEquals() = doTest()
}
