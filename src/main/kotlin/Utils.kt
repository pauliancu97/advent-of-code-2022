import java.io.File

fun getLines(path: String): List<String> =
    File(path).readLines().map { string -> string.filter { it != '\n' } }