package com.example.tfg.ui.components.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
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
fun MainFilledButton(
    onClick: () -> Unit,
    mainText: String,
    secondaryText: String? = null,
    color: Color = Color.Red,
    borderColor: Color = Color.Red,
    textColor: Color = Color.Black
) {
    FilledTonalButton(
        modifier = Modifier.fillMaxWidth(0.8f).border(width = 0.5.dp, color = borderColor, shape = CircleShape),
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = color)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = mainText,
                color = textColor,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
            if (secondaryText != null)
                Text(text = secondaryText,
                    color = textColor.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
        }
    }
}

@Composable
fun LabeledIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    iconColor: Color = colorResource(id = R.color.primary_color),
    label: String,
    labelColor: Color = colorResource(id = R.color.primary_color),
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
            fontSize = 12.sp,
        )
    }
}