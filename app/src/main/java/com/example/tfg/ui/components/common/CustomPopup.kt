package com.example.tfg.ui.components.common

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.tfg.R

@Composable
fun animateBlur(
    expandedStates: MutableTransitionState<Boolean>
): State<Dp> {
    return androidx.compose.animation.core.animateDpAsState(
        targetValue = if (expandedStates.currentState || !expandedStates.currentState && expandedStates.targetState) 2.5.dp else 0.dp,
        animationSpec =
        if (expandedStates.currentState || !expandedStates.currentState && expandedStates.targetState)
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
    whenFalse: Float = 0f,
    whenTrue: Float = 1f
): State<Float> {
    return transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                tween(
                    durationMillis = InTransitionDuration,
                    easing = LinearOutSlowInEasing
                )
            } else {
                tween(
                    durationMillis = 1,
                    delayMillis = OutTransitionDuration
                )
            }
        }, label = "popup scale"
    ) { if (it) whenTrue else whenFalse }
}

@Composable
fun CustomPopup(
    expandedStates: MutableTransitionState<Boolean>,
    onDismissRequest: (() -> Unit) = { expandedStates.targetState = false },
    backgroundColor: Color = colorResource(id = R.color.board_grid),
    content: @Composable BoxScope.() -> Unit
) {
    if (expandedStates.targetState || expandedStates.currentState) {

        val transition = updateTransition(expandedStates, "DropDownMenu")
        val scale by animateScale(transition, whenFalse = 0.8f)

        Popup(
            alignment = Alignment.Center,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true),
            offset = IntOffset(0,-140)
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .fillMaxWidth(0.8f)
                    .background(backgroundColor)
                    .padding(3.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                ) {
                    content()
                }
                CustomIconButton(
                    onClick = onDismissRequest,
                    imageVector = ImageVector.vectorResource(id = R.drawable.outline_close_24),
                    contentDescription = "",
                )
            }
        }
    }
}
