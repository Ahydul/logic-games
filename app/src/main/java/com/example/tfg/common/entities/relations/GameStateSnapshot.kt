package com.example.tfg.common.entities.relations

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.tfg.common.entities.GameState

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = GameState::class,
            parentColumns = ["gameStateId"],
            childColumns = ["gameStateId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class GameStateSnapshot(
    @PrimaryKey
    val gameStateId: Long,
    val snapshotFilePath: String
)