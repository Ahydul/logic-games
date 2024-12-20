package com.example.tfg.ui.components.activegame

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tfg.R
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.games.hakyuu.NumberValue
import com.example.tfg.games.kendoku.KendokuOperation
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.GridLayout


@Composable
fun Cell(
    corner: Pair<Int, KendokuOperation>? = null,
    viewModel: ActiveGameViewModel,
    coordinate: Coordinate
) {
    val cell = viewModel.getCell(coordinate)

    Log.d("recomposition", "CELL recomposition $cell")

    val isSelected = remember { { viewModel.isTileSelected(coordinate) } }
    val dividersToDraw = remember { viewModel.dividersToDraw(coordinate) }
    val isSecondarySelected = remember { { viewModel.isTileSecondarySelected(coordinate) } }

    val borderColor = colorResource(id = R.color.section_border)
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val noteColor = colorResource(id = R.color.note_color)
    val selectionColor = colorResource(id = R.color.selection_color)
    val backgroundColor = if (cell.backgroundColor == 0) MaterialTheme.colorScheme.secondary
        else if (cell.isErrorAndHasErrorBackground()) Color(cell.backgroundColor).copy(alpha = 0.4f)
        else Color(cell.backgroundColor).copy(alpha = 0.7f)
    val iconColor = if (cell.readOnly) MaterialTheme.colorScheme.onPrimary
        else if (cell.isError) MaterialTheme.colorScheme.error
        else colorResource(id = R.color.cell_value)

    val value = cell.value
    val modifier = Modifier

    Box(modifier = modifier.background(backgroundColor)) {
        Column {
            if (corner != null) {
                val cornerValue = corner.first
                Row(modifier.weight(0.25f)) {
                    for (value in NumberValue.getBigNumber(cornerValue)) {
                        Icon(
                            painter = painterResource(id = value.icon),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            contentDescription = "Value $cornerValue",
                            modifier = modifier.padding(start = 1.dp, top = 1.dp)
                        )
                    }
                    val cornerOperation = corner.second.toKnownEnum()
                    if (cornerOperation != null) {
                        Icon(
                            painter = painterResource(id = cornerOperation.icon),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            contentDescription = "Operation $cornerOperation",
                            modifier = modifier.padding(start = 1.dp, top = 1.dp)
                        )
                    }
                }
            }
            //Main value
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .weight(0.75f)
                    .fillMaxSize()
            ) {
                if (value != 0) {
                    Icon(
                        painter = painterResource(id = NumberValue.get(value).icon),
                        tint = iconColor,
                        contentDescription = "Value $value",
                        modifier = modifier.padding(if (corner != null) 2.dp else 6.dp)
                    )
                }
                //Notes
                GridLayout(numRows = 3, modifier = modifier.padding(2.dp)) {
                    cell.notes.forEach {
                        if (it != 0) {
                            Icon(
                                painter = painterResource(id = NumberValue.get(it).icon),
                                tint = noteColor,
                                contentDescription = "Value $it",
                            )
                        }
                        else {
                            Spacer(modifier = modifier)
                        }
                    }
                }
            }
        }

        //Paints region borders and selecting UI
        val canvasModifier = modifier.matchParentSize()
        Canvas(modifier = canvasModifier) {
            val bigBorderSize = 2.dp.toPx()
            val smallBorderSize = 0.8.dp.toPx()

            val drawTopDivider = { color: Color, dividerSize: Float ->
                val y = 0f
                drawLine(color = color, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = dividerSize)
            }
            val drawBottomDivider = { color: Color, dividerSize: Float ->
                val y = size.height
                drawLine(color = color, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = dividerSize)
            }
            val drawLeftDivider = { color: Color, dividerSize: Float ->
                val x = 0f
                drawLine(color = color, start = Offset(x, size.width), end = Offset(x, 0f), strokeWidth = dividerSize)
            }
            val drawRightDivider = { color: Color, dividerSize: Float ->
                val x = size.width
                drawLine(color = color, start = Offset(x, size.width), end = Offset(x, 0f), strokeWidth = dividerSize)
            }

            if (dividersToDraw.up) drawTopDivider(borderColor, bigBorderSize)
            else drawTopDivider(gridColor, smallBorderSize)

            if (dividersToDraw.down) drawBottomDivider(borderColor, bigBorderSize)
            else drawBottomDivider(gridColor, smallBorderSize)

            if (dividersToDraw.right) drawRightDivider(borderColor, bigBorderSize)
            else drawRightDivider(gridColor, smallBorderSize)

            if (dividersToDraw.left) drawLeftDivider(borderColor, bigBorderSize)
            else drawLeftDivider(gridColor, smallBorderSize)

            if(isSelected()) drawRect(color = selectionColor, alpha = 0.35f)
            if(isSecondarySelected()) drawRect(color = selectionColor, alpha = 0.15f)
        }
    }
}