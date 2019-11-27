## Module Grammar
###### `Grammar` class which interprets grammar<br>
Contains terminals, nonterminals, rules, build sets `FIRST` and `FOLLOW`<br>

NOTE: You should not to use letters in lower case for terminals and nonterminals.<br>
NOTE: '_' is symbol of empty string, you can use it in terminals<br>
NOTE: First terminal perceive as general<br>


###### `Rule` class for rule in grammar<br>
`Grammar.removeUselessNonterminals()` build new `Grammar` from `this`
without non-generative and unreachable nonterminals, remove useless rules <br>

`Grammar.removeLeftRecursion()` build new `Grammar` from `this` remove direct left recursion<br>

`@Early Grammar.unsafeRemoveRightBranching()` build new `Grammar` from `this` remove right branching<br>

`buildFirst(grammar: Grammar)` build set First and put it in map `FIRST`<br>
`buildFollow(grammar: Grammar)` build set Follow and put it in map `FOLLOW`<br>

`detailCheckLL1(grammar: Grammar)` return `CheckLL1Result`, which contains two fields: 
`isLL1: Boolean` and `description: String` with description of grammar problem<br>
