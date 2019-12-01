import junit.framework.TestCase
import org.junit.Test
import org.tihonovcore.utils.Early

@Early
abstract class TestGenerator : TestCase()

@Early
class TestFunctionDeclaration : TestGenerator() {
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
    fun testWhiteSpace() {
        doTest("  function   fun   (   a :  real  ) :  real   ;")
    }

    private fun doTest(input: String) {
        var render = ""
        try {
            val tokens = getfuncDeclrTokens(input)
            val parseResult = funcDeclrParser(tokens).parse()
            Render.visit(parseResult)
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

    //TODO: remove. using for generated parser
    object Render {
        fun visit(visit: Tree) {
            if (visit is Terminal) print("${visit.data} ")
            else visit.children.forEach { visit(it) }
        }
    }
}

@Early
class TestCalculator : TestGenerator() {
    @Test
    fun testAdd() {
        doTest("1 + 2", 3)
        doTest("4 + 8", 12)
        doTest("449 + 783", 1232)
        doTest("2 + 3 + 49 + 5", 59)
    }

    @Test
    fun testSubtract() {
        doTest("1 - 1", 0)
        doTest("1 - 100", -99)
        doTest("889 - 435", 454)
        doTest("10 - 2 - 3", 5)
    }

    @Test
    fun testMultiply() {
        doTest("10 * 1", 10)
        doTest("106 * 23", 2438)
        doTest("2345 * 34567", 81059615)
        doTest("7 * 12 * 3", 252)
    }

    @Test
    fun testDivide() {
        doTest("12 / 4", 3)
        doTest("99 / 14", 7)
        doTest("3452 / 18", 191)
        doTest("105 / 5 / 7", 3)
    }

    @Test
    fun testParenthesis() {
        doTest("(1 + 1) * (4 / 2) + 3 / 1", 7)
        doTest("(1 + 1) * 4 / (2 + 3) / 1", 1)
        doTest("(1 + 1) * (4 / 2 + 3) / 1", 10)
        doTest("1 + 1 * 4 / 2 + 3 / 1", 6)
    }

    @Test
    fun testSimpleExpression() {
        doTest("5 * 2 + 3", 13)
        doTest("1*2*3*4*(5 +5*2)", 360)
        doTest("12*4+778", 826)
        doTest("2 + 2 * 2", 6)
        doTest("1 + 2 * 3 / 4 - 9", -7)
    }

    @Test
    fun testLongExpression() {
        doTest("41*0+90-57/44-51*78+67/89-37-12/76*44+94/61-90+78*36-72+75+28*98+29/8+73+77*89/49*63", 10373)
        doTest("21-35*57+63-55/17/10/56-93/31-32*41-39*22/27*23*79+43-62", -59572)
        doTest("41/75+65-8+76-36-31-68", -2)
        doTest("56*35/36/28+17*16/34/70+23+39-13+88+1*57+77*34+89-1*67+33+36*76/54/31*30+27+84+27+32*84+69*92/68/28*28", 5808)
        doTest("11*78-88/50/30+73-43+94/51*12+28/71/39-15*56/34/9-37/7*62-90*58+53+81", -4498)
    }

    @Early
    private fun doTest(input: String, result: Int) {
        val tokens = getcalculatorTokens(input)
        val tree = calculatorParser(tokens).parseExpr()

        require(tree.value == result) {
            "$input expected $result, but was ${tree.value}"
        }

        println("=$input = ${tree.value}")
    }
}

@Early
class TestInheritedAttributes : TestGenerator() {
    @Test
    fun testSmallArray() = doTest("array(3) 1 2 3")

    @Test
    fun testBigArray() = doTest("array(12) 1 24 4 5 22 1 5 8 8 0 4 99")

    @Test
    fun testWrongSize() = doTestWithException("array(4) 15 4 3")

    @Test
    fun testParserError() = doTestWithException("array[4] 1 2 4 3")

    private fun doTest(input: String) {
        val tokens = getinhAttrTokens(input)
        inhAttrParser(tokens).parse()
    }

    private fun doTestWithException(input: String) {
        try {
            doTest(input)
        } catch (e: Throwable) {
            println(e.message)
            return
        }

        assert(false) { "Expected error" }
    }
}

@Early
class TestCodeBlocks : TestGenerator() {
    @Test
    fun test0() {
        /*
        TODO: out wrong: there is "with args..." for call with no args
        first code block adds in wrong place
         */
        doTest("plus(plus());")
    }

    @Test
    fun test1() {
        doTest("print plus(read, read, plus(read));")
    }

    @Test
    fun test2() {
        /*
        TODO: out wrong: there is "with args..." for call with no args
        first code block adds in wrong place
         */
        doTest("println eval(read, fact(id()), add(read, read));")
    }

    @Test
    fun test3() {
        doTest("run(fo(read), f(g(h(read))), negate(read));")
    }

    private fun doTest(input: String) {
        val tokens = getfunctionLanguageTokens(input)
        val tree = functionLanguageParser(tokens).parse()
    }
}
