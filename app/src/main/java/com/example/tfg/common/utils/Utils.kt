package com.example.tfg.common.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.tfg.ActiveGameView
import com.example.tfg.MainActivity
import com.example.tfg.common.entities.relations.GameStateSnapshot
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.format.DateTimeFormatter

val dateFormatter = DateTimeFormatter.ofPattern("dd MM yyyy HH:mm")

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

        //arr must be sorted
        fun percentile(arr: LongArray, percentile: Int = 50): Number {
            require(percentile in 1..99){ "Invalid percentile" }
            val size = arr.size
            val a = 100/percentile.toDouble()
            val index = (size / a).toInt()
            return if (size % a == 0.0) {
                (arr[index - 1] + arr[index]) / 2.0
            } else {
                arr[index]
            }
        }
        //arr must be sorted
        fun percentile(arr: IntArray, percentile: Int = 50): Number {
            require(percentile in 1..99){ "Invalid percentile" }
            val size = arr.size
            val a = 100/percentile.toDouble()
            val index = (size / a).toInt()
            return if (size % a == 0.0) {
                (arr[index - 1] + arr[index]) / 2.0
            } else {
                arr[index]
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

        fun filterIndices(arr: IntArray, indices: IntArray): IntArray {
            return arr.filterIndexed { index, _ -> indices.contains(index) }.toIntArray()
        }

        fun filterIndices(arr: LongArray, indices: IntArray): LongArray {
            return arr.filterIndexed { index, _ -> indices.contains(index) }.toLongArray()
        }

        fun filterIndices(ls: List<IntArray>, indices: IntArray): List<IntArray> {
            return ls.map { filterIndices(it, indices) }
        }

        fun saveBitmapToFile(
            filesDir: File,
            bitmap: Bitmap,
            fileName: String,
            directory: String
        ): String? {
            val directory = File(filesDir, "snapshots/$directory")
            if (!directory.exists()) directory.mkdirs()

            val file = File(directory, fileName)
            try {
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    return file.absolutePath
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        fun getBitmapFromFile(filePath: String?): Bitmap? {
            return BitmapFactory.decodeFile(filePath)
        }

        fun getBitmapFromFile(snapshot: GameStateSnapshot?): Bitmap? {
            return getBitmapFromFile(snapshot?.snapshotFilePath)
        }

        fun deleteFile(filePath: String?): Boolean {
            val file = filePath?.let { File(it) }
            return file?.delete() ?: false
        }
    }
}