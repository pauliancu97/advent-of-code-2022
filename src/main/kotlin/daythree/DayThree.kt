package daythree

import getLines

private fun getItemPriority(char: Char): Int =
    if (char.isLowerCase()) {
        char.code - 'a'.code + 1
    } else {
        char.code - 'A'.code + 27
    }

private fun getMissplacedPackagePriority(rucksack: String): Int {
    val firstCompartmentItems = rucksack.substring(0 until (rucksack.length / 2)).toSet()
    val secondCompartmentItems = rucksack.substring((rucksack.length / 2) until rucksack.length).toSet()
    val firstCommonItem = (firstCompartmentItems intersect  secondCompartmentItems).first()
    return getItemPriority(firstCommonItem)
}

private fun getTotalPriorities(rucksacks: List<String>) =
    rucksacks.sumOf { getMissplacedPackagePriority(it) }

private fun solvePartOne() {
    val rucksacks = getLines("day_3.txt")
    println(getTotalPriorities(rucksacks))
}

private fun getPriorityForGroup(group: List<String>): Int {
    var currentSet = group.first().toSet()
    for (rucksack in group.drop(1)) {
        val distinctItems = rucksack.toSet()
        currentSet = currentSet intersect distinctItems
    }
    return getItemPriority(currentSet.first())
}

private fun getTotalPriorityForGroups(rucksacks: List<String>, groupSize: Int): Int {
    var totalPriority = 0
    var currentRucksacks = rucksacks
    while (currentRucksacks.isNotEmpty()) {
        val priority = getPriorityForGroup(currentRucksacks.take(groupSize))
        totalPriority += priority
        currentRucksacks = currentRucksacks.drop(groupSize)
    }
    return totalPriority
}

private fun solvePartTwo() {
    val rucksacks = getLines("day_3.txt")
    println(getTotalPriorityForGroups(rucksacks, 3))
}

fun main() {
    //solvePartOne()
    solvePartTwo()
}