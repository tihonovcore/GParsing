import gen.GrammarBaseVisitor
import gen.GrammarParser
import org.antlr.v4.runtime.tree.TerminalNode
import org.tihonovcore.grammar.Rule
import org.tihonovcore.utils.Early

/**
 * Visitor for getting information about [nonterminals], [terminals],
 * [rules], [lexerRules], nonterminals for code - [codeBlocks],
 * attributes - [synthesized] and [inherited]
 */
@Early
class GrammarGenerator : GrammarBaseVisitor<String>() {
    /**
     * [Set] of nonterminals' names
     * [GrammarGenerator] creates additional nonterminals for rules with `*`,
     * `?` or `+`, which names starts with `generated_`, and nonterminals for
     * blocks of code: `code_`
     */
    val nonterminals = mutableSetOf<String>()

    /**
     * Set of terminals' names
     */
    val terminals = mutableSetOf<String>()

    /**
     * List of parser rules
     */
    val rules = mutableListOf<Rule>()

    /**
     * List of lexer rules <regex, tokenName>
     */
    val lexerRules = mutableListOf<Pair<String, String>>()

    /**
     * Maps code nonterminal (their names start with `code_`) to block of code
     */
    val codeBlocks = mutableMapOf<String, String>()

    /**
     * Maps nonterminal name to list of his synthesized attributes
     */
    val synthesized: MutableMap<String, MutableList<String>> = mutableMapOf()

    /**
     * Maps nonterminal name to list of his inherited attributes
     * Format: <name : type>
     */
    val inherited: MutableMap<String, MutableList<Pair<String, String>>> = mutableMapOf()


    /**
     * Left side of current rule
     */
    private var currentLeft: String = ""

    /**
     * Right side of current rule
     */
    private var currentRight = mutableListOf<String>()

    /**
     * List of arguments that passes to nonterminals with inherited attributes
     */
    private var calls: MutableList<String> = mutableListOf()

    private var underStarPlusQuestion = false

    @Early
    override fun visitRule_decl(ctx: GrammarParser.Rule_declContext?): String? {
        require(ctx != null)

        val ruleId = ctx.RULE_ID().text
        nonterminals += ruleId
        currentLeft = ruleId

        visit(ctx.attributes())
        currentRight.add(visit(ctx.codeblock()))

        val right = ctx.description()
        visit(right)

        rules += Rule(currentLeft, currentRight, calls)

        calls = mutableListOf()
        currentRight = mutableListOf()
        return null
    }

    override fun visitInherited(ctx: GrammarParser.InheritedContext?): String? {
        require(ctx != null)

        val declarations = mutableListOf<Pair<String, String>>()
        for (i in 1 until ctx.childCount - 1 step 2) {
            val (name, type) = ctx.children[i].text.split(":")
            declarations += name to type
        }
        if (declarations.isNotEmpty()) inherited[currentLeft] = declarations

        return null
    }

    override fun visitSynthesized(ctx: GrammarParser.SynthesizedContext?): String? {
        require(ctx != null)

        val declarations = mutableListOf<String>()
        for (i in 2 until ctx.childCount - 1 step 2) {
            declarations += ctx.children[i].text
        }
        synthesized[currentLeft] = declarations

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
    override fun visitDescription(ctx: GrammarParser.DescriptionContext?): String? {
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
            return super.visitDescription(ctx)
        }
    }

    @Early
    override fun visitAnd(ctx: GrammarParser.AndContext?): String? {
        require(ctx != null)

        if (!underStarPlusQuestion) {
            ctx.children.forEach {
                currentRight.add(visit(it))
            }
            return null
        }

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
                val prevFlag = underStarPlusQuestion
                underStarPlusQuestion = true
                val arg = withNewAlpha { visit(ctx.children[1]) } //TODO: why rule doesnt add
                underStarPlusQuestion = prevFlag
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
                val prevFlag = underStarPlusQuestion
                underStarPlusQuestion = true
                val arg = withNewAlpha { visit(ctx.children[0]) } //TODO: why rule doesnt add
                underStarPlusQuestion = prevFlag
                when (ctx.children[1].text) {
                    "+" -> wrapWithPlus(arg)
                    "*" -> wrapWithStar(arg)
                    "?" -> wrapWithQuestion(arg)
                    else -> throw IllegalStateException()
                }
            }
        }
    }

    override fun visitRuleIdWithPass(ctx: GrammarParser.RuleIdWithPassContext?): String {
        require(ctx != null)

        if (ctx.PASS() != null) {
            calls.add(ctx.PASS().text.drop(2).dropLast(2))
        }

        return visit(ctx.RULE_ID())
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
