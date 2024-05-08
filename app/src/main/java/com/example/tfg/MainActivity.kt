package com.example.tfg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tfg.ui.components.common.CustomIconButton
import com.example.tfg.ui.components.common.CustomFilledIconButton
import com.example.tfg.ui.components.common.LabeledIconButton
import com.example.tfg.ui.components.common.PopupMenu
import com.example.tfg.ui.theme.TFGTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TFGTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TFGTheme {
                        MainScreen(
                            modifier = Modifier
                                .background(colorResource(id = R.color.primary_background))
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.weight(1f))
        PopupMenu {
            CustomFilledIconButton(
                onClick = {  },
                imageVector = ImageVector.vectorResource(id = R.drawable.paint_brush),
                contentDescription = "Click me for menu",
                color = Color.Red,
            )
            CustomFilledIconButton(
                onClick = {  },
                imageVector = ImageVector.vectorResource(id = R.drawable.paint_brush),
                contentDescription = "Click me for menu",
                color = Color.Green
            )
            CustomFilledIconButton(
                onClick = {  },
                imageVector = ImageVector.vectorResource(id = R.drawable.paint_brush),
                contentDescription = "Click me for menu",
                color = Color.Magenta
            )
        }
        CustomIconButton(
            onClick = {  },
            imageVector = ImageVector.vectorResource(id = R.drawable.gear),
            contentDescription = "Click me for menu",
        )
    }
}

@Composable
fun NavigationBar(modifier: Modifier = Modifier) {
    val bgColor = colorResource(id = R.color.board_grid)
    Divider (
        color = colorResource(id = R.color.board_grid2),
        modifier = Modifier
            .height(1.dp)
            .fillMaxWidth()
    )
    Row(modifier = modifier
        .fillMaxWidth()
        .background(bgColor)
    ) {
        val mod = Modifier.weight(1f)

        LabeledIconButton(
            onClick = { /*TODO*/ },
            imageVector = ImageVector.vectorResource(id = R.drawable.house),
            label = "Inicio",
            modifier = mod
        )
        LabeledIconButton(
            onClick = { /*TODO*/ },
            imageVector = ImageVector.vectorResource(id = R.drawable.controller_game),
            label = "Juegos",
            modifier = mod
        )
        LabeledIconButton(
            onClick = { /*TODO*/ },
            imageVector = ImageVector.vectorResource(id = R.drawable.graphs),
            label = "Estadísticas",
            modifier = mod
        )

    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxHeight().padding(top = 10.dp)) {
        MainHeader(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.weight(18f))
        NavigationBar(modifier = Modifier.weight(2f))
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    TFGTheme {
        MainScreen(
            modifier = Modifier
                .background(colorResource(id = R.color.primary_background))
                .fillMaxWidth()
        )
    }
}

