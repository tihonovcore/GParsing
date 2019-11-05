package org.expr

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.expr.gen.ExprLexer
import org.expr.gen.ExprParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.io.BufferedWriter
import java.io.OutputStreamWriter

import org.tihonovcore.utils.Early
import java.lang.Exception
import java.nio.file.Paths

@Early
class MPLCompiler {
    var lastTypeMap: Map<String, String> = mutableMapOf() //TODO: make clearly

    fun evaluate(source: String, input: List<Any> = emptyList()): String {
        val (c, typeMap) = generateC(source)
        println(c)
        lastTypeMap = typeMap

        val path = "c_out.out" //TODO: create tmp file
        compileC(c, path)
        return runC(path, input).also { Files.delete(Paths.get(path)) }
    }

    fun generateC(source: String): Pair<String, Map<String, String>> {
        val lexer = ExprLexer(CharStreams.fromString(source))
        val tokens = CommonTokenStream(lexer)
        val parser = ExprParser(tokens).also { it.mySource = source }

        val file = parser.file()
        if (parser.numberOfSyntaxErrors != 0) {
            throw Exception()
        }

        val codeGenerator = CodeGenerator(parser, file)
        return Pair(codeGenerator.gen(), parser.current.first())
    }

    fun compileC(source: String, outPath: String) {
        val temp = Files.createTempFile("source", ".c")
        Files.write(temp, source.toByteArray())

        val compileCommand = "gcc -o $outPath $temp"
        val process = Runtime.getRuntime().exec(compileCommand)
        process.waitFor()
    }

    fun runC(outPath: String, input: List<Any>): String {
        val command = "./$outPath"
        val process = Runtime.getRuntime().exec(command)

        BufferedWriter(OutputStreamWriter(process.outputStream)).use {
            for (a in input) it.write("$a ")
        }
        process.waitFor()

        val reader = BufferedReader(InputStreamReader(process.inputStream))

        var evaluationResult = ""
        while (true) {
            val line = reader.readLine() ?: break
            evaluationResult += line.dropLastWhile { it == ' ' } + "\n"
        }

        return evaluationResult
    }
}
