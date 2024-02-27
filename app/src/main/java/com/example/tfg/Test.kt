package com.example.tfg

import com.example.tfg.games.hakyuu.Hakyuu
import kotlin.random.Random

fun logisticRandom(maxValue: Int, random: Random, f: (Double) -> Double): Int {
    val x = random.nextDouble(1.0) // generates a random number between 0 (inclusive) and 1 (exclusive)
    val v = f(x)
    return (v * maxValue).toInt()
}

fun test(maxValue: Int, random: Random, f: (Double) -> Double) {
    val m = mutableMapOf<Int,Int>()

    for (v in (0..<maxValue)) {
        m[v] = 0
    }

    repeat(100) {
        val randomValue = logisticRandom(maxValue, random, f)
        if(m.contains(randomValue)){
            m[randomValue] = m[randomValue]!! + 1
        }
        else {//Crash
            m[20000] = m[20000]!! + 1
        }
    }
    println("$m")
}

fun main() {
    //val maxValue = 25
    //val random = Random(876458709876)

    //test(maxValue = maxValue, random = random, f = { Curves.lessMoreLess(it) } )
    val numColumns = 8
    val numRows = 8
    val x: Int = ((numRows+numColumns) * 1.5).toInt()

    repeat(1000000) {
        val seed = (Math.random()*100000000).toInt()
        val random = Random(seed)
        val regions = Hakyuu.create(
            numRows = numColumns,
            numColumns = numRows,
            minNumberOfRegions = x,
            random = random
        ).boardRegions
    }
    val pito = 2

}