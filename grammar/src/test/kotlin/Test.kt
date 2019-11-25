import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import org.tihonovcore.grammar.*
import java.io.File

class TestGrammar : TestCase() {
    @Test
    fun testUseless() {
        doTest(
            "grammar",
            "S A B",
            "a b _",
            setOf("S->A B", "A->a B", "B->b", "B->_"),
            listOf("S a", "A a", "B b _").buildMap(),
            listOf("S $", "A b $", "B b $").buildMap()
        )
    }

    @Test
    fun testHardExpr() {
        doTest(
            "hardExpr",
            "E F T W Q",
            "n ( ) + * _",
            setOf("Q->+ T Q", "Q->_", "W->* F W", "W->_", "T->F W", "F->n", "F->( E )", "E->T Q"),
            listOf("E n (", "F n (", "T n (", "W * _", "Q + _").buildMap(),
            listOf("E $ )", "F + * $ )", "T + $ )", "W + $ )", "Q $ )").buildMap()
        )
    }

    @Test
    fun testNaiveExpr() {
        doTest(
            "naiveExpr",
            "E F T e t",
            "n ( ) + *",
            setOf("e->+ T e", "e->_", "t->* F t", "t->_", "T->F t", "F->n", "F->( E )", "E->T e"),
            listOf("E n (", "F n (", "T n (", "e + _", "t * _").buildMap(),
            listOf("E $ )", "F + * $ )", "T + $ )", "e $ )", "t + $ )").buildMap()
        )
    }

    @Test
    fun testCorrectBracketSeq() {
        doTest(
            "psp",
            "S T",
            "( ) _",
            setOf("S->T S", "T->( S )", "S->_"),
            listOf("S _ (", "T (").buildMap(),
            listOf("S $ )", "T ( $ )").buildMap()
        )
    }

    private fun getGrammar(testName: String): Grammar {
        val path = "src/test/resources/$testName"
        val lines = File(path).readLines()
        return readGrammar(lines)
    }

    private fun doTest(
        testName: String,
        expectedNonterminals: String,
        expectedTerminals: String,
        expectedRules: Set<String>,
        expectedFirst: Map<String, Set<String>>,
        expectedFollow: Map<String, Set<String>>
    ) {
        val grammar = getGrammar(testName).removeUselessNonterminals().removeLeftRecursion()

        val first = buildFirst(grammar)
        val follow = buildFollow(grammar)

        Assert.assertEquals("Wrong FIRST", expectedFirst, first)
        Assert.assertEquals("Wrong FOLLOW", expectedFollow, follow)

        Assert.assertEquals("Wrong nonterminals", expectedNonterminals.split(" ").toSet(), grammar.nonterminals.toSet())
        Assert.assertEquals("Wrong terminals", expectedTerminals.split(" ").toSet(), grammar.terminals.toSet())
        Assert.assertEquals(
            "Wrong rules",
            expectedRules.map { it.split("->") }.map { Rule(it[0], it[1].split(" ")) }.toSet(),
            grammar.rules.toSet()
        )

        val checkLL1Result = detailCheckLL1(grammar)
        Assert.assertTrue("Grammar is not LL(1): \n ${checkLL1Result.description}", checkLL1Result.isLL1)
    }

    private fun List<String>.buildMap(): Map<String, Set<String>> {
        return this.associate { s ->
            val splited = s.split(" ")
            splited[0] to splited.drop(1).toSet()
        }
    }
}
