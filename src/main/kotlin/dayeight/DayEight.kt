package dayeight

import getLines
import kotlin.math.max

private val OFFSETS = arrayOf(
    -1 to 0,
    0 to +1,
    +1 to 0,
    0 to -1
)

private typealias Matrix = List<List<Int>>
private typealias Coordinate = Pair<Int, Int>

private val Matrix.rows: Int
    get() = this.size

private val Matrix.cols: Int
    get() = this.first().size

private fun Matrix.has(row: Int, col: Int): Boolean =
    row in 0 until this.rows && col in 0 until this.cols

private fun getVisibleTreesFromLeftRight(matrix: Matrix): Set<Coordinate> {
    val visibleTrees = mutableSetOf<Coordinate>()
    for (row in 0 until matrix.rows) {
        var currentMax = -1
        for (col in 0 until matrix.cols) {
            if (matrix[row][col] > currentMax) {
                visibleTrees.add(row to col)
            }
            currentMax = max(currentMax, matrix[row][col])
        }
    }
    return visibleTrees
}

private fun getVisibleTreesFromRightLeft(matrix: Matrix): Set<Coordinate> {
    val visibleTrees = mutableSetOf<Coordinate>()
    for (row in 0 until matrix.rows) {
        var currentMax = -1
        for (col in (matrix.cols - 1) downTo 0) {
            if (matrix[row][col] > currentMax) {
                visibleTrees.add(row to col)
            }
            currentMax = max(currentMax, matrix[row][col])
        }
    }
    return visibleTrees
}

private fun getVisibleTreesFromDownUp(matrix: Matrix): Set<Coordinate> {
    val visibleTrees = mutableSetOf<Coordinate>()
    for (col in 0 until matrix.cols) {
        var currentMax = -1
        for (row in 0 until matrix.rows) {
            if (matrix[row][col] > currentMax) {
                visibleTrees.add(row to col)
            }
            currentMax = max(currentMax, matrix[row][col])
        }
    }
    return visibleTrees
}

private fun getVisibleTreesFromUpDown(matrix: Matrix): Set<Coordinate> {
    val visibleTrees = mutableSetOf<Coordinate>()
    for (col in 0 until matrix.cols) {
        var currentMax = -1
        for (row in (matrix.rows - 1) downTo 0) {
            if (matrix[row][col] > currentMax) {
                visibleTrees.add(row to col)
            }
            currentMax = max(currentMax, matrix[row][col])
        }
    }
    return visibleTrees
}

private fun getVisibleTrees(matrix: Matrix): Set<Coordinate> {
    val leftRight = getVisibleTreesFromLeftRight(matrix)
    val rightLeft = getVisibleTreesFromRightLeft(matrix)
    val downUp = getVisibleTreesFromDownUp(matrix)
    val upDown = getVisibleTreesFromUpDown(matrix)
    return leftRight union rightLeft union downUp union upDown
}

private fun getNumVisibleTreesInDirection(
    matrix: Matrix,
    row: Int,
    col: Int,
    rowOffset: Int,
    colOffset: Int
): Int {
    var currentRow = row + rowOffset
    var currentCol = col + colOffset
    if (!matrix.has(currentRow, currentCol)) {
        return 0
    }
    var numOfVisibleTrees = 1
    while (matrix.has(currentRow, currentCol) && matrix[currentRow][currentCol] < matrix[row][col]) {
        numOfVisibleTrees++
        currentRow += rowOffset
        currentCol += colOffset
    }
    if (!matrix.has(currentRow, currentCol)) {
        numOfVisibleTrees--
    }
    return numOfVisibleTrees
}

private fun getVisibilityScore(matrix: Matrix, row: Int, col: Int): Int {
    var visibilityScore = 1
    for ((rowOffset, colOffset) in OFFSETS) {
        visibilityScore *= getNumVisibleTreesInDirection(matrix, row, col, rowOffset, colOffset)
    }
    return visibilityScore
}

private fun getMaxVisibilityScore(matrix: Matrix): Int {
    var maxVisibilityScore = -1
    for (row in 0 until matrix.rows) {
        for (col in 0 until matrix.cols) {
            maxVisibilityScore = max(maxVisibilityScore, getVisibilityScore(matrix, row, col))
        }
    }
    return maxVisibilityScore
}

private fun getMatrix(path: String): Matrix =
    getLines(path)
        .map { string -> string.map { it.digitToInt() } }


private fun solvePartOne() {
    val matrix = getMatrix("day_8.txt")
    val visibleTrees = getVisibleTrees(matrix)
    println(visibleTrees.size)
}

private fun solvePartTwo() {
    val matrix = getMatrix("day_8.txt")
    val maxVisibilityScore = getMaxVisibilityScore(matrix)
    println(maxVisibilityScore)
}

fun main() {
    //solvePartOne()
    solvePartTwo()
}
