package daythirteen

import getLines

private sealed class Token {
    object LeftBracket : Token() {
        const val CHAR = '['
    }
    object RightBracket : Token() {
        const val CHAR = ']'
    }
    data class Number(val value: Int) : Token()
    object Separator : Token() {
        const val CHAR = ','
    }
}

private sealed class Packet : Comparable<Packet> {
    data class Number(val value: Int) : Packet()
    data class PacketList(val list: List<Packet>) : Packet()

    override fun compareTo(other: Packet): Int {
        return when (getCompareResult(this, other)) {
            CompareResult.Less -> -1
            CompareResult.Equal -> 0
            CompareResult.Greater -> 1
        }
    }
}

private enum class CompareResult {
    Less,
    Greater,
    Equal
}

private fun getTokenizedString(string: String): List<Token> {
    val tokens: MutableList<Token> = mutableListOf()
    var currentString = string
    while (currentString.isNotEmpty()) {
        when (currentString.first()) {
            Token.LeftBracket.CHAR -> {
                tokens.add(Token.LeftBracket)
                currentString = currentString.drop(1)
            }
            Token.RightBracket.CHAR -> {
                tokens.add(Token.RightBracket)
                currentString = currentString.drop(1)
            }
            Token.Separator.CHAR -> {
                tokens.add(Token.Separator)
                currentString = currentString.drop(1)
            }
            in '0'..'9' -> {
                var index = 0
                while (index < currentString.length && currentString[index].isDigit()) {
                    index++
                }
                val value = currentString.take(index).toInt()
                tokens.add(Token.Number(value))
                currentString = currentString.drop(index)
            }
        }
    }
    return tokens
}

private fun getParsedPacketElement(tokens: List<Token>): Pair<Packet, List<Token>> =
    when (val token = tokens.firstOrNull()) {
        is Token.Number -> Packet.Number(token.value) to tokens.drop(2)
        Token.LeftBracket -> {
            var index = 0
            var depth = 0
            while (index < tokens.size && (depth != 0 || index == 0)) {
                when (tokens[index]) {
                    Token.LeftBracket -> depth++
                    Token.RightBracket -> depth--
                    else -> {}
                }
                index++
            }
            val packetListTokens = tokens.take(index)
            val remainingTokens = if (index < tokens.size && tokens[index] == Token.Separator) {
                tokens.drop(index + 1)
            } else {
                tokens.drop(index)
            }
            val packetList = getParsedPacket(packetListTokens)
            packetList to remainingTokens
        }
        else -> error("Empty token string or invalid token")
    }

private fun getParsedPacket(tokens: List<Token>): Packet {
    var currentTokens = tokens.drop(1).dropLast(1)
    val packets: MutableList<Packet> = mutableListOf()
    while (currentTokens.isNotEmpty()) {
        val (packet, remainingTokens) = getParsedPacketElement(currentTokens)
        packets.add(packet)
        currentTokens = remainingTokens
    }
    return Packet.PacketList(packets)
}

private fun String.toPacket(): Packet {
    val tokenized = getTokenizedString(this)
    return getParsedPacket(tokenized)
}

private fun getCompareResult(first: Int, second: Int): CompareResult =
    when {
        first < second -> CompareResult.Less
        first > second -> CompareResult.Greater
        else -> CompareResult.Equal
    }

private fun getCompareResult(first: Packet, second: Packet): CompareResult =
    when (first) {
        is Packet.Number -> when (second) {
            is Packet.Number -> getCompareResult(first.value, second.value)
            is Packet.PacketList -> {
                val firstList = Packet.PacketList(list = listOf(Packet.Number(first.value)))
                getCompareResult(firstList, second)
            }
        }
        is Packet.PacketList -> when (second) {
            is Packet.Number -> {
                val secondList = Packet.PacketList(list = listOf(Packet.Number(second.value)))
                getCompareResult(first, secondList)
            }
            is Packet.PacketList -> {
                when {
                    first.list.isEmpty() && second.list.isEmpty() -> CompareResult.Equal
                    first.list.isEmpty() -> CompareResult.Less
                    second.list.isEmpty() -> CompareResult.Greater
                    else -> {
                        val firstElement = first.list.first()
                        val secondElement = second.list.first()
                        val compareResult = getCompareResult(firstElement, secondElement)
                        if (compareResult == CompareResult.Equal) {
                            val remainingFirst = Packet.PacketList(list = first.list.drop(1))
                            val remainingSecond = Packet.PacketList(list = second.list.drop(1))
                            getCompareResult(remainingFirst, remainingSecond)
                        } else {
                            compareResult
                        }
                    }
                }
            }
        }
    }

private fun getPacketPairs(path: String): List<Pair<Packet, Packet>> {
    val strings = getLines(path)
    val numOfPacketPairs = (strings.size + 1) / 3
    return (0 until numOfPacketPairs)
        .map { index ->
            val firstString = strings[3 * index]
            val secondString = strings[3 * index + 1]
            val firstPacket = firstString.toPacket()
            val secondPacket = secondString.toPacket()
            firstPacket to secondPacket
        }
}

private fun getOrderedPairsIndexSum(packetPairs: List<Pair<Packet, Packet>>): Int =
    packetPairs.withIndex()
        .filter { (_, packetPair) ->
            val (firstPacket, secondPacket) = packetPair
            getCompareResult(firstPacket, secondPacket) == CompareResult.Less
        }
        .sumOf { (index, _) -> index + 1 }

private fun solvePartOne() {
    val packetPairs = getPacketPairs("day_13.txt")
    val answer = getOrderedPairsIndexSum(packetPairs)
    println(answer)
}

private fun solvePartTwo() {
    val packetPairs = getPacketPairs("day_13.txt")
    val firstDividerPacket = "[[2]]".toPacket()
    val secondDividerPacket = "[[6]]".toPacket()
    val packets = packetPairs.flatMap { (first, second) -> listOf(first, second) } +
            listOf(firstDividerPacket, secondDividerPacket)
    val sortedPackets = packets.sorted()
    val firstIndex = sortedPackets.indexOf(firstDividerPacket) + 1
    val secondIndex = sortedPackets.indexOf(secondDividerPacket) + 1
    val answer = firstIndex * secondIndex
    println(answer)
}

fun main() {
    //solvePartOne()
    solvePartTwo()
}