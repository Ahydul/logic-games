package com.example.tfg.components.activeGame

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.tfg.R
import com.example.tfg.addDebugBorder
import com.example.tfg.common.Board
import com.example.tfg.components.common.HorizontalGrid
import com.example.tfg.games.Games
import com.example.tfg.games.hakyuu.HakyuuValue


@Composable
fun Action(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
    iconColor: Color = colorResource(id = R.color.primary_color),
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.then(addDebugBorder)
    ) {
        Icon(
            tint = iconColor,
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun TopActionRow(
    isNote: Boolean,
    isPaint: Boolean,
    setNote: ()->Unit,
    setPaint: ()->Unit,
    modifier: Modifier
){
    val actionModifier = Modifier
        .padding(15.dp) //This controls the size

    HorizontalGrid(
        rows = 1,
        modifier = modifier
    ) {
        Action( //Paint
            onClick = setPaint,
            imageVector = ImageVector.vectorResource(id = R.drawable.broad_paint_brush),
            contentDescription = stringResource(id = R.string.brush_action),
            modifier = actionModifier
        )
        Action( //Erase
            onClick = { /*TODO*/ },
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_eraser),
            contentDescription = stringResource(id = R.string.erase_action),
            modifier = actionModifier
        )
        Action( //Note
            onClick = setNote,
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_edit_24),
            contentDescription = stringResource(id = R.string.edit_action),
            modifier = actionModifier
        )
        Action( //Undo
            onClick = { /*TODO*/ },
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_undo_24),
            contentDescription = stringResource(id = R.string.undo_action),
            modifier = actionModifier
        )
        Action( //Redo
            onClick = { /*TODO*/ },
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_redo_24),
            contentDescription = stringResource(id = R.string.redo_action),
            modifier = actionModifier
        )
    }
}


@Composable
fun BottomActionRow(
    gameType: Games,
    board: MutableState<Board>,
    selectedTiles: SnapshotStateList<Int>,
    isNote: Boolean,
    isPaint: Boolean,
    modifier: Modifier
) {
    Log.d("TAG", "BottomActionRow $currentRecomposeScope")

    val colorIds = integerArrayResource(id = R.array.cell_background_color_ints)
    val backgroundColors = colorIds.map { Color(it) }

    val numRows = when(gameType) {
        Games.HAKYUU -> 2
    }

    HorizontalGrid(
        rows = numRows,
        modifier = modifier
    ) {
        val shape = RoundedCornerShape(15.dp)
        val modifierValueButtons = Modifier
            .padding(8.dp)
            .clip(shape)
            .background(
                color = colorResource(id = R.color.cell_background),
                shape = shape
            )

        val noteAction = { value: Int ->
            for (tile in selectedTiles){
                val cell = board.value.getCell(index = tile)
                if(!cell.readOnly) board.value.setCellNote(index = tile, note = value, noteIndex = value)
            }
        }
        val paintAction = { value: Int ->
            for (tile in selectedTiles)
                board.value.setCellColor(index = tile, color = backgroundColors[value])
        }
        val writeAction = { value: Int ->
            if(selectedTiles.size == 1) {
                val cell = board.value.getCell(index = selectedTiles[0])
                if(!cell.readOnly){
                    board.value.setCellValue(index = selectedTiles[0], value = value)
                    selectedTiles.removeAll{ true }
                }
            }
        }

        val action = if(isNote) noteAction else writeAction

        if(isPaint){
            backgroundColors.forEachIndexed { index, color ->
                Action(
                    onClick = { paintAction(index) },
                    imageVector =  ImageVector.vectorResource(id = R.drawable.baseline_color_lens_24),
                    iconColor = color,
                    contentDescription = "Color $index",
                    modifier = modifierValueButtons
                )
            }
        }
        else when(gameType) {
            Games.HAKYUU -> {
                HakyuuValue.entries.forEach {
                    val iconColor = if(isNote) colorResource(id = R.color.note_color)
                        else colorResource(id = R.color.primary_color)

                    Action(
                        onClick = { action(it.value) },
                        imageVector = ImageVector.vectorResource(id = it.icon),
                        iconColor = iconColor,
                        contentDescription = null,
                        modifier = modifierValueButtons
                    )
                }
            }
        }
    }
}
