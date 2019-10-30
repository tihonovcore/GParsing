package org.expr

import java.lang.IllegalArgumentException
import java.lang.StringBuilder

class FunctionGenerator {
    val prototypes = mutableListOf<String>()
    val definitions = StringBuilder()

    fun simpleRead(type: String) {
        val name = "read" + type.normalize()
        val prototype = "$type $name();"

        if (prototype in prototypes) return
        prototypes += prototype

        val template = when (type) {
            "int" -> "%d"
            "bool" -> "%d"
            "long long" -> "%lld"
            "double" -> "%lf"
            "char" -> "%c"
            else -> throw IllegalArgumentException("Unexpected type: $type")
        }

        definitions.append("""
            |$type $name() {
            |    ${if (type == "bool") "int" else type} t;
            |    scanf("$template", &t);
            |    return t;
            |}
        """.trimMargin())
        definitions.append(System.lineSeparator())
    }

    fun stringRead(type: StringType) {
        val name = "read" + type.toString().normalize()
        val prototype = "char* $name();"

        if (prototype in prototypes) return
        prototypes += prototype

        val template = when(type) {
            StringType.LINE -> "fgets(t, 256, stdin);"
            StringType.STRING -> "scanf(\"%s\", &t);"
        }

        definitions.append("""
            |char* $name() {
            |    static char t[256];
            |    $template
            |    return t;
            |}
        """.trimMargin())
        definitions.append(System.lineSeparator())
    }

    fun stringConcat() {
        val prototype = "char* concat(char* x, char* y);"
        if (prototype in prototypes) return
        prototypes += prototype

        definitions.append("""
            |char* concat(char* x, char* y) {
            |    int len = strlen(x) + strlen(y) + 1;
            |    char* t = malloc(len);
            |    strcat(t, x);
            |    strcat(t, y);
            |    return t;
            |}
        """.trimMargin())
        definitions.append(System.lineSeparator())
    }

    fun arrayConcat(type: String) {
        val prototype = "$type concat($type x, $type y, $type variable, int x_size, int y_size, int* variable_size);"
        if (prototype in prototypes) return
        prototypes += prototype

        definitions.append("""
            |$type concat($type x, $type y, $type variable, int x_size, int y_size, int* variable_size) {
            |    int len = x_size + y_size;
            |    $type t = malloc(len * sizeof(${type.dropLast(1)}));
            |    for (int i = 0; i < x_size; i++) {
            |        t[i] = x[i];
            |    }
            |    for (int i = 0; i < y_size; i++) {
            |        t[i + x_size] = y[i];
            |    }
            |
            |    if (*variable_size != 0) free(variable);
            |    *variable_size = len;
            |    return t;
            |}
        """.trimMargin())
        definitions.append(System.lineSeparator())
    }

    fun stringAssign() {
        val prototype = "char* assign(char* x, char* y);"
        if (prototype in prototypes) return
        prototypes += prototype

        definitions.append("""
            |char* assign(char* x, char* y) {
            |    free(x);
            |    return strdup(y);
            |}
        """.trimMargin())
        definitions.append(System.lineSeparator())
    }

    fun arrayAssign(type: String) {
        val prototype = "$type assignArray($type x, $type y, int* x_size, int* y_size);"
        if (prototype in prototypes) return
        prototypes += prototype

        definitions.append("""
            |$type assignArray($type x, $type y, int* x_size, int* y_size) {
            |    if (*x_size != 0) free(x);
            |    *x_size = *y_size;
            |    x = malloc(*y_size * sizeof(${type.dropLast(1)}));
            |    for (int i = 0; i < *y_size; i++) {
            |         x[i] = y[i];
            |    }
            |    return x;
            |}
        """.trimMargin())
        definitions.append(System.lineSeparator())
    }

    private fun String.normalize() = this[0].toUpperCase() + this.split(" ").first().drop(1).toLowerCase()

    enum class StringType { LINE, STRING }
}
