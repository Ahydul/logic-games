package com.example.tfg.ui.components.activegame

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.CustomIconButton
import com.example.tfg.ui.components.common.HorizontalGrid

@Composable
fun BottomSection(
    viewModel: ActiveGameViewModel,
    modifier: Modifier = Modifier)
{
    Log.d("recomposition", "BOTTOM recomposition")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Actions
        TopActionRow(
            viewModel = viewModel,
            modifier = Modifier
                .weight(2f)
                .padding(2.dp)

        )
        BottomActionRow(
            viewModel = viewModel,
            modifier = Modifier
                .padding(4.dp)
                .weight(5f)

        )
        Spacer(modifier = Modifier.weight(2f))
    }
}


@Composable
fun BottomActionRow(
    viewModel: ActiveGameViewModel,
    modifier: Modifier
) {
    Log.d("recomposition", "BottomActionRow recomposition")

    val backgroundColors = integerArrayResource(id = R.array.cell_background_color_ints)
    val defaultCellBackground = colorResource(id = R.color.cell_background)

    HorizontalGrid(
        numRows = 2,
        verticalSpreadFactor = 0.5f,
        horizontalSpreadFactor = 0.7f,
        componentsScale = 0.85f,
        modifier = modifier
    ) {
        val shape = RoundedCornerShape(15.dp)
        val modifierValueButtons = Modifier
            .clip(shape)
            .background(
                color = defaultCellBackground,
                shape = shape
            )

        if(viewModel.isPaint()){
            backgroundColors.forEachIndexed { index, color ->
                CustomIconButton(                                                 //First color is remove the background
                    onClick = { viewModel.paintAction(colorInt = color)},
                    imageVector =  ImageVector.vectorResource(id = R.drawable.baseline_color_lens_24),
                    iconColor = Color(color),
                    contentDescription = "Color $index",
                    modifier = modifierValueButtons
                )
            }
        }
        else {
            for(v in 0..< viewModel.getMaxValue()) {
                val value = viewModel.getValue(v)
                val iconColor = if(viewModel.isNote()) colorResource(id = R.color.note_color)
                                else colorResource(id = R.color.primary_color)
                val icon = ImageVector.vectorResource(id = value.icon)

                CustomIconButton(
                    onClick = { viewModel.noteOrWriteAction(value.value) },
                    imageVector = icon,
                    iconColor = iconColor,
                    contentDescription = null,
                    modifier = modifierValueButtons
                )
            }
        }

    }
}


@Composable
fun TopActionRow(
    viewModel: ActiveGameViewModel,
    modifier: Modifier
){
    val actionModifier = Modifier
    HorizontalGrid(
        numRows = 1,
        horizontalSpreadFactor = 0.85f,
        componentsScale = 0.5f,
        modifier = modifier
    ) {
        /*
        //TODO: IMPLEMENT STATE BUTTONS. AND DO EXTENDED TESTS //Note
        CustomIconButton(
            onClick = { viewModel.newGameState() },
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_edit_24),
            contentDescription = stringResource(id = R.string.edit_action),
            modifier = actionModifier
        )
        CustomIconButton(
            onClick = { viewModel.setActualState(0) },
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_undo_24),
            contentDescription = stringResource(id = R.string.undo_action),
            modifier = actionModifier
        )
        CustomIconButton(
            onClick = { viewModel.setActualState(1) },
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_redo_24),
            contentDescription = stringResource(id = R.string.redo_action),
            modifier = actionModifier
        )
         */
        CustomIconButton( //Paint
            onClick = { viewModel.setIsPaint() },
            imageVector = ImageVector.vectorResource(id = R.drawable.broad_paint_brush),
            contentDescription = stringResource(id = R.string.brush_action),
            modifier = actionModifier
        )
        CustomIconButton( //Erase
            onClick = { viewModel.eraseAction() },
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_eraser),
            contentDescription = stringResource(id = R.string.erase_action),
            modifier = actionModifier
        )
        CustomIconButton( //Note
            onClick = { viewModel.setIsNote() },
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_edit_24),
            contentDescription = stringResource(id = R.string.edit_action),
            modifier = actionModifier
        )
        CustomIconButton( //Undo
            onClick = { viewModel.undoMove() },
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_undo_24),
            contentDescription = stringResource(id = R.string.undo_action),
            modifier = actionModifier
        )
        CustomIconButton( //Redo
            onClick = { viewModel.redoMove() },
            imageVector = ImageVector.vectorResource(id = R.drawable.outline_redo_24),
            contentDescription = stringResource(id = R.string.redo_action),
            modifier = actionModifier
        )
    }
}
