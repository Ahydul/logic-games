package com.example.tfg.common.utils

import android.content.Context
import android.content.Intent
import com.example.tfg.ActiveGameView
import com.example.tfg.MainActivity

abstract class Utils {
    companion object {
        fun replaceArray(thisArray: IntArray, with: IntArray) {
            var index = 0
            while (index < thisArray.size) {
                thisArray[index] = with[index]
                index++
            }
        }

        fun replaceArray(thisArray: Array<MutableList<Int>>, with: Array<MutableList<Int>>) {
            var index = 0
            while (index < thisArray.size) {
                thisArray[index] = with[index]
                index++
            }
        }

        fun startActiveGameActivity(context: Context, gameId: Long) {
            val intent = Intent(context, ActiveGameView::class.java)
            intent.putExtra("gameId", gameId)
            context.startActivity(intent)
        }

        fun startHomeActivity(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }

        fun median(arr: LongArray, size: Int): Number {
            return if (size % 2 == 0) {
                (arr[size / 2 - 1] + arr[size / 2]) / 2.0
            } else {
                arr[size / 2]
            }
        }

        fun median(arr: IntArray, size: Int): Number{
            return if (size % 2 == 0) {
                (arr[size / 2 - 1] + arr[size / 2]) / 2.0
            } else {
                arr[size / 2]
            }
        }

        //arr must be sorted
        fun mode(arr: IntArray): Number{
            var currentMax = 0
            var currentResult = 0
            var tmpMax = 0
            var tmpResult = 0

            for (i in arr) {
                if (tmpResult != i) {
                    if (currentMax < tmpMax) {
                        currentMax = tmpMax
                        currentResult = tmpResult
                    }
                    tmpMax = 1
                    tmpResult = i
                }else {
                    tmpMax++
                }
            }

            return currentResult
        }
    }
}