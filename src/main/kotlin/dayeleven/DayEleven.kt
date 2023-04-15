package dayeleven

import getLines
import java.math.BigInteger

private const val NUM_OF_ROUNDS = 10000

private typealias MonkeyMap = Map<Int, Monkey>
private typealias MonkeysState = Map<Int, List<BigInteger>>
private typealias Counter = MutableMap<Int, Long>

private fun MonkeyMap.toMonkeysState(): MonkeysState =
    this.mapValues { (_, monkey) -> monkey.itemsWorryLevels.toList() }

private fun Counter(): Counter = mutableMapOf()

private fun Counter.multiply(scaler: Int): Counter {
    val multiplied = Counter()
    for (key in this.keys) {
        multiplied[key] = (this[key] ?: 0L) * scaler
    }
    return multiplied
}

private fun Counter.add(other: Counter): Counter {
    val added = Counter()
    val allKeys = this.keys + other.keys
    for (key in allKeys) {
        added[key] = (this[key] ?: 0L) + (other[key] ?: 0L)
    }
    return added
}

private fun Counter.diff(other: Counter): Counter {
    val difference = Counter()
    val allKeys = this.keys + other.keys
    for (key in allKeys) {
        difference[key] = (this[key] ?: 0L) - (other[key] ?: 0L)
    }
    return difference
}

private sealed class Operand {
    object Old : Operand()
    data class Number(val value: Int) : Operand()

    fun getEvaluated(oldValue: BigInteger): BigInteger =
        when (this) {
            Old -> oldValue
            is Number -> BigInteger.valueOf(this.value.toLong())
        }
}

private enum class Operator {
    Plus,
    Multiply
}

private data class Expression(
    val firstOperand: Operand,
    val secondOperand: Operand,
    val operator: Operator
) {
    fun evaluated(oldValue: BigInteger): BigInteger {
        val firstOperandValue = firstOperand.getEvaluated(oldValue)
        val secondOperandValue = secondOperand.getEvaluated(oldValue)
        return when (this.operator) {
            Operator.Plus -> firstOperandValue + secondOperandValue
            Operator.Multiply -> firstOperandValue * secondOperandValue
        }
    }
}

private data class Monkey(
    val id: Int,
    val itemsWorryLevels: MutableList<BigInteger>,
    val expression: Expression,
    val testNumber: Int,
    val trueBranchMonkeyId: Int,
    val falseBranchMonkeyId: Int
) {
    companion object {
        private val MONKEY_ID_REGEX = """Monkey (\d+):""".toRegex()
        private val ITEMS_REGEX = """\s*Starting items: (.+)""".toRegex()
        private val OPERATION_REGEX = """\s*Operation: new = (.*)""".toRegex()
        private val TEST_REGEX = """\s*Test: divisible by (\d+)""".toRegex()
        private val IF_TRUE_REGEX = """\s*If true: throw to monkey (\d+)""".toRegex()
        private val IF_FALSE_REGEX = """\s*If false: throw to monkey (\d+)""".toRegex()
        private const val ITEMS_SEPARATOR = ", "
        private const val SPACE_SEPARATOR = " "
        private const val OLD = "old"

        private fun getSingleNumber(regex: Regex, string: String): Int? =
            regex.matchEntire(string)
                ?.groupValues
                ?.getOrNull(1)
                ?.toIntOrNull()

        private fun getMonkeyId(string: String): Int? =
            getSingleNumber(MONKEY_ID_REGEX, string)

        private fun getItems(string: String): List<BigInteger>? {
            val match = ITEMS_REGEX.matchEntire(string) ?: return null
            val itemsString = match.groupValues.getOrNull(1) ?: return null
            return itemsString.split(ITEMS_SEPARATOR).mapNotNull { it.toLongOrNull() }.map { BigInteger.valueOf(it) }
        }

        private fun getOperation(string: String): Expression? {
            val expressionString = OPERATION_REGEX
                .matchEntire(string)
                ?.groupValues
                ?.getOrNull(1)
                ?: return null
            val split = expressionString.split(SPACE_SEPARATOR)
            if (split.size != 3) {
                return null
            }
            val firstOperand = when {
                split[0] == OLD -> Operand.Old
                split[0].toIntOrNull() != null -> Operand.Number(split[0].toInt())
                else -> return null
            }
            val operator = when (split[1]) {
                "+" -> Operator.Plus
                "*" -> Operator.Multiply
                else -> return null
            }
            val secondOperand = when {
                split[2] == OLD -> Operand.Old
                split[2].toIntOrNull() != null -> Operand.Number(split[2].toInt())
                else -> return null
            }
            return Expression(firstOperand, secondOperand, operator)
        }

        private fun getTestNumber(string: String): Int? =
            getSingleNumber(TEST_REGEX, string)

        private fun getTrueMonkeyId(string: String): Int? =
            getSingleNumber(IF_TRUE_REGEX, string)

        private fun getFalseMonkeyId(string: String): Int? =
            getSingleNumber(IF_FALSE_REGEX, string)

        fun fromStrings(strings: List<String>): Monkey? {
            if (strings.size != 6) {
                return null
            }
            val id = getMonkeyId(strings[0]) ?: return null
            val itemsWorryLevels = getItems(strings[1])?.toMutableList() ?: return null
            val expression = getOperation(strings[2]) ?: return null
            val testNumber = getTestNumber(strings[3]) ?: return null
            val trueBranchMonkeyId = getTrueMonkeyId(strings[4]) ?: return null
            val falseBranchMonkeyId = getFalseMonkeyId(strings[5]) ?: return null
            return Monkey(id, itemsWorryLevels, expression, testNumber, trueBranchMonkeyId, falseBranchMonkeyId)
        }
    }
}

