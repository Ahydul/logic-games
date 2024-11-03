package com.example.tfg.ui.components.common

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.R
import com.example.tfg.common.utils.Utils

@Composable
fun CustomIconButton(
    modifier: Modifier = Modifier,
    insideModifier: Modifier = Modifier,
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String? = null,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.padding(0.dp)
    ) {
        Icon(
            tint = iconColor,
            painter = painter,
            contentDescription = contentDescription,
            modifier = insideModifier.fillMaxSize()
        )
    }
}

@Composable
fun CustomFilledIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String?,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.background,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = color),
        modifier = modifier
    ) {
        Icon(
            tint = iconColor,
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CustomText(
    modifier: Modifier = Modifier,
    mainText: String,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    mainFontSize: TextUnit = TextUnit.Unspecified,
    reverse: Boolean = false,
    secondaryText: String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (reverse && secondaryText != null)
            Text(text = secondaryText,
                color = textColor.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = modifier
            )
        Text(
            text = mainText,
            color = textColor,
            fontSize = mainFontSize,
            textAlign = TextAlign.Center,
            modifier = modifier
        )
        if (!reverse && secondaryText != null)
            Text(text = secondaryText,
                color = textColor.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = modifier
            )
    }
}

@Composable
fun CustomButton2(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    enabled: Boolean = true,
    horizontalArrangement: Arrangement.HorizontalOrVertical = Arrangement.Center,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Button(
        onClick = onClick,
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = shape,
        modifier = modifier.width(IntrinsicSize.Min),
        enabled = enabled
    ) {
        Row(
            horizontalArrangement = horizontalArrangement,
            modifier = modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun CustomSwitchButton(
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
    checked: Boolean,
    enabled: Boolean = true,
    label: @Composable RowScope.() -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        label()
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier
                .width(IntrinsicSize.Min)
                .padding(start = 10.dp),
            enabled = enabled
        )
    }
}


@Composable
fun LabeledIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageVector: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    label: String,
    labelColor: Color = MaterialTheme.colorScheme.onPrimary,
    fontSize: TextUnit = 11.sp,
    iconTextHeightProportion: Float = 0.7f,
    borderStroke: BorderStroke? = null,
    iconPadding: Dp = 0.dp
) {
    CustomButton(
        onClick = onClick,
        modifier = modifier,
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
                    .fillMaxWidth()
                    .fillMaxHeight(iconTextHeightProportion)
                    .padding(iconPadding, iconPadding, iconPadding, 0.dp)
            )
            Text(
                text = label,
                color = labelColor,
                fontSize = fontSize,
                softWrap = false
            )
        }
    }
}

@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    shape: Shape = RectangleShape,
    borderStroke: BorderStroke? = null,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.buttonColors(color),
        contentPadding = paddingValues,
        border = borderStroke,
        enabled = enabled,
        modifier = modifier.height(intrinsicSize = IntrinsicSize.Min),
        content = content
    )
}


@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    state: MutableState<String>,
    label: @Composable (() -> Unit)? = null,
    color: Color,
    backgroundColor: Color,
    numberValues: Boolean = false,
    range: List<String>? = null
) {
    val textStyle = TextStyle(color = color)
    val isRange = !range.isNullOrEmpty()

    Box(
        modifier = Modifier.height(IntrinsicSize.Max)
    ) {
        TextField(
            value = state.value,
            textStyle = textStyle,
            onValueChange = { state.value = it },
            label = label,
            modifier = modifier,
            colors = TextFieldDefaults.colors(unfocusedContainerColor = backgroundColor,focusedContainerColor = backgroundColor.copy(alpha = 0.7f)),
            readOnly = isRange,
            keyboardOptions = if (numberValues) KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                else KeyboardOptions.Default
        )
        if (isRange) {
            val i = range!!.indexOf(state.value).let { if (it == -1) null else it }
            require(i != null) {"Range doesn't have the value '${state.value}' included"}

            val pointer = remember { mutableIntStateOf(i) }
            Column(
                modifier = modifier
                    .align(Alignment.CenterEnd)
                    .height(54.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val buttonMod = Modifier.weight(1f)
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
                        tint = color,
                        painter = painterResource(id = R.drawable.arrow_up),
                        contentDescription = null,
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
                        tint = color,
                        painter = painterResource(id = R.drawable.arrow_down),
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
fun CustomClickableText(
    annotatedString: AnnotatedString,
    context: Context
) {
    ClickableText(text = annotatedString, style = TextStyle(color = MaterialTheme.colorScheme.onPrimary) ) { offset ->
        annotatedString.getStringAnnotations(tag = "web", start = offset, end = offset).firstOrNull()?.let {
            Utils.goToWebPage(it.item, context = context)
        }
    }
}

@Composable
fun CopyableText(text: String, description: String, context: Context) {
    val annotatedString = Utils.buildCopyableString(text, context = context)
    ClickableText(text = annotatedString, style = TextStyle(color = MaterialTheme.colorScheme.onPrimary) ) { offset ->
        annotatedString.getStringAnnotations(tag = "copy", start = offset, end = offset).firstOrNull()?.let {
            Utils.copyText(text = text, description = description, context = context)
            Toast.makeText(context,
                context.getString(R.string.text_copied_to_clipboard), Toast.LENGTH_SHORT).show()
        }
    }
}