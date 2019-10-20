package task3

import org.expr.MPLCompiler
import java.nio.file.Files
import java.nio.file.Paths

/**
 * args[0] - mypl file for compile
 * args[1] - out path
 */
fun main(args: Array<String>) {
    //TODO: Exceptions
    val sourcePath = Paths.get(args[0])
    val outPath = args[1]

    val sourceCode = String(Files.readAllBytes(sourcePath))
    println(sourceCode)

    val compiler = MPLCompiler()
    val (c_code, _) = compiler.generateC(sourceCode)
    println("\n$c_code")

    compiler.compileC(c_code, outPath)
}
