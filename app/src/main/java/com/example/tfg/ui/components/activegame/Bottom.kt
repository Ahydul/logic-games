package com.example.tfg.ui.components.activegame

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
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
import com.example.tfg.games.Games
import com.example.tfg.games.hakyuu.HakyuuValue
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.HorizontalGrid


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
    viewModel: ActiveGameViewModel,
    modifier: Modifier
){
    val actionModifier = Modifier
        .padding(15.dp) //This controls the size

    HorizontalGrid(
        rows = 1,
        modifier = modifier
    ) {
        Action( //Paint
            onClick = { viewModel.setIsPaint() },
            imageVector = ImageVector.vectorResource(id = R.drawable.broad_paint_brush),
            contentDescription = stringResource(id = R.string.brush_action),
            modifier = actionModifier
        )
        Action( //Erase
            onClick = { viewModel.eraseAction() },
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_eraser),
            contentDescription = stringResource(id = R.string.erase_action),
            modifier = actionModifier
        )
        Action( //Note
            onClick = { viewModel.setIsNote() },
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
    viewModel: ActiveGameViewModel,
    gameType: Games,
    modifier: Modifier
) {
    Log.d("TAG", "BottomActionRow $currentRecomposeScope")

    val backgroundColorIds = integerArrayResource(id = R.array.cell_background_color_ints)
    val defaultCellBackground = backgroundColorIds[0]
    Log.d("ASD", "BottomActionRow $defaultCellBackground")

    HorizontalGrid(
        rows = 2,
        modifier = modifier
    ) {
        val shape = RoundedCornerShape(15.dp)
        val modifierValueButtons = Modifier
            .padding(8.dp)
            .clip(shape)
            .background(
                color = Color(defaultCellBackground),
                shape = shape
            )


        if(viewModel.isPaint()){
            backgroundColorIds.forEachIndexed { index, color ->
                Action(
                    onClick = { viewModel.paintAction(color = backgroundColorIds[index], defaultColor = defaultCellBackground)},
                    imageVector =  ImageVector.vectorResource(id = R.drawable.baseline_color_lens_24),
                    iconColor = Color(color),
                    contentDescription = "Color $index",
                    modifier = modifierValueButtons
                )
            }
        }
        else when(gameType) {
            Games.HAKYUU -> {
                HakyuuValue.entries.forEach {
                    val iconColor = if(viewModel.isNote()) colorResource(id = R.color.note_color)
                        else colorResource(id = R.color.primary_color)

                    Action(
                        onClick = { viewModel.action(it.value) },
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
