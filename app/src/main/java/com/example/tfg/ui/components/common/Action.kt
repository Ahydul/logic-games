package com.example.tfg.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.tfg.R

@Composable
fun CustomIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
    iconColor: Color = colorResource(id = R.color.primary_color)
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
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
fun CustomFilledIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
    color: Color = Color.Red,
    iconColor: Color = Color.Black
) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = color),
        modifier = modifier
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
fun CustomText(
    modifier: Modifier = Modifier,
    mainText: String,
    textColor: Color = Color.Black,
    mainFontSize: TextUnit = TextUnit.Unspecified,
    secondaryText: String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = mainText,
            color = textColor,
            fontSize = mainFontSize,
            textAlign = TextAlign.Center,
            modifier = modifier
        )
        if (secondaryText != null)
            Text(text = secondaryText,
                color = textColor.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = modifier
            )
    }
}

@Composable
fun CustomFilledButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    mainText: String,
    secondaryText: String? = null,
    color: Color = Color.Red,
    borderColor: Color = Color.Red,
    textColor: Color = Color.Black,
    fontSize: TextUnit = TextUnit.Unspecified,
    textModifier: Modifier = Modifier
) {
    FilledTonalButton(
        modifier = modifier.border(width = 0.5.dp, color = borderColor, shape = CircleShape),
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = color)
    ) {
        CustomText(
            mainText = mainText,
            secondaryText = secondaryText,
            textColor = textColor,
            mainFontSize = fontSize,
            modifier = textModifier
        )
    }
}

@Composable
fun LabeledIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageVector: ImageVector,
    iconColor: Color = colorResource(id = R.color.primary_color),
    label: String,
    labelColor: Color = colorResource(id = R.color.primary_color),
    fontSize: TextUnit = TextUnit.Unspecified,
    shape: Shape = RectangleShape,
    borderStroke: BorderStroke? = null,
    iconPadding: Dp = 0.dp
) {
    CustomButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        borderStroke = borderStroke
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                tint = iconColor,
                imageVector = imageVector,
                contentDescription = label,
                modifier = Modifier
                    .weight(5f)
                    .fillMaxSize()
                    .padding(iconPadding, iconPadding, iconPadding, 0.dp)
            )
            Text(
                text = label,
                color = labelColor,
                fontSize = fontSize,
                modifier = Modifier.weight(2f)
            )
        }
    }
}

@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    shape: Shape = RectangleShape,
    borderStroke: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.board_grid)),
        contentPadding = paddingValues,
        border = borderStroke,
        modifier = modifier.height(intrinsicSize = IntrinsicSize.Min),
        content = content
    )
}


@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    state: MutableState<String>,
    label: @Composable (() -> Unit)? = null,
    bgColors: TextFieldColors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent,focusedContainerColor = Color(1f,1f,1f, 0.04f)),
    color: Color,
    numberValues: Boolean = false,
    range: List<String>? = null
) {
    val textStyle = TextStyle(color = color)
    val isRange = !range.isNullOrEmpty()

    Box(modifier = Modifier.height(IntrinsicSize.Min)) {
        TextField(
            value = state.value,
            textStyle = textStyle,
            onValueChange = { state.value = it },
            label = label,
            modifier = modifier,
            colors = bgColors,
            readOnly = isRange,
            keyboardOptions = if (numberValues) KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
            else KeyboardOptions.Default
        )
        if (isRange) {
            val i = range!!.indexOf(state.value).let { if (it == -1) null else it }
            require(i != null) {"Range doesn't have the value '${state.value}' included"}

            val pointer = remember { mutableIntStateOf(i) }
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val buttonMod = Modifier.size(25.dp)
                val iconModifier = Modifier.fillMaxSize()
                IconButton(
                    modifier = buttonMod,
                    onClick = {
                        if (pointer.intValue < range.size - 1) {
                            pointer.intValue = pointer.intValue + 1
                            state.value = range[pointer.intValue]
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.expand_less_24px),
                        contentDescription = "Add one",
                        tint = color,
                        modifier = iconModifier
                    )
                }
                IconButton(
                    modifier = buttonMod,
                    onClick = {
                        if (pointer.intValue > 0) {
                            pointer.intValue = pointer.intValue - 1
                            state.value = range[pointer.intValue]
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.expand_more_24px),
                        contentDescription = "Subtract one",
                        tint = color,
                        modifier = iconModifier
                    )
                }
            }
        }
    }
}