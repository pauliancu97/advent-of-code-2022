package day7

import getLines

private const val TOTAL_SPACE = 70_000_000L
private const val REQUIRED_SPACE = 30_000_000L

private sealed class Entry {
    abstract val name: String
    data class File(override val name: String, val size: Long) : Entry()
    data class Directory(
        override val name: String,
        val entries: MutableMap<String, Entry> = mutableMapOf(),
        val parent: Directory? = null
    ) : Entry()
}

private sealed class ChangeDirectoryInput {
    object Root : ChangeDirectoryInput()
    object Parent : ChangeDirectoryInput()
    data class Child(val name: String) : ChangeDirectoryInput()
}

private sealed class Command {
    data class ChangeDirectory(val input: ChangeDirectoryInput) : Command() {
        companion object {
            private const val ROOT = "/"
            private const val PARENT = ".."
            private val REGEX = """cd (.+)""".toRegex()

            fun fromString(string: String): ChangeDirectory? {
                if (!string.startsWith("$ ")) {
                    return null
                }
                val actualString = string.drop(2)
                val match = REGEX.matchEntire(actualString) ?: return null
                val inputString = match.groupValues.getOrNull(1) ?: return null
                val input = when (inputString) {
                    ROOT -> ChangeDirectoryInput.Root
                    PARENT -> ChangeDirectoryInput.Parent
                    else -> ChangeDirectoryInput.Child(inputString)
                }
                return ChangeDirectory(input)
            }
        }
    }
    object ListDirectory : Command() {
        fun fromString(string: String): ListDirectory? =
            if (string == "$ ls") ListDirectory else null
    }
}

private sealed class ListDirectoryOutput {
    data class DirectoryOutput(val name: String) : ListDirectoryOutput() {
        companion object {
            private val REGEX = """dir (.*)""".toRegex()

            fun fromString(string: String): DirectoryOutput? {
                val match = REGEX.matchEntire(string) ?: return null
                val name = match.groupValues.getOrNull(1) ?: return null
                return DirectoryOutput(name)
            }
        }
    }
    data class FileOutput(val name: String, val size: Long) : ListDirectoryOutput() {
        companion object {
            private val REGEX = """(\d+) (.+)""".toRegex()

            fun fromString(string: String): FileOutput? {
                val match = REGEX.matchEntire(string) ?: return null
                val size = match.groupValues.getOrNull(1)?.toLongOrNull() ?: return null
                val name = match.groupValues.getOrNull(2) ?: return null
                return FileOutput(name, size)
            }
        }
    }
}

private sealed class CommandLine {
    data class Input(val command: Command) : CommandLine() {
        companion object {
            fun fromString(string: String): Input? {
                val command = Command.ListDirectory.fromString(string) ?:
                    Command.ChangeDirectory.fromString(string) ?:
                    return null
                return Input(command)
            }
        }
    }
    data class Output(val output: ListDirectoryOutput) : CommandLine() {
        companion object {
            fun fromString(string: String): Output? {
                val output = ListDirectoryOutput.DirectoryOutput.fromString(string) ?:
                    ListDirectoryOutput.FileOutput.fromString(string) ?:
                    return null
                return Output(output)
            }
        }
    }
}

private fun String.toCommandLine(): CommandLine? =
    CommandLine.Input.fromString(this) ?: CommandLine.Output.fromString(this)

private fun getStateAfterChangeDirectoryCommand(
    rootDirectory: Entry.Directory,
    currentDirectory: Entry.Directory,
    command: Command.ChangeDirectory
): Entry.Directory =
    when (command.input) {
        is ChangeDirectoryInput.Child -> {
            val childDirectory = currentDirectory.entries[command.input.name] as? Entry.Directory
            if (childDirectory != null) {
                childDirectory
            } else {
                val createdChildDirectory = Entry.Directory(
                    name = command.input.name,
                    parent = currentDirectory
                )
                currentDirectory.entries[command.input.name] = createdChildDirectory
                createdChildDirectory
            }
        }
        ChangeDirectoryInput.Parent -> currentDirectory.parent ?: error("No parent directory")
        ChangeDirectoryInput.Root -> rootDirectory
    }

