import gen.GrammarBaseVisitor
import gen.GrammarParser
import org.antlr.v4.runtime.tree.TerminalNode
import org.tihonovcore.grammar.Rule
import org.tihonovcore.utils.Early

@Early
class GrammarGenerator : GrammarBaseVisitor<String>() {
    val nonterminals = mutableSetOf<String>()
    val terminals = mutableSetOf<String>()

    val rules = mutableListOf<Rule>()
    val lexerRules = mutableListOf<Pair<String, String>>()
    val codeBlocks = mutableMapOf<String, String>()

    private var currentLeft: String = ""
    private var currentRight = mutableListOf<String>()

    @Early
    override fun visitRule_decl(ctx: GrammarParser.Rule_declContext?): String? {
        require(ctx != null)

        val ruleId = ctx.RULE_ID().text
        val right = ctx.rule1()

        currentRight.add(visit(ctx.codeblock()))

        nonterminals += ruleId
        currentLeft = ruleId

        visit(right)

        rules += Rule(currentLeft, currentRight)

        currentRight = mutableListOf()
        return null
    }

    @Early
    override fun visitToken_decl(ctx: GrammarParser.Token_declContext?): String? {
        require(ctx != null)

        val tokenId = ctx.TOKEN_ID().text
        terminals += tokenId

        val regex = ctx.REGEX().text.drop(1).dropLast(1)
        lexerRules += "\"$regex\"" to tokenId

        return null
    }

    @Early
    override fun visitRule1(ctx: GrammarParser.Rule1Context?): String? {
        require(ctx != null)

        if (ctx.childCount > 1) { //exists OR
            val left = findFreeNonterminalName()
            nonterminals += left

            currentRight.add(left)

            for (i in ctx.children.indices step 2) { //skip `|`
                withNewAlpha {
                    visit(ctx.children[i])
                    rules += Rule(left, currentRight)
                }
            }

            return left
        } else {
            return super.visitRule1(ctx)
        }
    }

    @Early
    override fun visitAnd(ctx: GrammarParser.AndContext?): String? {
        require(ctx != null)

//        TODO: support flag `underStarPlusQuestion`. then we'll have less generated nonterminals
//        if (/*we are not under `*+?`*/) {
//            ctx.children.forEach {
//                currentRight.add(visit(it))
//            }
//            return null
//        }

        val left = findFreeNonterminalName()
        nonterminals += left

        currentRight.add(left)
        withNewAlpha {
            ctx.children.forEach {
                currentRight.add(visit(it))
            }
            rules += Rule(left, currentRight)
        }

        return left
    }

    @Early
    override fun visitFactor(ctx: GrammarParser.FactorContext?): String? {
        require(ctx != null)

        return if (ctx.childCount >= 3) { // (expr)+*?
            if (ctx.childCount == 3) {
                val left = findFreeNonterminalName()
                nonterminals += left

                withNewAlpha {
                    visit(ctx.children[1])
                    rules += Rule(left, currentRight)
                }

                left
            } else {
                val arg = withNewAlpha { visit(ctx.children[1]) }
                when (ctx.children[3].text) {
                    "+" -> wrapWithPlus(arg)
                    "*" -> wrapWithStar(arg)
                    "?" -> wrapWithQuestion(arg)
                    else -> throw IllegalStateException()
                }
            }
        } else { //id*+?
            if (ctx.childCount == 1) {
                visit(ctx.children[0])
            } else {
                val arg = withNewAlpha { visit(ctx.children[0]) }
                when (ctx.children[1].text) {
                    "+" -> wrapWithPlus(arg)
                    "*" -> wrapWithStar(arg)
                    "?" -> wrapWithQuestion(arg)
                    else -> throw IllegalStateException()
                }
            }
        }
    }

    @Early
    override fun visitCodeblock(ctx: GrammarParser.CodeblockContext?): String {
        require(ctx != null)

        val name = "code_${codeBlocks.size}"
        nonterminals.add(name)
        rules += Rule(name, "_")

        val text = ctx.text.drop(2).dropLast(2)
        codeBlocks[name] = text

        return name
    }

    @Early
    private fun wrapWithPlus(arg: String): String {
        val left = findFreeNonterminalName()
        nonterminals += left
        val suffix = findFreeNonterminalName()
        nonterminals += suffix

        rules += Rule(left, listOf(arg, suffix))
        rules += Rule(suffix, listOf("_"))
        rules += Rule(suffix, listOf(arg, suffix))

        return left
    }

    @Early
    private fun wrapWithStar(arg: String): String {
        val left = findFreeNonterminalName()
        nonterminals += left

        rules += Rule(left, listOf("_"))
        rules += Rule(left, listOf(arg, left))

        return left
    }

    @Early
    private fun wrapWithQuestion(arg: String): String {
        val left = findFreeNonterminalName()
        nonterminals += left

        rules += Rule(left, listOf(arg))
        rules += Rule(left, listOf("_"))

        return left
    }

    @Early
    override fun visitTerminal(node: TerminalNode?): String {
        return node!!.text
    }

    @Early
    private fun findFreeNonterminalName(): String {
        var index = 0
        var suggestion = "generated_$index"
        while (suggestion in nonterminals) {
            index++
            suggestion = "generated_$index"
        }
        return suggestion
    }

    @Early
    private fun <T> withNewAlpha(body: () -> T): T {
        val previousAlpha = currentRight
        currentRight = mutableListOf()

        return body().also { currentRight = previousAlpha }
    }
}
