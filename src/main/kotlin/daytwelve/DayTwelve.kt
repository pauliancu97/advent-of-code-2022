package daytwelve

import getLines
import java.util.PriorityQueue
import kotlin.math.abs
import kotlin.math.min

private typealias Matrix = MutableList<MutableList<Int>>

private val Matrix.rows: Int
    get() = this.size

private val Matrix.cols: Int
    get() = this.first().size

private operator fun Matrix.get(coordinate: Coordinate): Int = this[coordinate.row][coordinate.col]

private operator fun Matrix.set(coordinate: Coordinate, value: Int) {
    this[coordinate.row][coordinate.col] = value
}

private fun Matrix.isInbounds(coordinate: Coordinate): Boolean =
    coordinate.row in 0 until this.rows && coordinate.col in 0 until this.cols

private fun Matrix(rows: Int, cols: Int, func: (Int, Int) -> Int): Matrix =
    MutableList(rows) { row ->
        MutableList(cols) { col ->
            func(row, col)
        }
    }

private fun Matrix.getString(): String {
    var string = ""
    for (row in 0 until this.rows) {
        for (col in 0 until this.cols) {
            string += if (this[row][col] < 10) " ${this[row][col]} " else "${this[row][col]} "
        }
        string += "\n"
    }
    return string
}

private data class Coordinate(
    val row: Int = 0,
    val col: Int = 0
) {
    companion object {
        private val OFFSETS = arrayOf(
            Coordinate(row = -1, col = 0),
            Coordinate(row = 0, col = +1),
            Coordinate(row = +1, col = 0),
            Coordinate(row = 0, col = -1),
        )
    }

    operator fun plus(other: Coordinate): Coordinate =
        Coordinate(row = this.row + other.row, col = this.col + other.col)

    fun getNeighbours(): List<Coordinate> =
        OFFSETS.map { offset -> this + offset }
}

private fun getValidNeighbours(coordinate: Coordinate, heights: Matrix, visited: Set<Coordinate>): List<Coordinate> =
    coordinate.getNeighbours()
        .filter { updatedCoordinate ->
            if (heights.isInbounds(updatedCoordinate)) {
                val isNotVisited = updatedCoordinate !in visited
                val originalHeight = heights[coordinate]
                val updatedHeight = heights[updatedCoordinate]
                val isHeightDifferenceValid = updatedHeight - originalHeight <= 1
                isHeightDifferenceValid && isNotVisited
            } else {
                false
            }
        }

private fun getEnergyCost(
    start: Coordinate,
    destination: Coordinate,
    heights: Matrix
): Int {
    val queue: PriorityQueue<Pair<Int, Coordinate>> = PriorityQueue(compareBy { (numOfSteps, _) -> -numOfSteps })
    val visited: MutableSet<Coordinate> = mutableSetOf()
    val costs = Matrix(heights.rows, heights.cols) { row, col ->
        if (start.row == row && start.col == col) 0 else -1
    }
    queue.add(0 to start)
    while (queue.isNotEmpty()) {
        val (numOfSteps, coordinate) = queue.poll()
        visited.add(coordinate)
        for (updatedCoordinate in getValidNeighbours(coordinate, heights, visited)) {
            val updatedCost = numOfSteps + 1 + abs(heights[updatedCoordinate] - heights[coordinate])
            if (costs[updatedCoordinate] == -1 || updatedCost < costs[updatedCoordinate]) {
                costs[updatedCoordinate] = updatedCost
                queue.add(updatedCost to updatedCoordinate)
            }
        }
    }
    return costs[destination]
}

private fun getNumOfSteps(
    start: Coordinate,
    destination: Coordinate,
    heights: Matrix
): Int {
    val queue: MutableList<Pair<Coordinate, Int>> = mutableListOf(start to 0)
    val visited: MutableSet<Coordinate> = mutableSetOf(start)
    while (queue.isNotEmpty()) {
        val (coordinate, numOfSteps) = queue.removeFirst()
        if (coordinate == destination) {
            return numOfSteps
        }
        for (updatedCoordinate in getValidNeighbours(coordinate, heights, visited)) {
            queue.add(updatedCoordinate to (numOfSteps + 1))
            visited.add(updatedCoordinate)
        }
    }
    return -1
}

private fun getHeights(path: String): Triple<Coordinate, Coordinate, Matrix> {
    val strings = getLines(path)
    val heightsMatrix = strings.map { string ->
        string.map { char ->
            when (char) {
                'S' -> 0
                'E' -> 'z'.code - 'a'.code
                else -> char.code - 'a'.code
            }
        }
    }
    val rows = strings.size
    val cols = strings.first().length
    var start = Coordinate()
    var destination = Coordinate()
    for (row in 0 until rows) {
        for (col in 0 until cols) {
            when (strings[row][col]) {
                'S' -> start = Coordinate(row, col)
                'E' -> destination = Coordinate(row, col)
                else -> {}
            }
        }
    }
    val matrix = Matrix(rows, cols) { row, col -> heightsMatrix[row][col] }
    return Triple(start, destination, matrix)
}

private fun getMinNumOfSteps(destination: Coordinate, heights: Matrix): Int {
    var minNumOfSteps = Int.MAX_VALUE
    for (row in 0 until heights.rows) {
        for (col in 0 until heights.cols) {
            val coordinate = Coordinate(row, col)
            if (heights[coordinate] == 0) {
                val numOfSteps = getNumOfSteps(
                    coordinate,
                    destination,
                    heights
                )
                if (numOfSteps > 0) {
                    minNumOfSteps = min(minNumOfSteps, numOfSteps)
                }
            }
        }
    }
    return minNumOfSteps
}

private fun solvePartOne() {
    val (start, destination, heights) = getHeights("day_12.txt")
    println(getNumOfSteps(start, destination, heights))
}

private fun solvePartTwo() {
    val (start, destination, heights) = getHeights("day_12.txt")
    println(getMinNumOfSteps(destination, heights))
}

fun main() {
    //solvePartOne()
    solvePartTwo()
}