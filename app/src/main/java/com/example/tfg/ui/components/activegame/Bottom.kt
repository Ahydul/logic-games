package com.example.tfg.ui.components.activegame

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tfg.R
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.CustomFilledIconButton
import com.example.tfg.ui.components.common.CustomIconButton
import com.example.tfg.ui.components.common.GridLayout

@Composable
fun BottomSection(
    viewModel: ActiveGameViewModel,
    onStateClick: () -> Unit,
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
            onStateClick = onStateClick,
            modifier = Modifier
                .weight(1f)
                .padding(2.dp)

        )
        BottomActionRow(
            viewModel = viewModel,
            modifier = Modifier
                .padding(4.dp)
                .weight(3f)

        )
        Spacer(modifier = Modifier.weight(1f))
    }
}


@Composable
fun BottomActionRow(
    viewModel: ActiveGameViewModel,
    modifier: Modifier
) {
    Log.d("recomposition", "BottomActionRow recomposition")

    val backgroundColors = integerArrayResource(id = R.array.cell_background_color_ints)
    val numValues = viewModel.getMaxValue()+1
    GridLayout(
        numRows = if (numValues > 10) 3 else if (numValues > 4) 2 else 1,
        verticalSpreadFactor = 0.3f,
        horizontalSpreadFactor = 0.7f,
        componentsScale = 0.85f,
        modifier = modifier
    ) {
        val shape = RoundedCornerShape(15.dp)
        val modifierValueButtons = Modifier
            .clip(shape)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = shape
            )

        if(viewModel.isPaint()){
            backgroundColors.forEachIndexed { index, color ->
                CustomIconButton(                                                 //First color is remove the background
                    onClick = { viewModel.paintAction(colorInt = color)},
                    painter =  painterResource(id = R.drawable.palette),
                    iconColor = Color(color),
                    contentDescription = "Color $index",
                    modifier = modifierValueButtons,
                    enabled = viewModel.buttonShouldBeEnabled()
                )
            }
        }
        else {
            for(v in 0..< numValues) {
                val value = viewModel.getValue(v)
                val iconColor = if(viewModel.isNote()) colorResource(id = R.color.note_color)
                                else MaterialTheme.colorScheme.onPrimary

                CustomIconButton(
                    onClick = { viewModel.noteOrWriteAction(value.value) },
                    painter =  painterResource(id = value.icon),
                    iconColor = iconColor,
                    contentDescription = null,
                    modifier = modifierValueButtons,
                    enabled = viewModel.buttonShouldBeEnabled()
                )
            }
        }

    }
}


@Composable
fun TopActionRow(
    viewModel: ActiveGameViewModel,
    onStateClick: () -> Unit,
    modifier: Modifier
){
    val actionModifier = Modifier
    GridLayout(
        numRows = 1,
        horizontalSpreadFactor = 0.85f,
        componentsScale = 0.5f,
        modifier = modifier
    ) {
        val iconColor = MaterialTheme.colorScheme.onPrimary
        val backgroundColor = MaterialTheme.colorScheme.background
        val selectedColor = colorResource(id = R.color.cell_value)
        CustomIconButton( //States
            onClick = onStateClick,
            iconColor = iconColor,
            painter =  painterResource(id = R.drawable.notebook),
            contentDescription = stringResource(id = R.string.change_gamestate),
            modifier = actionModifier,
            enabled = viewModel.buttonShouldBeEnabled()
        )
        CustomFilledIconButton( //Paint
            onClick = { viewModel.setIsPaint() },
            iconColor = if (viewModel.isPaint()) Color.Black else iconColor,
            color = if (viewModel.isPaint()) selectedColor else backgroundColor,
            painter =  painterResource(id = R.drawable.broad_paint_brush),
            contentDescription = stringResource(id = R.string.brush_action),
            modifier = actionModifier,
            enabled = viewModel.buttonShouldBeEnabled()
        )
        CustomIconButton( //Erase
            onClick = { viewModel.eraseAction() },
            iconColor = iconColor,
            painter =  painterResource(id = R.drawable.outline_eraser),
            contentDescription = stringResource(id = R.string.erase_action),
            modifier = actionModifier,
            enabled = viewModel.buttonShouldBeEnabled()
        )
        CustomFilledIconButton( //Note
            onClick = { viewModel.setIsNote() },
            iconColor = if (viewModel.isNote()) Color.Black else iconColor,
            color = if (viewModel.isNote()) selectedColor else backgroundColor,
            painter =  painterResource(id = R.drawable.outline_edit_24),
            contentDescription = stringResource(id = R.string.edit_action),
            modifier = actionModifier,
            enabled = viewModel.buttonShouldBeEnabled()
        )
        CustomIconButton( //Undo
            onClick = { viewModel.undoMove() },
            iconColor = iconColor,
            painter =  painterResource(id = R.drawable.outline_undo_24),
            contentDescription = stringResource(id = R.string.undo_action),
            modifier = actionModifier,
            enabled = viewModel.buttonShouldBeEnabled()
        )
        CustomIconButton( //Redo
            onClick = { viewModel.redoMove() },
            iconColor = iconColor,
            painter =  painterResource(id = R.drawable.outline_redo_24),
            contentDescription = stringResource(id = R.string.redo_action),
            modifier = actionModifier,
            enabled = viewModel.buttonShouldBeEnabled()
        )
    }
}
