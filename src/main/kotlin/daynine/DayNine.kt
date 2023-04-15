package daynine

import getLines
import kotlin.math.abs
import kotlin.math.sign

private data class Coordinate(
    val x: Int = 0,
    val y: Int = 0
) {
    operator fun plus(other: Coordinate) =
        Coordinate(x = this.x + other.x, y = this.y + other.y)
}

private fun Coordinate.isLinked(other: Coordinate): Boolean =
    abs(this.x - other.x) <= 1 && abs(this.y - other.y) <= 1

private enum class Direction(val offset: Coordinate, private val string: String) {
    Up(Coordinate(x = 0, y = 1), "U"),
    Down(Coordinate(x = 0, y = -1), "D"),
    Left(Coordinate(x = -1, y = 0), "L"),
    Right(Coordinate(x = 1, y = 0), "R");

    companion object {
        fun fromString(string: String) =
            Direction.values().first { it.string == string }
    }
}

private data class Instruction(
    val direction: Direction,
    val steps: Int
)

private data class Rope(
    val head: Coordinate = Coordinate(),
    val tail: Coordinate = Coordinate()
) {
    private fun isCorrect(): Boolean =
        abs(head.x - tail.x) <= 1 && abs(head.y - tail.y) <= 1

    fun getUpdated(direction: Direction): Rope {
        val previousHead = this.head
        val updatedHead = this.head + direction.offset
        val updatedRope = Rope(head = updatedHead, tail = this.tail)
        return if (updatedRope.isCorrect()) {
            updatedRope
        } else {
            updatedRope.copy(tail = previousHead)
        }
    }
}

private data class LongRope(
    private val nodes: List<Coordinate> = List(10) { Coordinate() }
) {
    val tail: Coordinate = nodes.last()

    fun getUpdated(direction: Direction): LongRope {
        val updatedNodes = this.nodes.toMutableList()
        updatedNodes[0] = updatedNodes[0] + direction.offset
        for (index in 1 until this.nodes.size) {
            if (!updatedNodes[index].isLinked(updatedNodes[index - 1])) {
                val xDiff = (updatedNodes[index - 1].x - updatedNodes[index].x).sign
                val yDiff = (updatedNodes[index - 1].y - updatedNodes[index].y).sign
                updatedNodes[index] = updatedNodes[index] + Coordinate(x = xDiff, y = yDiff)
            }
        }
        return LongRope(nodes = updatedNodes)
    }
}

private fun getUpdatedAfterInstruction(rope: Rope, instruction: Instruction): Pair<Rope, Set<Coordinate>> {
    val tailCoordinates = mutableSetOf(rope.tail)
    var currentRope = rope
    repeat(instruction.steps) {
        currentRope = currentRope.getUpdated(instruction.direction)
        tailCoordinates.add(currentRope.tail)
    }
    return currentRope to tailCoordinates
}

private fun getTailVisitedCoordinates(rope: Rope, instructions: List<Instruction>): Set<Coordinate> {
    val tailCoordinates = mutableSetOf<Coordinate>()
    var currentRope = rope
    for (instruction in instructions) {
        val (updatedRope, updatedTailCoordinates) = getUpdatedAfterInstruction(currentRope, instruction)
        currentRope = updatedRope
        tailCoordinates.addAll(updatedTailCoordinates)
    }
    return tailCoordinates
}

private fun getLongRopeUpdatedAfterInstruction(
    longRope: LongRope,
    instruction: Instruction
): Pair<LongRope, Set<Coordinate>> {
    val tailCoordinates = mutableSetOf(longRope.tail)
    var currentLongRope = longRope
    repeat(instruction.steps) {
        currentLongRope = currentLongRope.getUpdated(instruction.direction)
        tailCoordinates.add(currentLongRope.tail)
    }
    return currentLongRope to tailCoordinates
}

private fun getLongRopeVisitedCoordinates(
    longRope: LongRope,
    instructions: List<Instruction>
): Set<Coordinate> {
    val tailCoordinates = mutableSetOf<Coordinate>()
    var currentLongRope = longRope
    for (instruction in instructions) {
        val (updatedLongRope, updatedTailCoordinates) = getLongRopeUpdatedAfterInstruction(currentLongRope, instruction)
        currentLongRope = updatedLongRope
        tailCoordinates.addAll(updatedTailCoordinates)
    }
    return tailCoordinates
}

private fun String.toInstruction(): Instruction {
    val (directionString, stepsString) = this.split(" ")
    val direction = Direction.fromString(directionString)
    val steps = stepsString.toInt()
    return Instruction(direction, steps)
}

private fun getInstructions(path: String): List<Instruction> =
    getLines(path).map { it.toInstruction() }

private fun solvePartOne() {
    val instructions = getInstructions("day_9.txt")
    val tailCoordinates = getTailVisitedCoordinates(Rope(), instructions)
    println(tailCoordinates.size)
}

private fun solvePartTwo() {
    val instructions = getInstructions("day_9.txt")
    val tailCoordinates = getLongRopeVisitedCoordinates(LongRope(), instructions)
    println(tailCoordinates.size)
}

fun main() {
    //solvePartOne()
    solvePartTwo()
}