package com.example.tfg.common.utils

import android.content.Context
import android.content.Intent
import com.example.tfg.ActiveGameView
import com.example.tfg.MainActivity

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

        fun startActiveGameActivity(context: Context, gameId: Long) {
            val intent = Intent(context, ActiveGameView::class.java)
            intent.putExtra("gameId", gameId)
            context.startActivity(intent)
        }

        fun startHomeActivity(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }


    }
}