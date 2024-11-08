package com.example.tfg.games.common

import com.example.tfg.games.hakyuu.Hakyuu
import com.example.tfg.games.kendoku.Kendoku
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class StrategiesUnitClass {

    private fun foldResult(line: Array<MutableList<Int>>): String {
        return line.joinToString(separator = ";") { it.joinToString(separator = "") }
    }

    private fun parsePossibleValues(possibleValues: String): Array<MutableList<Int>> {
        return possibleValues.split(";").map { it.split("").drop(1).dropLast(1).map { i -> i.toInt() }.toMutableList() }.toTypedArray()
    }

    private fun executeForEachGame(id: Long, numColumns: Int, numRows: Int, seed: Long, execute: (CommonStrategies) -> Unit) {
        println("Executing test for Kendoku\n")
        execute(CommonStrategies(Kendoku(id, numColumns, numRows, seed)))
        println("Executing test for Hakyuu\n")
        execute(CommonStrategies(Hakyuu(id, numColumns, numRows, seed)))
    }

    @ParameterizedTest
    @CsvSource(
        "123;23;234;24;235;56;74, 3, 1;23;234;24;235;6;7",
        "1;23;234;24;235;6;7, 1, 1;23;234;24;5;6;7",
        "1345;2345;345;56;345;345;3457, 4, 1;2;345;6;345;345;7",
        "1;2;345;6;345;345;7, 0, 1;2;345;6;345;345;7",
    )
    fun testCleanHiddenSingles(input: String, expectedNumSingles: Int, expectedResult: String) {
        executeForEachGame(0, 7, 7,0L) { strategies ->
            val line = parsePossibleValues(input)

            val numberSingles = strategies.cleanHiddenSingles(line, line.size)
            val foldResult = foldResult(line)

            println(foldResult)
            assert(foldResult == expectedResult)
            assert(numberSingles == expectedNumSingles)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "12345;23;23;23456;56;56, 2, 14;23;23;4;56;56",
        "12;12;1234567;1234567;1234567;1234567;1234567, 1, 12;12;34567;34567;34567;34567;34567",
        "12;12;34;34;56;56;1234567, 3, 12;12;34;34;56;56;7",
    )
    fun testCleanNakedPairs(input: String, expectedNumPairs: Int, expectedResult: String) {
        executeForEachGame(0, 6, 6,0L) { strategies ->
            val line = parsePossibleValues(input)

            val numPairs = strategies.cleanNakedPairsInLine(line)
            val foldResult = foldResult(line)

            println(foldResult)
            assert(foldResult == expectedResult)
            assert(numPairs == expectedNumPairs)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "12345;2345;345;345;345;56, 1, 12;2;345;345;345;6",
        "123;123;12;1234567;1234567;1234567;1234567, 1, 123;123;12;4567;4567;4567;4567",
        "123;123;123;456;456;456;1234567, 2, 123;123;123;456;456;456;7",
        "123;23;24;43;1234567;1234567;1234567, 1, 1;23;24;43;1567;1567;1567",
    )
    fun testCleanNakedTriples(input: String, expectedNumTriples: Int, expectedResult: String) {
        executeForEachGame(0, 7, 7,0L) { strategies ->
            val line = parsePossibleValues(input)

            val numTriples = strategies.cleanNakedTriplesInLine(line)
            val foldResult = foldResult(line)

            println(foldResult)
            assert(foldResult == expectedResult)
            assert(numTriples == expectedNumTriples)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "7, 123;23;234;24;235;56;74, 3;0;0, 1;23;234;24;235;6;7",
        "7, 1;23;234;24;235;6;7, 1;0;0, 1;23;234;24;5;6;7",
        "7, 1345;2345;345;56;345;345;3457, 4;0;0, 1;2;345;6;345;345;7",
        "7, 1;2;345;6;345;345;7, 0;0;0, 1;2;345;6;345;345;7",
        "7, 1234567;12345;1234567;12345;12345;12345;12345, 0;1;0, 67;12345;67;12345;12345;12345;12345",
        "7, 34567;345;34567;345;345;12345;12345, 0;2;0, 67;345;67;345;345;12;12",
        "7, 12367;1235;12367;123;123;123;1234, 2;1;0, 67;5;67;123;123;123;4",
        "7, 1234567;1234;1234567;1234;1234567;1234;1234, 0;0;1, 567;1234;567;1234;567;1234;1234",
        "7, 123456;1234;1234567;1234;1234567;1234;1234, 0;0;1, 56;1234;567;1234;567;1234;1234",
        "7, 123456;1234;123457;1234;123467;1234;1234, 0;0;1, 56;1234;57;1234;67;1234;1234",
        "7, 3456;34;3457;34;3467;1234;1234, 0;1;1, 56;34;57;34;67;12;12",
        "4, 124;124;124;, 0;0;0, 124;124;124;",
    )
    fun testCleanHiddenSinglesPairsTriples(size: Int, input: String, expectedSPT: String, expectedResult: String) {
        executeForEachGame(0, size, size,0L) { strategies ->
            val line = parsePossibleValues(input)

            val numberSPT = strategies.cleanHiddenSinglesPairsTriplesInline(line, line.size)
            val foldResult = foldResult(line)

            println(foldResult)
            println(numberSPT.toList())
            assert(foldResult == expectedResult)
            assert(numberSPT.toList() == expectedSPT.split(";").map { it.toInt() })
        }
    }

    @ParameterizedTest
    @CsvSource(
        "5, 12;;;;12;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;23;;;;23, 12;;;;12;1345;12345;12345;12345;1345;1345;12345;12345;12345;1345;1345;12345;12345;12345;1345;23;;;;23, 1",
        "5, 12;1345;1345;1345;12;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;23;1345;1345;1345;23, 12;1345;1345;1345;12;1345;12345;12345;12345;1345;1345;12345;12345;12345;1345;1345;12345;12345;12345;1345;23;1345;1345;1345;23, 1",
        "5, 12;;;;12;14;1235;1235;1235;14;12345;12345;12345;12345;12345;45;1235;1235;1235;45;23;;;;23, 12;;;;12;14;1235;1235;1235;14;135;124;124;124;135;45;123;123;123;45;23;;;;, 4",
        "3, 123;123;123;123;123;123;123;123;123, 123;123;123;123;123;123;123;123;123, 0",
    )
    fun testCleanXWing(size: Int, possibleValuesInput: String, expectedResult: String, expectedNumChanges: Int) {
        executeForEachGame(0, size, size,0L) { strategies ->
            val possibleValues = parsePossibleValues(possibleValuesInput)

            val numChanges = strategies.cleanXWing(possibleValues)
            val foldResult = foldResult(possibleValues)

            println(foldResult)
            assert(foldResult.contains(expectedResult))
            assert(numChanges == expectedNumChanges)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "4, 12;23;134;134;1234;13;1234;1234;1234;124;1234;1234;1234;124;1234;1234, 12;23;134;134;234;13;1234;1234;1234;124;1234;1234;1234;124;1234;1234",
    )
    fun testCleanYWing(size: Int, possibleValuesInput: String, expectedResult: String) {
        executeForEachGame(0, size, size,0L) { strategies ->
            val possibleValues = parsePossibleValues(possibleValuesInput)

            strategies.cleanYWing(possibleValues, size)
            val foldResult = foldResult(possibleValues)

            println(foldResult)
            assert(foldResult.contains(expectedResult))
        }
    }


    @ParameterizedTest
    @CsvSource(
        "4, 1234;123;123;1234;1234;123;1234;123;1234;1234;123;123;123;1234;1234;123, 123;123;123;1234;1234;123;1234;123;1234;1234;123;123;123;1234;1234;123, 1",
        "5, 1234;12345;1234;1234;12345;1234;12345;1234;12345;1234;1234;12345;12345;1234;1234;1234;1234;12345;12345;1234;12345;12345;1234;1234;12345, 1234;1234;1234;1234;12345;1234;12345;1234;12345;1234;1234;12345;12345;1234;1234;1234;1234;12345;12345;1234;12345;1234;1234;1234;12345, 1",
        "5, 1234;1345;1234;134;1345;1234;12345;134;1345;134;134;12345;12345;134;134;1234;134;1345;12345;1234;12345;1345;134;1234;12345, 1234;134;1234;134;1345;1234;12345;134;1345;134;134;12345;12345;134;134;134;134;1345;12345;1234;1345;134;134;1234;12345, 2",
    )
    fun testCleanColoring(size: Int, possibleValuesInput: String, expectedResult: String, expectedNumChanges: Int) {
        executeForEachGame(0, size, size,0L) { strategies ->
            val possibleValues = parsePossibleValues(possibleValuesInput)

            val numChanges = strategies.cleanColoring(possibleValues, size)
            val foldResult = foldResult(possibleValues)

            println(foldResult)
            assert(foldResult == expectedResult)
            assert(numChanges == expectedNumChanges)
        }
    }
}