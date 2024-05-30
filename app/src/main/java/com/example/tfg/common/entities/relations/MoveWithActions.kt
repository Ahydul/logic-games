package com.example.tfg.common.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.tfg.common.entities.Action
import com.example.tfg.common.entities.Move

data class MoveWithActions(
    @Embedded val move: Move,
    @Relation(
        parentColumn = "moveId",
        entityColumn = "moveId",
    )
    val actions: List<Action>
)
