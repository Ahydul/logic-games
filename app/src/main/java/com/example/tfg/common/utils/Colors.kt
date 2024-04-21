package com.example.tfg.common.utils

class Colors(val colors: MutableList<String> = mutableListOf()) {

    fun newColor(): String {
        var res = randomColor()
        while (colors.contains(res)) {
            res = randomColor()
        }
        colors.add(res)
        return res
    }

    private fun randomColor(): String {
        val letters = "0123456789ABCDEF"
        var color = "#"
        repeat(6) {
            color += letters.random()
        }
        return color
    }

}