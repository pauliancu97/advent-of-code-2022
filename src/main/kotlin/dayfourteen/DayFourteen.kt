package dayfourteen

import getLines
import kotlin.math.max
import kotlin.math.min

private typealias Matrix = List<MutableList<Boolean>>

private val Matrix.rows: Int
    get() = this.size

private val Matrix.cols: Int
    get() = this.first().size

private operator fun Matrix.get(row: Int, col: Int): Boolean = this[row][col]

private operator fun Matrix.set(row: Int, col: Int, value: Boolean) {
    this[row][col] = value
}

private fun Matrix(rows: Int, cols: Int): Matrix =
    List(rows) { MutableList(cols) { false } }

private fun Matrix.isInbounds(row: Int, col: Int) =
    row in 0 until this.rows && col in 0 until this.cols

private data class InfiniteMatrix(
    val matrix: MutableMap<Coordinate, Boolean>,
    val rows: Int
) {
    operator fun get(row: Int, col: Int): Boolean =
        if (row == rows - 1) true else this.matrix[Coordinate(row, col)] ?: false

    operator fun set(row: Int, col: Int, value: Boolean) {
        this.matrix[Coordinate(row, col)] = value
    }
}

private fun InfiniteMatrix(rows: Int, cols: Int): InfiniteMatrix {
    val matrix: MutableMap<Coordinate, Boolean> = mutableMapOf()
    for (row in 0 until (rows - 1)) {
        for (col in 0 until cols) {
            matrix[Coordinate(row, col)] = false
        }
    }
    for (col in 0 until cols) {
        matrix[Coordinate(row = rows - 1, col = col)] = true
    }
    return InfiniteMatrix(matrix, rows)
}

private data class CaveCoordinate(val x: Int, val y: Int)

private data class Coordinate(val row: Int, val col: Int)

private typealias CavePath = List<CaveCoordinate>

private typealias Path = List<Coordinate>

private fun String.toCaveCoordinate(): CaveCoordinate {
    val (xString, yString) = this.split(",")
    val x = xString.toInt()
    val y = yString.toInt()
    return CaveCoordinate(x, y)
}

private fun getCoordinate(caveCoordinate: CaveCoordinate, offsetX: Int, offsetY: Int): Coordinate =
    Coordinate(row = caveCoordinate.y - offsetY, col = caveCoordinate.x - offsetX)

private fun getPath(cavePath: CavePath, offsetX: Int, offsetY: Int): Path =
    cavePath.map { getCoordinate(it, offsetX, offsetY) }

private fun String.toCavePath(): CavePath =
    this.split(" -> ")
        .map { it.toCaveCoordinate() }

private fun placePath(matrix: Matrix, path: Path) {
    for ((first, second) in path.zipWithNext()) {
        if (first.row == second.row) {
            val row = first.row
            val startCol = min(first.col, second.col)
            val endCol = max(first.col, second.col)
            for (col in startCol..endCol) {
                matrix[row, col] = true
            }
        } else if (first.col == second.col) {
            val col = first.col
            val startRow = min(first.row, second.row)
            val endRow = max(first.row, second.row)
            for (row in startRow..endRow) {
                matrix[row, col] = true
            }
        }
    }
}

private fun placePath(matrix: InfiniteMatrix, path: Path) {
    for ((first, second) in path.zipWithNext()) {
        if (first.row == second.row) {
            val row = first.row
            val startCol = min(first.col, second.col)
            val endCol = max(first.col, second.col)
            for (col in startCol..endCol) {
                matrix[row, col] = true
            }
        } else if (first.col == second.col) {
            val col = first.col
            val startRow = min(first.row, second.row)
            val endRow = max(first.row, second.row)
            for (row in startRow..endRow) {
                matrix[row, col] = true
            }
        }
    }
}

private fun getCave(path: String): Pair<Matrix, Coordinate> {
    val strings = getLines(path)
    val cavePaths = strings.map { it.toCavePath() }
    val minX = cavePaths.flatMap { cavePath -> cavePath.map { it.x } }.min()
    val minY = 0
    val maxX = cavePaths.flatMap { cavePath -> cavePath.map { it.x } }.max()
    val maxY = cavePaths.flatMap { cavePath -> cavePath.map { it.y } }.max()
    val rows = maxY - minY + 1
    val cols = maxX - minX + 1
    val matrix = Matrix(rows, cols)
    val paths = cavePaths.map { cavePath -> getPath(cavePath, minX, minY) }
    paths.forEach { placePath(matrix, it) }
    val coordinate = Coordinate(row = 0, col = 500 - minX)
    return matrix to coordinate
}

