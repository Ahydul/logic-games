package com.example.tfg.ui.components.activegame

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
    cell: Cell, isSelected: () -> Boolean,
    dividersToDraw: Quadruple<Boolean>,
    modifier: Modifier = Modifier
) {
    Log.d("cell", "CELLcurrentRecomposeScope $currentRecomposeScope")
    //val cell = setCell()

    val value = cell.value
    val borderColor = colorResource(id = R.color.section_border)
    val cellValueColor = colorResource(id = R.color.cell_value)
    val cellNoteValueColor = colorResource(id = R.color.cell_note)

    Box(
        modifier = modifier
            .border(
                width = 0.2.dp,
                color = colorResource(id = R.color.board_grid)
            )
    ) {
        //Main value
        if (value != 0) {
            Icon(
                painter = painterResource(id = HakyuuValue.values()[value].icon),
                tint = cellValueColor,
                contentDescription = "Value $value"
            )
        }
        //Notes
        HorizontalGrid(rows = 3) {
            cell.notes.forEach {
                if (it != 0) {
                    Icon(
                        painter = painterResource(id = HakyuuValue.values()[it].icon),
                        tint = cellNoteValueColor,
                        contentDescription = "Value $it",
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }



        //Paints region borders and selecting UI
        Canvas(modifier = Modifier.matchParentSize()) {
            val borderSize = 1.dp.toPx()
            val drawDivider = { start: Offset, end: Offset ->
                drawLine(
                    color = borderColor,
                    start = start,
                    end = end,
                    strokeWidth = borderSize
                )
            }
            val drawHorizontalDivider = { y:Float ->
                drawDivider(Offset(0f, y), Offset(size.width, y))
            }
            val drawVerticalDivider = { x:Float ->
                drawDivider(Offset(x, size.width), Offset(x, 0f))
            }

            if (dividersToDraw.up) drawHorizontalDivider(0f + borderSize / 2)
            if (dividersToDraw.down) drawHorizontalDivider(size.height - borderSize / 2)
            if (dividersToDraw.right) drawVerticalDivider(size.width - borderSize / 2)
            if (dividersToDraw.left) drawVerticalDivider(0f + borderSize / 2)

            if(isSelected()){
                drawOval(color = Color.Red)
            }
        }
    }
}
