package dayten

import getLines
import kotlin.math.abs

private const val OFFSET = 20
private const val PERIOD = 40

private typealias Matrix = List<List<Boolean>>
private typealias MutableMatrix = MutableList<MutableList<Boolean>>

private val Matrix.rows: Int
    get() = this.size

private val Matrix.cols: Int
    get() = this.first().size

private fun Matrix(rows: Int, cols: Int): Matrix =
    List(rows) { List(cols) { false } }

private fun Matrix.toMutableMatrix(): MutableMatrix =
    MutableList(this.rows) { rowIndex ->
        this[rowIndex].toMutableList()
    }

private fun MutableMatrix.toMatrix(): Matrix =
    this.map { row -> row.toList() }

private fun Matrix.getRepresentation(): String {
    var string = ""
    for (row in 0 until this.rows) {
        for (col in 0 until this.cols) {
            string += if (this[row][col]) "#" else "."
        }
        string += "\n"
    }
    return string
}

private sealed class Operation {
    object NoOperation : Operation() {
        private const val STRING = "noop"

        fun fromString(string: String): NoOperation? =
            if (string == STRING) NoOperation else null
    }
    data class AddOperation(val value: Int) : Operation() {
        companion object {
            private const val STRING = "addx"

            fun fromString(string: String): AddOperation? {
                val splits = string.split(" ")
                if (splits.size != 2) {
                    return null
                }
                if (splits.first() != STRING) {
                    return null
                }
                val value = splits[1].toIntOrNull() ?: return null
                return AddOperation(value)
            }
        }
    }
}

private data class ProcessorState(
    val value: Int = 1,
    val cycle: Int = 1
)

private data class SignalState(
    val processorState: ProcessorState = ProcessorState(),
    val values: List<Int> = emptyList()
) {
    companion object {
        private const val NUM_OF_VALUES = 6
    }

    fun isDesiredState(): Boolean = this.values.size >= NUM_OF_VALUES

    fun getSumSignalsStrengths(): Int =
        this.values
            .mapIndexed { index, value ->
                val cycle = index * PERIOD + OFFSET
                value * cycle
            }
            .sum()
}

private data class ScreenState(
    val processorState: ProcessorState = ProcessorState(value = 1, cycle = 0),
    val screen: Matrix = Matrix(ROWS, COLS)
) {
    companion object {
        private const val ROWS = 6
        private const val COLS = 40
    }
}

private fun String.toOperation(): Operation? =
    Operation.NoOperation.fromString(this) ?: Operation.AddOperation.fromString(this)

private fun isImportantCycle(cycle: Int): Boolean =
    cycle % PERIOD == OFFSET

private fun updateScreen(screen: MutableMatrix, cycle: Int, value: Int) {
    val row = cycle / screen.cols
    val col = cycle % screen.cols
    if (abs(value - col) <= 1) {
        screen[row][col] = true
    }
}

private fun getUpdatedSignalState(signalState: SignalState, operation: Operation): SignalState {
    val (processorState, values) = signalState
    return when (operation) {
        Operation.NoOperation -> {
            val updatedValues = values.toMutableList()
            if (isImportantCycle(processorState.cycle)) {
                updatedValues.add(processorState.value)
            }
            SignalState(
                processorState = processorState.copy(cycle = processorState.cycle + 1),
                values = updatedValues
            )
        }
        is Operation.AddOperation -> {
            val updatedValues = values.toMutableList()
            if (isImportantCycle(processorState.cycle)) {
                updatedValues.add(processorState.value)
            }
            if (isImportantCycle(processorState.cycle + 1)) {
                updatedValues.add(processorState.value)
            }
            SignalState(
                processorState = ProcessorState(
                    value = processorState.value + operation.value,
                    cycle = processorState.cycle + 2
                ),
                values = updatedValues
            )
        }
    }
}

private fun getUpdatedScreenState(screenState: ScreenState, operation: Operation): ScreenState {
    val (processorState, screen) = screenState
    return when (operation) {
        Operation.NoOperation -> {
            val mutableScreen = screen.toMutableMatrix()
            updateScreen(mutableScreen, processorState.cycle, processorState.value)
            ScreenState(
                processorState = processorState.copy(cycle = processorState.cycle + 1),
                screen = mutableScreen.toMatrix()
            )
        }
        is Operation.AddOperation -> {
            val mutableScreen = screen.toMutableMatrix()
            updateScreen(mutableScreen, processorState.cycle, processorState.value)
            updateScreen(mutableScreen, processorState.cycle + 1, processorState.value)
            ScreenState(
                processorState = ProcessorState(
                    value = processorState.value + operation.value,
                    cycle = processorState.cycle + 2
                ),
                screen = mutableScreen
            )
        }
    }
}

private fun getFinalScreenState(operations: List<Operation>): ScreenState =
    operations.fold(ScreenState()) { screenState, operation ->
        getUpdatedScreenState(screenState, operation)
    }

private fun getDesiredState(operations: List<Operation>): SignalState {
    var currentSignalState = SignalState()
    var currentOperations = operations
    while (currentOperations.isNotEmpty() && !currentSignalState.isDesiredState()) {
        val operation = currentOperations.first()
        currentOperations = currentOperations.drop(1)
        currentSignalState = getUpdatedSignalState(currentSignalState, operation)
    }
    return currentSignalState
}

private fun getDesiredStateSignalStrength(operations: List<Operation>): Int =
    getDesiredState(operations).getSumSignalsStrengths()

private fun getOperations(path: String): List<Operation> =
    getLines(path).mapNotNull { it.toOperation() }

private fun solvePartOne() {
    val operations = getOperations("day_10.txt")
    println(getDesiredStateSignalStrength(operations))
}

private fun solvePartTwo() {
    val operations = getOperations("day_10.txt")
    val screenState = getFinalScreenState(operations)
    println(screenState.screen.getRepresentation())
}


fun main() {
    //solvePartOne()
    solvePartTwo()
}