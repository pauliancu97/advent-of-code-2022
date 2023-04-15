package dayfive

import getLines

typealias State = MutableList<MutableList<Char>>

private data class Instruction(
    val quantity: Int,
    val source: Int,
    val destination: Int
) {
    companion object {
        private val REGEX = """move (\d+) from (\d+) to (\d+)""".toRegex()

        fun fromString(string: String): Instruction? {
            val match = REGEX.matchEntire(string) ?: return null
            val quantity = match.groupValues.getOrNull(1)?.toIntOrNull() ?: return null
            val source = match.groupValues.getOrNull(2)?.toIntOrNull() ?: return null
            val destination = match.groupValues.getOrNull(3)?.toIntOrNull() ?: return null
            return Instruction(quantity, source - 1, destination - 1)
        }
    }
}

private fun getState(lines: List<String>): State {
    val rows = lines.size
    val numOfStacks = (lines.last().length - 3) / 4 + 1
    val state = MutableList(numOfStacks) {
        mutableListOf<Char>()
    }
    for (stackIndex in 0 until numOfStacks) {
        val col = 4 * stackIndex + 1
        for (row in 0 until (rows - 1)) {
            if (col < lines[row].length && lines[row][col].isUpperCase()) {
                state[stackIndex].add(lines[row][col])
            }
        }
    }
    return state
}

private fun executeInstruction(state: State, instruction: Instruction) {
    val top = state[instruction.source].take(instruction.quantity)
    state[instruction.source] = state[instruction.source].drop(instruction.quantity).toMutableList()
    state[instruction.destination] = (top + state[instruction.destination]).toMutableList()
}

private fun execute(state: State, instructions: List<Instruction>) {
    for (instruction in instructions) {
        executeInstruction(state, instruction)
    }
}

private fun getInput(path: String): Pair<State, List<Instruction>> {
    val lines = getLines(path)
    val emptyLineIndex = lines.indexOfFirst { it.isEmpty() }
    val stateLines = lines.take(emptyLineIndex)
    val instructionsLines = lines.drop(emptyLineIndex + 1)
    val state = getState(stateLines)
    val instructions = instructionsLines.mapNotNull { Instruction.fromString(it) }
    return state to instructions
}

private fun State.getAnswer(): String =
    this.mapNotNull { it.firstOrNull() }.joinToString(separator = "")

private fun solvePartOne() {
    val (state, instructions) = getInput("day_5.txt")
    execute(state, instructions)
    println(state.getAnswer())
}

fun main() {
    solvePartOne()
}