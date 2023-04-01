package dayfour

import getLines

private val REGEX = """(\d+)-(\d+),(\d+)-(\d+)""".toRegex()

private data class Interval(
    val start: Int,
    val end: Int
) {
    fun isContaining(other: Interval): Boolean =
        this.start <= other.start && this.end >= other.end

    fun isOverlapping(other: Interval): Boolean =
        !(this.end < other.start || this.start > other.end)
}

private data class ElvesPair(
    val first: Interval,
    val second: Interval
)

private fun isInefficientPair(elvesPair: ElvesPair): Boolean =
    elvesPair.first.isContaining(elvesPair.second) ||
            elvesPair.second.isContaining(elvesPair.first)

private fun isReallyInefficientPair(elvesPair: ElvesPair) =
    elvesPair.first.isOverlapping(elvesPair.second)

private fun String.toElvesPair(): ElvesPair? {
    val match = REGEX.matchEntire(this) ?: return null
    val firstStart = match.groupValues.getOrNull(1)?.toIntOrNull() ?: return null
    val firstEnd = match.groupValues.getOrNull(2)?.toIntOrNull() ?: return null
    val secondStart = match.groupValues.getOrNull(3)?.toIntOrNull() ?: return null
    val secondEnd = match.groupValues.getOrNull(4)?.toIntOrNull() ?: return null
    return ElvesPair(
        first = Interval(firstStart, firstEnd),
        second = Interval(secondStart, secondEnd)
    )
}

private fun getElvesPairs(path: String): List<ElvesPair> =
    getLines(path).mapNotNull { it.toElvesPair() }

private fun getNumInefficientPairs(elvesPairs: List<ElvesPair>): Int =
    elvesPairs.count { isInefficientPair(it) }

private fun getNumReallyInefficientPairs(elvesPairs: List<ElvesPair>): Int =
    elvesPairs.count { isReallyInefficientPair(it) }

private fun solvePartOne() {
    val elvesPair = getElvesPairs("day_4.txt")
    println(getNumInefficientPairs(elvesPair))
}

private fun solvePartTwo() {
    val elvesPair = getElvesPairs("day_4.txt")
    println(getNumReallyInefficientPairs(elvesPair))
}

fun main() {
    //solvePartOne()
    solvePartTwo()
}