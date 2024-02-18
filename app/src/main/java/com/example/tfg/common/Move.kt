package com.example.tfg.common


class Move private constructor(
    val action: Action,
    val coordinates: List<Coordinate>,
    val value: Int,
) {

    companion object {
        fun eraseAction(coordinates: List<Coordinate>) =
            Move(
                action = Action.ERASE,
                coordinates = coordinates,
                value = -1
            )
        fun noteAction(coordinates: List<Coordinate>, value: Int, ordered: Boolean) =
            Move(
                action = if (ordered) Action.ORDERED_NOTE else Action.UNORDERED_NOTE,
                coordinates = coordinates,
                value = value
            )
        fun writeAction(coordinates: List<Coordinate>, value: Int) =
            Move(
                action = Action.WRITE,
                coordinates = coordinates,
                value = value
            )
        fun paintAction(coordinates: List<Coordinate>, value: Int) =
            Move(
                action = Action.PAINT,
                coordinates = coordinates,
                value = value
            )
        fun removePaintAction(coordinates: List<Coordinate>) =
            Move(
                action = Action.REMOVE_PAINT,
                coordinates = coordinates,
                value = -1
            )

    }
}