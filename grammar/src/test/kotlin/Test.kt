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
            setOf("S->AB", "A->aB", "B->b", "B->_")
        )
    }

    @Test
    fun testHardExpr() {
        doTest(
            "hardExpr",
            "EFTWQ",
            "n()+*_",
            setOf("Q->+TQ", "Q->_", "W->*FW", "W->_", "T->FW", "F->n", "F->(E)", "E->TQ")
        )

    }

    @Test
    fun testNaiveExpr() {
        doTest(
            "naiveExpr",
            "EFTet",
            "n()+*",
            setOf("e->+Te", "e->_", "t->*Ft", "t->_", "T->Ft", "F->n", "F->(E)", "E->Te")
        )
    }

    @Test
    fun testCorrectBracketSeq() {
//        doTest( //TODO: whats wrong??
//            "psp",
//            "Ss",
//            "()_",
//            setOf("S->(S)s", "s->Ss", "S->s", "s->_") //TODO: check
//        )

        doTest(
            "psp",
            "ST",
            "()_",
            setOf("S->TS", "T->(S)", "S->_")
        )
    }

    private fun getGrammar(testName: String): Grammar {
        //TODO: make absolute path
        val path = "/home/tihonovcore/work/GParser/src/test/resources/" + testName
        val lines = File(path).readLines()
        return readGrammar(lines)
    }

    private fun doTest(
        testName: String,
        expectedNonterminals: String,
        expectedTerminals: String,
        expectedRules: Set<String>
    ) {
        val grammar = getGrammar(testName).removeUselessNonterminals().removeLeftRecursion()

        FIRST.clear()
        FOLLOW.clear()
        buildFirst(grammar)
        buildFollow(grammar)

        println(FIRST)
        println(FOLLOW)

//        Assert.assertEquals() test first
//        Assert.assertEquals() test follow

        Assert.assertEquals("Wrong nonterminals", expectedNonterminals.toSet(), grammar.nonterminals.toSet())
        Assert.assertEquals("Wrong terminals", expectedTerminals.toSet(), grammar.terminals.toSet())
        Assert.assertEquals(
            "Wrong rules",
            expectedRules.map { it.split("->") }.map { Rule(it[0].single(), it[1]) }.toSet(),
            grammar.rules.toSet()
        )
        Assert.assertTrue("Grammar is not LL(1)", checkLL1(grammar))
    }
}
