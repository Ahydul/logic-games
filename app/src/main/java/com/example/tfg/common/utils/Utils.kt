package com.example.tfg.common.utils

class Utils {
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

    }
}