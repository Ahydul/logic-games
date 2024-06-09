package com.example.tfg.ui.components.common

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.tfg.R

@Composable
fun animateBlur(
    expandedStates: MutableTransitionState<Boolean>
): State<Dp> {
    return androidx.compose.animation.core.animateDpAsState(
        targetValue = if (expandedStates.currentState && expandedStates.targetState) 2.5.dp else 0.dp,
        animationSpec =
        if (expandedStates.currentState && expandedStates.targetState)
            tween(
                durationMillis = InTransitionDuration,
                easing = LinearOutSlowInEasing
            )
        else
            tween(
                durationMillis = OutTransitionDuration,
                easing = LinearOutSlowInEasing
            ), label = "AnimateBlur"
    )
}

@Composable
fun animateScale(
    transition: Transition<Boolean>,
    inDuration: Int = InTransitionDuration,
    whenFalse: Float = 0f,
    whenTrue: Float = 1f
): State<Float> {
    return transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                tween(
                    durationMillis = inDuration,
                    easing = LinearOutSlowInEasing
                )
            } else {
                tween(
                    durationMillis = 1,
                    delayMillis = OutTransitionDuration - 1
                )
            }
        }, label = "popup scale"
    ) { if (it) whenTrue else whenFalse }
}

@Composable
fun CustomPopup(
    modifier: Modifier = Modifier,
    expandedStates: MutableTransitionState<Boolean>,
    onDismissRequest: (() -> Unit) = { expandedStates.targetState = false },
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    offset: IntOffset = IntOffset(0,0),
    startScale: Float = 0.8f,
    closable: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    if (expandedStates.targetState || expandedStates.currentState) {

        val transition = updateTransition(expandedStates, "DropDownMenu")
        val scale by animateScale(transition, whenFalse = startScale)
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true),
            offset = offset,
        ) {
            val shape = RoundedCornerShape(8.dp)
            Box(
                modifier = modifier
                    .graphicsLayer {
                        //scaleX = scale
                        scaleY = scale
                    }
                    .border(color = borderColor, width = 1.dp, shape = shape)
                    .clip(shape)
                    .background(backgroundColor)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    content()
                }
                if (closable)
                    CustomIconButton(
                        onClick = onDismissRequest,
                        painter =  painterResource(id = R.drawable.outline_close_24),
                        contentDescription = "",
                    )
            }
        }
    }
}


@Composable
fun DropdownMenu(
    modifier: Modifier = Modifier,
    selected: String,
    numDropdownMenuButtons: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    val expandedStates = remember { MutableTransitionState(false) }
    val parentSize = remember { mutableStateOf(IntSize(0,0)) }
    Box {
        CustomButton(
            onClick = { expandedStates.targetState = true },
            modifier = modifier.onGloballyPositioned { coordinates ->
                parentSize.value = coordinates.size
            }
        ) {
            Text(
                text = selected,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = modifier.padding(horizontal = 10.dp)
            )
        }
        val factor = (numDropdownMenuButtons + 1).toDouble()/2
        val height = (parentSize.value.height * factor).toInt()
        CustomPopup(
            expandedStates = expandedStates,
            closable = false,
            offset = IntOffset(0, height),
            modifier = modifier
                .width(with(LocalDensity.current) { parentSize.value.width.toDp() })
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun DropdownMenuButton(
    onClick: () -> Unit,
    color: Color,
    text: String
) {
    CustomButton(
        onClick = onClick,
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = color
        )
    }
}
