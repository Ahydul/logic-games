package com.example.tfg.common.utils

data class Quadruple<T>(val up: T, val down: T, val left: T, val right: T) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Quadruple<*>

        if (up != other.up) return false
        if (down != other.down) return false
        if (left != other.left) return false
        return right == other.right
    }

    override fun hashCode(): Int {
        var result = up?.hashCode() ?: 0
        result = 31 * result + (down?.hashCode() ?: 0)
        result = 31 * result + (left?.hashCode() ?: 0)
        result = 31 * result + (right?.hashCode() ?: 0)
        return result
    }
}