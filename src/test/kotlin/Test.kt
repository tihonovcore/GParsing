import org.junit.Assert
import org.junit.Test
import java.io.File

class TestGrammar {
    @Test
    fun testUseless() {
        val path = "/home/tihonovcore/work/GParser/src/test/resources/grammar"
        val lines = File(path).readLines()
        val grammar = readGrammar(lines)

        val cleanGrammar = removeUselessNonterminals(grammar)

        Assert.assertEquals(listOf('S', 'A', 'B'), cleanGrammar.nonterminals)
        Assert.assertEquals(listOf('a', 'b', '_'), cleanGrammar.terminals)
        Assert.assertEquals(
            listOf(
                Rule('S', "AB"),
                Rule('A', "aB"),
                Rule('B', "b"),
                Rule('B', "_")
            ),
            cleanGrammar.rules
        )
    }
}
