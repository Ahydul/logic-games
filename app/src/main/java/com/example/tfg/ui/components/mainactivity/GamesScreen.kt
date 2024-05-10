package com.example.tfg.ui.components.mainactivity

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.R
import com.example.tfg.ui.components.common.CustomButton
import com.example.tfg.ui.components.common.LabeledIconButton

@Composable
private fun ChooseGameButton(modifier: Modifier = Modifier) {
    CustomButton(
        onClick = { Log.d("button", "choose game") },
        paddingValues = PaddingValues(12.dp, 12.dp, 0.dp, 12.dp),
        shape = RoundedCornerShape(8.dp),
        borderStroke = BorderStroke(0.5.dp, color = colorResource(id = R.color.board_grid2)),
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.hakyuu_dark),
            contentDescription = "",
            modifier = Modifier.weight(3f)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .weight(4f)
                .fillMaxHeight()
        ) {
            Text(text = "Hakyuu", fontSize = 25.sp, color = colorResource(id = R.color.primary_color))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val mod = Modifier.weight(1f).aspectRatio(1f)
                val shape = RoundedCornerShape(20.dp)
                val fontSize = 11.sp
                val iconPadding = 14.dp
                LabeledIconButton(
                    onClick = { Log.d("button", "REGLAS") },
                    imageVector = ImageVector.vectorResource(id = R.drawable.question_mark_24px),
                    iconColor = colorResource(id = R.color.primary_color),
                    label = "Reglas",
                    labelColor = colorResource(id = R.color.primary_color),
                    fontSize = fontSize,
                    shape = shape,
                    iconPadding = iconPadding,
                    modifier = mod
                )
                LabeledIconButton(
                    onClick = { Log.d("button", "CUSTOM") },
                    imageVector = ImageVector.vectorResource(id = R.drawable.handyman_24px),
                    iconColor = colorResource(id = R.color.primary_color),
                    label = "Custom",
                    labelColor = colorResource(id = R.color.primary_color),
                    fontSize = fontSize,
                    shape = shape,
                    iconPadding = iconPadding,
                    modifier = mod
                )
                LabeledIconButton(
                    onClick = { Log.d("button", "STATS") },
                    imageVector = ImageVector.vectorResource(id = R.drawable.graphs),
                    iconColor = colorResource(id = R.color.primary_color),
                    label = "Estadísticas",
                    labelColor = colorResource(id = R.color.primary_color),
                    fontSize = fontSize,
                    shape = shape,
                    iconPadding = iconPadding,
                    modifier = mod
                )
            }
        }
    }
}

@Composable
fun GamesScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        repeat(6){
            ChooseGameButton()
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
