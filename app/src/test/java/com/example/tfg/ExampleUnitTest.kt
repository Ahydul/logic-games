package com.example.tfg

import com.example.tfg.common.Board
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val board = Board.example()

        assertEquals(4, 2 + 2)
    }
}