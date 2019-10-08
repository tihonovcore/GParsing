# MT course homeworks

## Utilities for grammars

<code>Grammar</code> class for Grammar<br>
NOTE: Terminals and nonterminals are Chars.<br>
NOTE: You should not to use letters in lower case for terminals and nonterminals.<br>
NOTE: '_' is symbol of empty string, you can use it in terminals<br>
NOTE: First terminal perceive as general<br>
<code>Rule</code> class for rule in grammar<br>
<code>Grammar.removeUselessNonterminals()</code> build new <code>Grammar</code> from <code>this</code> without non-generative 
and unreachable nonterminals, remove useless rules <br>
@Early  <code>Grammar.removeLeftRecursion()</code> build new <code>Grammar</code> from <code>this</code> remove direct left recursion<br>
<code>buildFirst(grammar: Grammar)</code> build First and put it in map <code>FIRST</code><br>
<code>buildFollow(grammar: Grammar)</code> build Follow and put it in map <code>FOLLOW</code><br>
<code>checkLL1(grammar: Grammar)</code> return <code>true</code> if <code>grammar</code> is LL(1)-grammar<br>
<code>detailCheckLL1(grammar: Grammar)</code> return <code>CheckLL1Result</code>, which contains two fields: 
<code>isLL1: Boolean</code> and <code>description: String</code> with description of error<br>


## Parser for function defenition in Pascal
@Early [Lexer](https://github.com/tihonovcore/GParsing/blob/master/pascalParser/src/main/kotlin/Lexer.kt)<br>
