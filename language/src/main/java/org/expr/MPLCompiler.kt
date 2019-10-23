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

@Early
class MPLCompiler {
    var lastTypeMap: Map<String, String> = mutableMapOf() //TODO: make clearly

    fun evaluate(source: String, input: List<Any> = emptyList()): String {
        val (c, typeMap) = generateC(source)
        lastTypeMap = typeMap

        val path = "c_out.out" //TODO: create tmp file
        compileC(c, path)
        return runC(path, input)
    }

    fun generateC(source: String): Pair<String, Map<String, String>> {
        val lexer = ExprLexer(CharStreams.fromString(source))
        val tokens = CommonTokenStream(lexer)
        val parser = ExprParser(tokens)

        val codeGenerator = CodeGenerator(parser, parser.statement())
        return Pair(codeGenerator.gen(), parser.idToType)
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
            evaluationResult += line + "\n"
        }

        return evaluationResult
    }
}
