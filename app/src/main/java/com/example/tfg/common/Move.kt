package com.example.tfg.common


data class Move(
    val coordinates: List<Coordinate>,
    val previousCells: List<Cell>,
    val newCells: List<Cell>
)