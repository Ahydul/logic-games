package com.example.tfg.common.utils

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan
import kotlin.random.Random

abstract class Curves {

    companion object {
        fun logisticRandom(maxValue: Int, random: Random, f: (Double) -> Double): Int {
            val x = random.nextDouble(1.0) // generates a random number between 0 (inclusive) and 1 (exclusive)

            val v = f(x)

            return (v * maxValue).toInt()

        }

        fun bezier(value: Double): Double {
            val p0 = Point(0.0, 0.0)
            val p1 = Point(0.3, 0.5)
            val p2 = Point(0.7, 0.5)
            val p3 = Point(1.0, 1.0)

            return bezierCurve(value, p0, p1, p2, p3)
        }

        fun bezier1(value: Double): Double {

            val p0 = Point(0.0, 0.0)
            val p1 = Point(0.7, 0.05)
            val p2 = Point(0.5, 0.8)
            val p3 = Point(1.0, 1.0)

            return bezierCurve(value, p0, p1, p2, p3)
        }

        fun moreProgressively(value: Double): Double {
            return sin(PI * value)
        }

        fun easeInSine(value: Double): Double {
            return 1 - cos((value * PI) / 2)
        }

        fun easeOutSine(value: Double): Double {
            return sin((value * PI) / 2)
        }

        fun easeInOutSine(value: Double): Double {
            return -(cos(value * PI) - 1) / 2
        }

        fun easierInOutSine(value: Double): Double {
            require(value < 13)
            return 1 / (1 + exp(-7 * (value - 0.6))) //1 / (1 + exp(-6.5 * (value - 0.5)))

        }

        fun lessMoreLess(value: Double): Double {
            require(value < 20)
            val a = 1.3
            val b = tan(a/2)
            return ((tan(value - 0.5) * a) + b) / (2*b)
        }

        fun lowerValues(value: Double, upperLimitFactor: Double = 0.6): Double {
            val p0 = Point(0.0, 0.0)
            val p1 = Point(1.0, 0.0)
            val p2 = Point(1.0, 0.0)
            val p3 = Point(1.0, upperLimitFactor)

            return bezierCurve(value, p0, p1, p2, p3)
        }

        data class Point(val x: Double, val y: Double)

        private fun bezierCurve(x: Double, vararg controlPoints: Point): Double {
            val n = controlPoints.size - 1
            var y = 0.0

            for (i in 0..n) {
                val coefficient = (factorial(n) / (factorial(i) * factorial(n - i))) * (1 - x).pow(n - i) * x.pow(i)
                y += coefficient * controlPoints[i].y
            }

            return y
        }

        private fun factorial(n: Int): Int {
            var result = 1
            for (i in 1..n) {
                result *= i
            }
            return result
        }

    }
}