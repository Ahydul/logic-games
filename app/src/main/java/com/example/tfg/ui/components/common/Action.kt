package com.example.tfg.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.R

@Composable
fun CustomIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
    iconColor: Color = colorResource(id = R.color.primary_color),
    modifier: Modifier = Modifier
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
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
    color: Color = Color.Red,
    iconColor: Color = Color.Black,
    modifier: Modifier = Modifier
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
    mainText: String,
    textColor: Color = Color.Black,
    mainFontSize: TextUnit = TextUnit.Unspecified,
    secondaryText: String? = null,
    modifier: Modifier = Modifier
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
fun MainFilledButton(
    onClick: () -> Unit,
    mainText: String,
    secondaryText: String? = null,
    color: Color = Color.Red,
    borderColor: Color = Color.Red,
    textColor: Color = Color.Black
) {
    FilledTonalButton(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .border(width = 0.5.dp, color = borderColor, shape = CircleShape),
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = color)
    ) {
        CustomText(
            mainText = mainText,
            secondaryText = secondaryText,
            textColor = textColor,
            mainFontSize = 22.sp,
            modifier = Modifier.fillMaxWidth(0.7f)
        )
    }
}
/*
@Composable
fun LabeledIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    iconColor: Color = colorResource(id = R.color.primary_color),
    label: String,
    labelColor: Color = colorResource(id = R.color.primary_color),
    fontSize: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomIconButton(onClick, imageVector, label, iconColor)
        Text(
            text = label,
            color = labelColor,
            fontSize = fontSize,
        )
    }
}

 */
@Composable
fun LabeledIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    iconColor: Color = colorResource(id = R.color.primary_color),
    label: String,
    labelColor: Color = colorResource(id = R.color.primary_color),
    fontSize: TextUnit = TextUnit.Unspecified,
    shape: Shape = RectangleShape,
    borderStroke: BorderStroke? = null,
    iconPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
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
                modifier = Modifier.weight(5f).fillMaxSize().padding(iconPadding, iconPadding, iconPadding, 0.dp)
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