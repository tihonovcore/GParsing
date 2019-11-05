# MPL

`ok` - done <br>
`ss` - almost done <br>
`ww` - work in progress <br>
`ns` - not started

**\[SOFT DEADLINE/HARD DEADLINE\]**

## 01 Expressions \[16/20\]
* `ok` Expressions (Int, Bool)
* `removed` Evaluation
* `ok` Type inference

## 02 Variables & IO \[20/23\]
* `ok` Variable declaration
* `ok` Assignments
* `ok` IO functions
* `ok` Codegen
* `ok` Compiler MPL

## 03 Types \[27/30\] @Early
* `ok` Primitives (Long, Double, Char)
* `ss` String
* `ss` Array

## 04 Conditions & loops & functions \[3/6\] @Early
* `ok` if/else
* `ok` while
* `ss` Functions
* `ok` cast

## Other \[3/10\]
* rm semicolon, use WS
* SingleLine-comments

## Probably will supported never
* String interpolation - сложно, строка становиться нетерминалом, а пробел символом
* Multiline-comments - сложно и излишне
* Многомерные массивы - *потенциальные* проблемы с ссылочностью (пока забанены (в тч массивы строк))
* Classes - сложны в реализации, могут породить проблемы с ссылочностью