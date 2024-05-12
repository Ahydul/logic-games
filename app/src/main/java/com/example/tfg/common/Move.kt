package com.example.tfg.common

import android.os.Parcelable
import com.example.tfg.common.utils.Coordinate
import kotlinx.parcelize.Parcelize

@Parcelize
data class Move(
    val coordinates: List<Coordinate>,
    val previousCells: List<Cell>,
    val newCells: List<Cell>
) : Parcelable