private fun updateMonkey(
    monkey: Monkey,
    monkeyMap: MonkeyMap,
    monkeyItemCounter: Counter
) {
    while (monkey.itemsWorryLevels.isNotEmpty()) {
        val itemWorryLevel = monkey.itemsWorryLevels.removeFirst()
        monkeyItemCounter[monkey.id] = (monkeyItemCounter[monkey.id] ?: 0) + 1
        //val updatedItemWorryLevel = monkey.expression.evaluated(itemWorryLevel) / 3
        val updatedItemWorryLevel = monkey.expression.evaluated(itemWorryLevel)
        if (updatedItemWorryLevel % BigInteger.valueOf(monkey.testNumber.toLong()) == BigInteger.ZERO) {
            monkeyMap[monkey.trueBranchMonkeyId]?.itemsWorryLevels?.add(updatedItemWorryLevel)
        } else {
            monkeyMap[monkey.falseBranchMonkeyId]?.itemsWorryLevels?.add(updatedItemWorryLevel)
        }
    }
}

private fun updateMonkeys(
    monkeys: List<Monkey>,
    monkeyMap: MonkeyMap,
    monkeyItemCounter: Counter
) {
    for (monkey in monkeys) {
        updateMonkey(monkey, monkeyMap, monkeyItemCounter)
    }
}

private fun getActiveScore(
    monkeys: List<Monkey>,
    numOfRounds: Int
): Long {
    val monkeyMap = monkeys.associateBy { it.id }
    val monkeyItemCounter = Counter()
    repeat(numOfRounds) {
        updateMonkeys(monkeys, monkeyMap, monkeyItemCounter)
    }
    val activitiesScore = monkeyItemCounter.values.sortedDescending()
    return activitiesScore[0] * activitiesScore[1]
}

private fun getOptimizedActiveScore(
    monkeys: List<Monkey>,
    numOfRounds: Int
): Long {
    val monkeyMap = monkeys.associateBy { it.id }
    val monkeyItemCounter = Counter()
    val monkeysStatesMap: MutableMap<MonkeysState, Int> = mutableMapOf()
    val monkeyItemCounters: MutableList<Counter> = mutableListOf()
    var round = 0
    var shouldContinue = true
    monkeysStatesMap[monkeyMap.toMonkeysState()] = 0
    monkeyItemCounters.add(monkeyItemCounter)
    while (shouldContinue) {
        round++
        updateMonkeys(monkeys, monkeyMap, monkeyItemCounter)
        val monkeyState= monkeyMap.toMonkeysState()
        if (monkeyState in monkeysStatesMap) {
            shouldContinue = false
        } else {
            monkeysStatesMap[monkeyState] = round
            monkeyItemCounters.add(monkeyItemCounter)
            if (round >= numOfRounds) {
                shouldContinue = false
            }
        }
    }
    return if (round == numOfRounds) {
        val activitiesScore = monkeyItemCounter.values.sortedDescending()
        activitiesScore[0] * activitiesScore[1]
    } else {
        val monkeyState= monkeyMap.toMonkeysState()
        val index = monkeysStatesMap[monkeyState] ?: 0
        val differenceCounter = monkeyItemCounters.last().diff(monkeyItemCounters[index])
        val scaler = (numOfRounds - index) / (round - index)
        val mod = (numOfRounds - index) % (round - index)
        val differenceLast = monkeyItemCounters[index + mod].diff(monkeyItemCounters[index])
        val multiplied = differenceCounter.multiply(scaler)
        var finalCounter = if (index > 0) monkeyItemCounters[index - 1] else Counter()
        finalCounter = finalCounter.add(multiplied)
        finalCounter = finalCounter.add(monkeyItemCounters[mod])
        val activitiesScore = finalCounter.values.sortedDescending()
        activitiesScore[0] * activitiesScore[1]
    }
}

private fun getMonkeys(path: String): List<Monkey> {
    val strings = getLines(path)
    val indices = listOf(-1) +
            strings.withIndex().filter { (_, string) -> string.isEmpty() }.map { (index, _) -> index } +
            listOf(strings.size)
    return indices.zipWithNext { start, end ->
        val subStrings = strings.subList(start + 1, end)
        Monkey.fromStrings(subStrings)
    }.filterNotNull()
}

private fun solvePartOne() {
    val monkeys = getMonkeys("day_11.txt")
    val answer = getActiveScore(monkeys, NUM_OF_ROUNDS)
    println(answer)
}

fun main() {
    solvePartOne()
}