# Ручное построение нисходящих синтаксических анализаторов

## Задание
#### Описание заголовка функции в Паскале
Заголовок функции в Паскале. Заголовок начинается ключевым
словом <code>function</code> или <code>procedure</code>, далее идет имя функции, скобка,
несколько описаний аргументов. Описание аргументов содержит имена переменных через 
запятую, затем двоеточие, затем имя типа. Достаточно рассматривать только примитивные 
типы (массивы, записи и т. п. не требуется). После этого в случае функции следует
двоеточие и имя типа.

Используйте один терминал для всех имен переменных и имен типов. Используйте один терминал
для ключевых слов <code>function</code> и т. п. (не несколько <code>f</code>,
<code>u</code>, <code>n</code>, ...).

Пример: <code>function fib(n: integer): integer;</code>


## Грамматика
Нетерминалы:
 * Q - файл
 * F - <code>function</code>
 * P - <code>procedure</code>
 * S - сигнатура вызова (имя функции и список аргументов)
 * N - имя
 * A - список аргументов
 * D - ровно один агрумент
 * T - имя типа
 * Z - продолжение списка аргументов

Терминалы: 
 * 0 - <code>function</code>
 * 1 - <code>procedure</code>
 * \* - string (e.g. declaration name)
 * _ - символ пустой строки
 * А так же: <code>( ) : , </code>

Спиок привил:
 * Q -> F
 * Q -> P
 * F -> 0S:T
 * P -> 1S
 * S -> N(A)
 * N -> *
 * T -> *
 * A -> _
 * A -> DZ
 * D -> N:T
 * Z -> _
 * Z -> ,DZ

## FIRST & FOLLOW

<code>FIRST = { Q=[0, 1], F=[0], P=[1], S=[*], N=[*], A=[_, *], T=[*], D=[*], Z=[_, ,] }</code> 

<code>FOLLOW = { Q=[$], F=[$], P=[$], S=[:, $], N=[(, :], A=[)], T=[$, ,, )], D=[,, )], Z=[)] }</code>

## Решение
[Лексический анализатор](https://github.com/tihonovcore/GParser/pascalParser/src/main/kotlin/org/tihonovcore/pascal/Lexer.kt) <br>
[Синтаксический анализатор](https://github.com/tihonovcore/GParser/pascalParser/src/main/kotlin/org/tihonovcore/pascal/PascalParser.kt) <br>
[Визуализатор дерева разбора](https://github.com/tihonovcore/GParser/pascalParser/src/main/kotlin/org/tihonovcore/pascal/RenderVisitor.kt) <br>
[Тесты](https://github.com/tihonovcore/GParser/pascalParser/src/test/kotlin/TestParser.kt) <br>
