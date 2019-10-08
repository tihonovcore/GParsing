import org.tihonovcore.utils.Early

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import org.tihonovcore.grammar.*
import java.io.File

@Early
class TestGrammar : TestCase() {
    @Test
    fun testUseless() {
        doTest(
            "grammar",
            "SAB",
            "ab_",
            setOf("S->AB", "A->aB", "B->b", "B->_"),
            listOf("Sa", "Aa", "Bb_").buildMap(),
            listOf("S$", "Ab$", "Bb$").buildMap()
        )
    }

    @Test
    fun testHardExpr() {
        doTest(
            "hardExpr",
            "EFTWQ",
            "n()+*_",
            setOf("Q->+TQ", "Q->_", "W->*FW", "W->_", "T->FW", "F->n", "F->(E)", "E->TQ"),
            listOf("En(", "Fn(", "Tn(", "W*_", "Q+_").buildMap(),
            listOf("E$)", "F+*$)", "T+$)", "W+$)", "Q$)").buildMap()
        )
    }

    @Test
    fun testNaiveExpr() {
        doTest(
            "naiveExpr",
            "EFTet",
            "n()+*",
            setOf("e->+Te", "e->_", "t->*Ft", "t->_", "T->Ft", "F->n", "F->(E)", "E->Te"),
            listOf("En(", "Fn(", "Tn(", "e+_", "t*_").buildMap(),
            listOf("E$)", "F+*$)", "T+$)", "e$)", "t+$)").buildMap()
        )
    }

    @Test
    fun testCorrectBracketSeq() {
        doTest(
            "psp",
            "ST",
            "()_",
            setOf("S->TS", "T->(S)", "S->_"),
            listOf("S_(", "T(").buildMap(),
            listOf("S$)", "T($)").buildMap()
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
        expectedFirst: Map<Char, Set<Char>>,
        expectedFollow: Map<Char, Set<Char>>
    ) {
        val grammar = getGrammar(testName).removeUselessNonterminals().removeLeftRecursion()

        val first = buildFirst(grammar)
        val follow = buildFollow(grammar)

        Assert.assertEquals("Wrong FIRST", expectedFirst, first)
        Assert.assertEquals("Wrong FOLLOW", expectedFollow, follow)

        Assert.assertEquals("Wrong nonterminals", expectedNonterminals.toSet(), grammar.nonterminals.toSet())
        Assert.assertEquals("Wrong terminals", expectedTerminals.toSet(), grammar.terminals.toSet())
        Assert.assertEquals(
            "Wrong rules",
            expectedRules.map { it.split("->") }.map { Rule(it[0].single(), it[1]) }.toSet(),
            grammar.rules.toSet()
        )

        val checkLL1Result = detailCheckLL1(grammar)
        Assert.assertTrue("Grammar is not LL(1): \n ${checkLL1Result.description}", checkLL1Result.isLL1)
    }

    private fun List<String>.buildMap(): Map<Char, Set<Char>> {
        return this.associate { s -> s[0] to s.drop(1).toSet() }
    }
}
