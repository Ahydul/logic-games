package com.example.tfg.common

import com.example.tfg.common.utils.Coordinate


data class Move(
    val coordinates: List<Coordinate>,
    val previousCells: List<Cell>,
    val newCells: List<Cell>
)