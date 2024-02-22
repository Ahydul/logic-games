package com.example.tfg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.activegame.Board
import com.example.tfg.ui.theme.TFGTheme

class TestRegionsView : ComponentActivity() {
    val viewModel: ActiveGameViewModel = ActiveGameViewModel()

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
                        Test(
                            viewModel = viewModel,
                            modifier = Modifier
                                .background(colorResource(id = R.color.primary_background))
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun Test(
    viewModel: ActiveGameViewModel,
    modifier: Modifier = Modifier
) {
    Box (
        contentAlignment = Alignment.Center,
        modifier = modifier
    ){
        Board(viewModel=viewModel, modifier = Modifier)
        Text(
            text = "${viewModel.getRegionSize()}",
            color = Color.Red,
            modifier = Modifier.size(20.dp).background(Color.White)
        )
    }
}

@Preview
@Composable
fun TestPreview() {
    val shape = RoundedCornerShape(8.dp)

    TFGTheme {
        Test(
            viewModel = ActiveGameViewModel(),
            modifier = Modifier
                .background(colorResource(id = R.color.primary_background))
                .border(
                    width = 1.dp,
                    color = Color.Black,
                )
                .aspectRatio(ratio = 1f)
                .padding(4.dp)
                .clip(shape)
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = shape
                )
        )
    }
}

