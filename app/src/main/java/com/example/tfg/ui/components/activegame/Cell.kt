package com.example.tfg.ui.components.activegame

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tfg.R
import com.example.tfg.common.Cell
import com.example.tfg.common.utils.Quadruple
import com.example.tfg.games.hakyuu.HakyuuValue
import com.example.tfg.ui.components.common.HorizontalGrid


@Composable
fun Cell(
    cell: Cell,
    isSelected: () -> Boolean,
    dividersToDraw: Quadruple<Boolean>,
    modifier: Modifier = Modifier
) {
    Log.d("cell", "CELL currentRecomposeScope $currentRecomposeScope")

    val gridColor = colorResource(id = R.color.board_grid)

    val backgroundColor = if (cell.backgroundColor == 0) colorResource(id = R.color.cell_background)
                            else Color(cell.backgroundColor).copy(alpha = 0.4f)
    val iconColor = if (cell.isError) colorResource(id = R.color.cell_value_error)
                        else colorResource(id = R.color.cell_value)

    val borderColor = colorResource(id = R.color.section_border)
    val noteColor = colorResource(id = R.color.cell_note)
    val value = cell.value

    Box(
        modifier = modifier
            .background(color = backgroundColor)
    ) {
        //Main value
        if (value != 0) {
            Icon(
                painter = painterResource(id = HakyuuValue.get(value).icon),
                tint = iconColor,
                contentDescription = "Value $value"
            )
        }
        //Notes
        HorizontalGrid(rows = 3) {
            cell.notes.forEach {
                if (it != 0) {
                    Icon(
                        painter = painterResource(id = HakyuuValue.get(it).icon),
                        tint = noteColor,
                        contentDescription = "Value $it",
                        modifier = Modifier.padding(2.dp)
                    )
                }
                else {
                    Spacer(modifier = Modifier.padding(2.dp))
                }
            }
        }



        //Paints region borders and selecting UI
        Canvas(modifier = Modifier.matchParentSize()) {
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

            if(isSelected()){
                drawOval(color = Color.Red, alpha = 0.2f)
            }
        }
    }
}