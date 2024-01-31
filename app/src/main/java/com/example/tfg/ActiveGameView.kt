package com.example.tfg

import android.animation.ArgbEvaluator
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.common.Board
import com.example.tfg.ui.theme.TFGTheme
import kotlin.math.log

class ActiveGameView : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TFGTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(modifier = Modifier.background(Color.DarkGray))
                }
            }
        }
    }
}

@Composable
fun TopSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row {
            Text(
                text = "Hakyuu"
            )
        }
        Row {
            Text(
                text = "Dificultad"
            )
            Text(
                text = "Errores"
            )
            Text(
                text = "Pistas"
            )
            Text(
                text = "Tiempo"
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BottomSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // Actions
        Row {
            Text(
                text = "Hakyuu"
            )
        } // Values
        FlowRow {
            Text(
                text = "Dificultad"
            )
        }
    }
}

@Composable
fun Cell(value: Int, isSelected: Boolean,
         drawDividerDown: Boolean, drawDividerUp: Boolean,
         drawDividerRight: Boolean, drawDividerLeft: Boolean,
         modifier: Modifier = Modifier) {
    Log.d("TAG", "CELLcurrentRecomposeScope $currentRecomposeScope")

    val borderColor = colorResource(id = R.color.section_border)

    Box(
        modifier = modifier
            .border(
                width = 0.2.dp,
                color = colorResource(id = R.color.board_grid)
            )
    ) {
        Text(
            text = value.toString(),
            fontSize = 30.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        //Paints region borders and selecting UI
        Canvas(modifier = Modifier.matchParentSize()) {
            val borderSize = 1.dp.toPx()
            val drawDivider = {start: Offset, end: Offset ->
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

            if (drawDividerUp) drawHorizontalDivider(0f + borderSize / 2)
            if (drawDividerDown) drawHorizontalDivider(size.height - borderSize / 2)
            if (drawDividerRight) drawVerticalDivider(size.width - borderSize / 2)
            if (drawDividerLeft) drawVerticalDivider(0f + borderSize / 2)

            if(isSelected){
                drawOval(color = Color.Red)
            }
        }
    }
}

@Composable
fun Board(board: Board, modifier: Modifier = Modifier) {
    Log.d("TAG", "BOARDcurrentRecomposeScope $currentRecomposeScope")

    // Int = flatten index of a cell in the board
    var selectedTiles = remember { mutableStateListOf<Int>() }

    val getRow = { y: Float, height: Int ->
        (y * board.numRows / height).toInt()
    }
    val getColumn = { x: Float, width: Int ->
        (x * board.numColumns / width).toInt()
    }
    val getIndex = { x: Float, width: Int, y: Float, height: Int ->
        if (x > 0 && y > 0 && x < width && y < height) board.indexToInt(row = getRow(y, height), column = getColumn(x, width))!!
        else null
    }

    // Boolean to control selecting/deselecting behaviour
    var selecting = true

    Column(
        modifier = modifier
        // Tap -> removes all selections and selects the cell
        // Long press -> keeps selections. Selects or deselects the cell if cell was deselected or selected
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { offset ->
                    val index = getIndex(offset.x, size.width, offset.y, size.height)!!

                    //True -> Action=select ; False -> Action=deselect
                    selecting = !selectedTiles.contains(index)

                    if (selecting) selectedTiles.add(index)
                    else selectedTiles.remove(index)
                },
                onTap = { offset ->
                    selectedTiles.removeAll { true }
                    val index = getIndex(offset.x, size.width, offset.y, size.height)
                    if (index != null) {
                        if (!selectedTiles.contains(index))
                            selectedTiles.add(index)
                        else
                            selectedTiles.remove(index)
                    }
                }
            )
        }
        // Normal drag -> removes all selections and selects the cells
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { selectedTiles.removeAll { true } },
                onDrag = { change, _ ->
                    val index =
                        getIndex(change.position.x, size.width, change.position.y, size.height,)

                    if (index != null && !selectedTiles.contains(index)) {
                        selectedTiles.add(index)
                    }
                }
            )
        }
        //Long press drag -> keeps selections. Select or deselect the cells depending if first cell was deselected or selected
        .pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    val index = getIndex(offset.x, size.width, offset.y, size.height)!!
                    //True -> Action=select ; False -> Action=deselect
                    selecting = !selectedTiles.contains(index)
                },
                onDrag = { change, _ ->
                    val index =
                        getIndex(change.position.x, size.width, change.position.y, size.height,)
                    if (index != null) {
                        if (selecting && !selectedTiles.contains(index))
                            selectedTiles.add(index)

                        if (!selecting && selectedTiles.contains(index))
                            selectedTiles.remove(index)
                    }
                }
            )
        }
    ) {

        for (row in 0 .. board.numRows - 1) {

            Row(modifier = modifier.weight(1f)) {

                for (col in 0..board.numColumns - 1) {

                    Cell(
                        value = board.getCellValue(row = row, column = col),
                        isSelected = selectedTiles.contains(board.indexToInt(row = row, column = col)!!),
                        drawDividerRight = board.drawDividerRight(row = row, column = col),
                        drawDividerDown = board.drawDividerDown(row = row, column = col),
                        drawDividerLeft = board.drawDividerLeft(row = row, column = col),
                        drawDividerUp = board.drawDividerUp(row = row, column = col),

                        modifier = modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(colorResource(id = R.color.cell_background))
                    )
                }
            }
        }
    }

}


@Composable
fun MiddleSection(modifier: Modifier = Modifier) {
    Box (
        contentAlignment = Alignment.Center,
        modifier = modifier
    ){
        Board(board = Board.example(), modifier = Modifier)
    }
}


@Composable
fun App(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(6.dp)

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
        modifier = modifier
    ) {
        TopSection()
        MiddleSection(
            modifier
                .aspectRatio(ratio = 1f)
                .padding(4.dp)
                .clip(shape)
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = shape
                )
        )
        BottomSection()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TFGTheme {
        App(modifier = Modifier
            .background(colorResource(id = R.color.primary_background))
            .fillMaxSize())
    }
}

@Composable
fun CustomBoxWithDrawing(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                style = Stroke(8.dp.toPx()),
                color = Color.Red,
                center = Offset(size.minDimension / 2, size.minDimension / 2),
                radius = size.minDimension /5
            )
        }
    }
}