private fun getStateAfterListDirectoryCommand(
    currentDirectory: Entry.Directory,
    outputs: List<ListDirectoryOutput>
) {
    for (output in outputs) {
        val (name, entry) = when (output) {
            is ListDirectoryOutput.FileOutput -> {
                output.name to Entry.File(name = output.name, size = output.size)
            }
            is ListDirectoryOutput.DirectoryOutput -> {
                output.name to Entry.Directory(name = output.name, parent = currentDirectory)
            }
        }
        currentDirectory.entries[name] = entry
    }
}

private fun getState(rootDirectory: Entry.Directory, commandLines: List<CommandLine>) {
    var currentDirectory = rootDirectory
    var currentCommandLines = commandLines
    while (currentCommandLines.isNotEmpty()) {
        val command = (currentCommandLines.firstOrNull() as? CommandLine.Input)?.command
            ?: error("Should be command input")
        currentCommandLines = currentCommandLines.drop(1)
        when (command) {
            is Command.ChangeDirectory -> {
                currentDirectory = getStateAfterChangeDirectoryCommand(rootDirectory, currentDirectory, command)
            }
            is Command.ListDirectory -> {
                var index = 0
                while (index < currentCommandLines.size && currentCommandLines[index] is CommandLine.Output) {
                    index++
                }
                val outputs = currentCommandLines.take(index).mapNotNull {
                    (it as? CommandLine.Output)?.output
                }
                currentCommandLines = currentCommandLines.drop(index)
                getStateAfterListDirectoryCommand(currentDirectory, outputs)
            }
        }
    }
}

private fun getFinalState(commandLines: List<CommandLine>): Entry.Directory {
    val rootDirectory = Entry.Directory(name = "/")
    getState(rootDirectory, commandLines)
    return rootDirectory
}

private fun getCommandLines(path: String): List<CommandLine> =
    getLines(path).mapNotNull { it.toCommandLine() }

private fun getDirectoriesSizesHelper(directory: Entry.Directory, sizes: MutableList<Long>): Long {
    val filesSize = directory.entries.values.filterIsInstance<Entry.File>().sumOf { it.size }
    val directorySizes =
        directory.entries.values.filterIsInstance<Entry.Directory>().sumOf { getDirectoriesSizesHelper(it, sizes) }
    val totalSize = filesSize + directorySizes
    sizes.add(totalSize)
    return totalSize
}

private fun getDirectoriesSizes(rootDirectory: Entry.Directory): List<Long> {
    val sizes = mutableListOf<Long>()
    getDirectoriesSizesHelper(rootDirectory, sizes)
    return sizes
}

private fun getSmallDirectoriesTotalSize(rootDirectory: Entry.Directory): Long {
    val sizes = getDirectoriesSizes(rootDirectory)
    return sizes.filter { it <= 100_000L }.sum()
}

private fun getSmallestDirectoryToDelete(rootDirectory: Entry.Directory): Long {
    val sizes = getDirectoriesSizes(rootDirectory)
    val freeSpace = TOTAL_SPACE - sizes.max()
    val deleteSpace = REQUIRED_SPACE - freeSpace
    return sizes.filter { it >= deleteSpace }.min()
}

private fun solvePartOne() {
    val commandLines = getCommandLines("day_7.txt")
    val rootDirectory = getFinalState(commandLines)
    val answer = getSmallDirectoriesTotalSize(rootDirectory)
    println(answer)
}

private fun solvePartTwo() {
    val commandLines = getCommandLines("day_7.txt")
    val rootDirectory = getFinalState(commandLines)
    val answer = getSmallestDirectoryToDelete(rootDirectory)
    println(answer)
}

fun main() {
    solvePartTwo()
}

