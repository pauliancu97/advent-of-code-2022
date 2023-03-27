package daytwo

import getLines

enum class Choice(
    private val elfString: String,
    private val playerString: String,
    val score: Int
) {
    Rock("A", "X", 1),
    Paper("B", "Y", 2),
    Scrissor("C", "Z", 3);

    fun beats(choice: Choice): Boolean =
        when (this) {
            Rock -> choice == Scrissor
            Paper -> choice == Rock
            Scrissor -> choice == Paper
        }

    companion object {
        fun fromElf(string: String): Choice =
            Choice.values().first { it.elfString == string }

        fun fromPlayer(string: String): Choice =
            Choice.values().first { it.playerString == string }
    }
}

enum class Outcome(val score: Int, private val string: String) {
    Won(6, "Z"),
    Lost(0, "X"),
    Draw(3, "Y");

    companion object {
        fun fromString(string: String) =
            Outcome.values().first { it.string == string }
    }
}

data class Round(
    val elfChoice: Choice,
    val playerChoice: Choice
)

data class RoundWithOutcome(
    val elfChoice: Choice,
    val outcome: Outcome
)

private fun String.toRound(): Round {
    val (elfString, playerString) = this.split(" ")
    return Round(
        elfChoice = Choice.fromElf(elfString),
        playerChoice = Choice.fromPlayer(playerString)
    )
}

private fun String.toRoundWithOutcome():RoundWithOutcome {
    val (elfString, outcomeString) = this.split(" ")
    return RoundWithOutcome(
        elfChoice = Choice.fromElf(elfString),
        outcome = Outcome.fromString(outcomeString)
    )
}

private fun getRounds(path: String): List<Round> =
    getLines(path).map { it.toRound() }

private fun getRoundsWithOutcome(path: String): List<RoundWithOutcome> =
    getLines(path).map { it.toRoundWithOutcome() }

private fun getPlayerChoice(elfChoice: Choice, outcome: Outcome): Choice =
    when (outcome) {
        Outcome.Won -> Choice.values().first { it.beats(elfChoice) }
        Outcome.Draw -> elfChoice
        Outcome.Lost -> Choice.values().first { elfChoice.beats(it) }
    }

private fun getRoundOutcome(elfChoice: Choice, playerChoice: Choice): Outcome =
    when {
        playerChoice == elfChoice -> Outcome.Draw
        playerChoice.beats(elfChoice) -> Outcome.Won
        else -> Outcome.Lost
    }

private fun getRoundScore(round: Round): Int {
    val (elfChoice, playerChoice) = round
    val outcome = getRoundOutcome(elfChoice, playerChoice)
    return outcome.score + playerChoice.score
}

private fun getRoundWithOutcomeScore(roundWithOutcome: RoundWithOutcome): Int {
    val (elfChoice, outcome) = roundWithOutcome
    val playerChoice = getPlayerChoice(elfChoice, outcome)
    return outcome.score + playerChoice.score
}

private fun getRoundsTotalScore(rounds: List<Round>): Int =
    rounds.sumOf { getRoundScore(it) }

private fun getRoundsWithOutcomeTotalScore(roundsWithOutcome: List<RoundWithOutcome>): Int =
    roundsWithOutcome.sumOf { getRoundWithOutcomeScore(it) }

private fun solvePartOne() {
    val rounds = getRounds("day_2.txt")
    val result = getRoundsTotalScore(rounds)
    println(result)
}

private fun solvePartTwo() {
    val roundsWithOutcome = getRoundsWithOutcome("day_2.txt")
    val result = getRoundsWithOutcomeTotalScore(roundsWithOutcome)
    println(result)
}

fun main() {
    solvePartTwo()
}