private fun getCaveInfinite(path: String): Pair<InfiniteMatrix, Coordinate> {
    val strings = getLines(path)
    val cavePaths = strings.map { it.toCavePath() }
    val minX = cavePaths.flatMap { cavePath -> cavePath.map { it.x } }.min()
    val minY = 0
    val maxX = cavePaths.flatMap { cavePath -> cavePath.map { it.x } }.max()
    val maxY = cavePaths.flatMap { cavePath -> cavePath.map { it.y } }.max()
    val rows = maxY - minY + 3
    val cols = maxX - minX + 1
    val matrix = InfiniteMatrix(rows, cols)
    val paths = cavePaths.map { cavePath -> getPath(cavePath, minX, minY) }
    paths.forEach { placePath(matrix, it) }
    val coordinate = Coordinate(row = 0, col = 500 - minX)
    return matrix to coordinate
}

private fun updateSand(matrix: Matrix, row: Int, col: Int): Boolean {
    var currentRow = row
    var currentCol = col
    var shouldContinue = true
    while (shouldContinue) {
        if (currentRow + 1 < matrix.rows) {
            if (!matrix[currentRow + 1, currentCol]) {
                currentRow += 1
                continue
            }
        } else {
            shouldContinue = false
            currentRow += 1
            continue
        }
        if (currentCol - 1 >= 0) {
            if (!matrix[currentRow + 1, currentCol - 1]) {
                currentRow += 1
                currentCol -= 1
                continue
            }
        } else {
            shouldContinue = false
            currentRow += 1
            currentCol -= 1
            continue
        }
        if (currentCol + 1 < matrix.cols) {
            if (!matrix[currentRow + 1, currentCol + 1]) {
                currentRow += 1
                currentCol += 1
                continue
            }
        } else {
            shouldContinue = false
            currentRow += 1
            currentCol += 1
            continue
        }
        shouldContinue = false
    }
    val shouldPlace = matrix.isInbounds(currentRow, currentCol)
    if (shouldPlace) {
        matrix[currentRow, currentCol] = true
    }
    return shouldPlace
}

private fun updateSandInfiniteMatrix(matrix: InfiniteMatrix, row: Int, col: Int): Boolean {
    var currentRow = row
    var currentCol = col
    var shouldContinue = true
    while (shouldContinue) {
        if (!matrix[currentRow + 1, currentCol]) {
            currentRow += 1
            continue
        }
        if (!matrix[currentRow + 1, currentCol - 1]) {
            currentRow += 1
            currentCol -= 1
            continue
        }
        if (!matrix[currentRow + 1, currentCol + 1]) {
            currentRow += 1
            currentCol += 1
            continue
        }
        shouldContinue = false
    }
    val shouldPlace = !(currentRow == row && currentCol == col)
    if (shouldPlace) {
        matrix[currentRow, currentCol] = true
    }
    return shouldPlace
}

private fun getNumOfSandParticles(matrix: Matrix, row: Int, col: Int): Int {
    var numOfSandParticles = 0
    var shouldContinue = true
    while (shouldContinue) {
        shouldContinue = updateSand(matrix, row, col)
        if (shouldContinue) {
            numOfSandParticles++
        }
    }
    return numOfSandParticles
}

private fun getNumOfSandParticlesInfiniteMatrix(matrix: InfiniteMatrix, row: Int, col: Int): Int {
    var numOfSandParticles = 0
    var shouldContinue = true
    while (shouldContinue) {
        shouldContinue = updateSandInfiniteMatrix(matrix, row, col)
        numOfSandParticles++
    }
    return numOfSandParticles
}

private fun Matrix.getRepresentation(): String {
    var string = ""
    for (row in 0 until this.rows) {
        for (col in 0 until this.cols) {
            string += if (this[row, col]) "#" else "."
        }
        string += "\n"
    }
    return string
}

private fun solvePartOne() {
    val (matrix, coordinate) = getCave("day_14.txt")
    val answer = getNumOfSandParticles(matrix, coordinate.row, coordinate.col)
    println(answer)
}

private fun solvePartTwo() {
    val (matrix, coordinate) = getCaveInfinite("day_14.txt")
    val answer = getNumOfSandParticlesInfiniteMatrix(matrix, coordinate.row, coordinate.col)
    println(answer)
}

fun main() {
    //solvePartOne()
    solvePartTwo()
}