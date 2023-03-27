package dayone

import getLines

private fun getElvesCalories(strings: List<String>): List<List<Int>> {
    val separatorIndices = listOf(-1) +
            strings.withIndex().mapNotNull { (index, string) -> if (string.isEmpty()) index else null } +
            listOf(strings.size)
    return separatorIndices.zipWithNext { start, end ->
        val stringsSublist = strings.subList(start + 1, end)
        stringsSublist.map { it.toInt() }
    }
}

private fun getMaxCaloriesForElf(elves: List<List<Int>>): Int =
    elves.maxOf { calories -> calories.sum() }

private fun getCaloriesOfTopElves(elves: List<List<Int>>): Int =
    elves
        .map { calories -> calories.sum() }
        .sortedDescending()
        .take(3)
        .sum()

private fun solvePartOne() {
    val strings = getLines("day_1.txt")
    val elves = getElvesCalories(strings)
    println(getMaxCaloriesForElf(elves))
}

private fun solvePartTwo() {
    val strings = getLines("day_1.txt")
    val elves = getElvesCalories(strings)
    println(getCaloriesOfTopElves(elves))
}

fun main() {
    solvePartTwo()
}