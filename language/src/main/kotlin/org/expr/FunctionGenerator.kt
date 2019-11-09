package org.expr

import java.lang.IllegalArgumentException
import java.lang.StringBuilder

class FunctionGenerator {
    val prototypes = mutableListOf<String>()
    val definitions = StringBuilder()

    private fun addln() { definitions.append(System.lineSeparator()) }

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

        definitions.append(
            """
            |$type $name() {
            |    ${if (type == "bool") "int" else type} t;
            |    scanf("$template", &t);
            |    return t;
            |}
            """.trimMargin()
        )
        addln()
        addln()
    }

    fun stringRead(type: StringType) {
        val name = "read" + type.toString().normalize()
        val prototype = "char* $name();"

        if (prototype in prototypes) return
        prototypes += prototype

        val template = when (type) {
            StringType.LINE -> "fgets(t, 256, stdin);"
            StringType.STRING -> "scanf(\"%s\", &t);"
        }

        definitions.append(
            """
            |char* $name() {
            |    static char t[256];
            |    $template
            |    return t;
            |}
            """.trimMargin()
        )
        addln()
        addln()
    }

    fun stringConcat() {
        val prototype = "char* concat(char* x, char* y);"
        if (prototype in prototypes) return
        prototypes += prototype

        definitions.append(
            """
            |char* concat(char* x, char* y) {
            |    int len = strlen(x) + strlen(y) + 1;
            |    char* t = malloc(len * sizeof(char));
            |    strcat(t, x);
            |    strcat(t, y);
            |    return t;
            |}
            """.trimMargin()
        )
        addln()
        addln()
    }

    fun arrayConcat(type: String) {
        val prototype = "$type concat($type x, $type y, $type variable, int x_size, int y_size, int* variable_size);"
        if (prototype in prototypes) return
        prototypes += prototype

        definitions.append(
            """
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
            """.trimMargin()
        )
        addln()
        addln()
    }

    fun stringAssign() {
        val prototype = "char* assign(char* x, char* y);"
        if (prototype in prototypes) return
        prototypes += prototype

        definitions.append(
            """
            |char* assign(char* x, char* y) {
            |    if (x == y) return x;
            |    
            |    if (x != 0) free(x);
            |    return strdup(y);
            |}
            """.trimMargin()
        )
        addln()
        addln()
    }

    fun arrayAssign(type: String) {
        val prototype = "$type assignArray($type x, $type y, int* x_size, int* y_size);"
        if (prototype in prototypes) return
        prototypes += prototype

        definitions.append(
            """
            |$type assignArray($type x, $type y, int* x_size, int* y_size) {
            |    if (x == y) return x;
            |    
            |    if (*x_size != 0) free(x);
            |    *x_size = *y_size;
            |    x = malloc(*y_size * sizeof(${type.dropLast(1)}));
            |    for (int i = 0; i < *y_size; i++) {
            |        x[i] = y[i];
            |    }
            |    return x;
            |}
            """.trimMargin()
        )
        addln()
        addln()
    }

    fun castToString(type: String) {
        val prototype = "char* ${type}_to_string($type x);"
        if (prototype in prototypes) return
        prototypes += prototype

        when (type) {
            "char" -> {
                definitions.append(
                    """
                    |char* ${type}_to_string($type x) {
                    |    int len = 1;
                    |    char* t = malloc(len * sizeof(char));
                    |    t[0] = x;
                    |
                    |    return t;
                    |}
                    """.trimMargin()
                )
            }
            "double" -> {
                definitions.append(
                    """
                    |char* ${type}_to_string($type x) {
                    |    bool negative = (x >= 0 ? false : true);
                    |    if (x < 0) x = -x;
                    |    
                    |    long long left = x;
                    |    
                    |    int len = 0;
                    |    do {
                    |       len++;
                    |       left /= 10;
                    |    } while (left > 0);
                    |    left = x;
                    |    
                    |    if (negative) len++;
                    |
                    |    char* t = malloc((len + 1 + 6) * sizeof(char));
                    |    for (int i = len - 1; i >= 0; i--) {
                    |        t[i] = '0' + (left % 10);
                    |        left /= 10;
                    |    }
                    |    if (negative) t[0] = '-';
                    |
                    |    t[len] = '.';
                    |   
                    |    if (x < 0) x = -x;
                    |    long long right = (x - left) * 1000000;
                    |    for (int i = (len + 1 + 6) - 1; i > len; i--) {
                    |        t[i] = '0' + (right % 10);
                    |        right /= 10;
                    |    }
                    |    return t;
                    |}
                    """.trimMargin()
                )
            }
            else -> {
                definitions.append(
                    """
                    |char* ${type}_to_string($type x) {
                    |    bool negative = (x >= 0 ? false : true);
                    |    if (x < 0) x = -x;
                    |    
                    |    int tmp_x = x;
                    |    int len = 0;
                    |    do {
                    |       len++;
                    |       tmp_x /= 10;
                    |    } while (tmp_x > 0);
                    |
                    |    if (negative) len++;
                    |    
                    |    char* t = malloc(len * sizeof(char));
                    |    for (int i = len - 1; i >= 0; i--) {
                    |        t[i] = '0' + (x % 10);
                    |        x /= 10;
                    |    } 
                    |    if (negative) t[0] = '-';
                    |    
                    |    return t;
                    |}
                    """.trimMargin()
                )
            }
        }
        addln()
        addln()
    }

    fun copyArray(type: String) {
        val typeParameter = type.dropLast(1)
        val prototype = "$type copy_${typeParameter}_array($type a, int a_size);"
        if (prototype in prototypes) return
        prototypes += prototype

        definitions.append(
            """
            |$type copy_${typeParameter}_array($type a, int a_size) {
            |    $type t = malloc(a_size * sizeof($typeParameter));
            |    for (int i = 0; i < a_size; i++) {
            |        t[i] = a[i];
            |    }
            |    return t;
            |}
            """.trimMargin()
        )
        addln()
        addln()
    }

    private fun String.normalize() = this[0].toUpperCase() + this.split(" ").first().drop(1).toLowerCase()

    enum class StringType { LINE, STRING }
}
