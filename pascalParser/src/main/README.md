# Ручное построение нисходящих синтаксических анализаторов

## Задание
#### Описание заголовка функции в Паскале
Заголовок функции в Паскале. Заголовок начинается ключевым
словом `function` или `procedure`, далее идет имя функции, скобка,
несколько описаний аргументов. Описание аргументов содержит имена переменных через 
запятую, затем двоеточие, затем имя типа. Достаточно рассматривать только примитивные 
типы (массивы, записи и т. п. не требуется). После этого в случае функции следует
двоеточие и имя типа.

Используйте один терминал для всех имен переменных и имен типов. Используйте один терминал
для ключевых слов <code>function</code> и т. п. (не несколько `f`,
`u`, `n`, ...).

Пример: <code>function fib(n: integer): integer;</code>


## Грамматика
Нетерминалы:
 * file - файл
 * function - функция
 * procedure - процедура
 * signature - сигнатура вызова (имя функции и список аргументов)
 * name - имя
 * arguments - список аргументов
 * declaration - ровно один агрумент
 * type - имя типа
 * suffix - продолжение списка аргументов

Терминалы: 
 * FUNCTION - `function`
 * PROCEDURE - `procedure`
 * `*` - string (e.g. declaration name)
 * `_` - символ пустой строки
 * А так же: `( ) : , ;`

Спиок привил:
 * file -> function `;`
 * file -> procedure `;`
 * function -> FUNCTION signature `:` type
 * procedure -> PROCEDURE signature
 * signature -> name `(` arguments `)`
 * name -> `*`
 * type -> `*`
 * arguments -> `_`
 * arguments -> declaration suffix
 * declaration -> name `:` type
 * suffix -> `_`
 * suffix -> `,` declaration suffix

## FIRST & FOLLOW

    FIRST = { 
        file=[FUNCTION, PROCEDURE], 
        function=[FUNCTION], 
        procedure=[PROCEDURE], 
        signature=[*], 
        name=[*], 
        arguments=[_, *], 
        type=[*],
        declaration=[*], 
        suffix=[_, `,`] 
    } 

    FOLLOW = { 
        file=[$], 
        function=[;], 
        procedure=[;],
        signature=[:, ;], 
        name=[(, :], 
        arguments=[)], 
        type=[;, `,`, )], 
        declaration=[`,`, )], 
        suffix=[)] 
    }

## Решение
[Лексический анализатор](https://github.com/tihonovcore/GParsing/blob/master/pascalParser/src/main/kotlin/org/tihonovcore/pascal/Lexer.kt) <br>
[Синтаксический анализатор](https://github.com/tihonovcore/GParsing/blob/master/pascalParser/src/main/kotlin/org/tihonovcore/pascal/PascalParser.kt) <br>
[Визуализатор дерева разбора](https://github.com/tihonovcore/GParsing/blob/master/pascalParser/src/main/kotlin/org/tihonovcore/pascal/RenderVisitor.kt) <br>
[Тесты](https://github.com/tihonovcore/GParsing/blob/master/pascalParser/src/test/kotlin/TestParser.kt) <br>
