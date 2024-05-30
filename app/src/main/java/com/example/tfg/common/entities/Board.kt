package com.example.tfg.common.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


/*
* Manages the indexes of the different cells
* */
@Entity
data class Board(
    @PrimaryKey(autoGenerate = true)
    val boardId: Long = 0,
    val numColumns: Int,
    val numRows: Int,
    val gameStateId: Long,
)