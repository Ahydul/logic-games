package com.example.tfg.ui.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.example.tfg.R

class LeftPopupPositionProvider(private val buttonBounds: IntRect) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val x = buttonBounds.left + buttonBounds.width - popupContentSize.width
        val y = buttonBounds.top
        return IntOffset(x, y)
    }
}

//TODO: eventually use abstracted CustomPopup
@Composable
fun PopupMenu(
    modifier: Modifier = Modifier,
    properties: PopupProperties = PopupProperties(focusable = true),
    expandedColor: Color,
    dismissedColor: Color,
    content: @Composable RowScope.() -> Unit
) {
    var buttonBounds by remember { mutableStateOf(IntRect(0,0,0,0)) }
    val expandedStates = remember { MutableTransitionState(false) }
    val onDismissRequest = { expandedStates.targetState = false }
    val animatedSurfaceColor by animateColorAsState(
        targetValue = if (expandedStates.currentState || !expandedStates.currentState && expandedStates.targetState) expandedColor else dismissedColor,
        animationSpec = tween(
            durationMillis = InTransitionDuration,
            easing = SlowOutFastInEasing
        ), label = "AnimateColor"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (expandedStates.currentState || !expandedStates.currentState && expandedStates.targetState) expandedColor else dismissedColor,
        animationSpec = tween(
            durationMillis = InTransitionDuration * 2,
            easing = SlowOutFastInEasing
        ), label = "AnimateColor"
    )

    val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }

    val menuButton: @Composable (Modifier) -> Unit = {
        Surface(
            shape = CircleShape,
            color = animatedSurfaceColor,
            border = BorderStroke(1.dp, animatedBorderColor)
        ) {
            CustomIconButton(
                onClick = { expandedStates.targetState = !expandedStates.targetState },
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_color_lens_24),
                contentDescription = "Click me for menu",
                modifier = it
            )
        }
    }

    menuButton(Modifier.onGloballyPositioned {
        buttonBounds = it.boundsInWindow().roundToIntRect()
    })

    if (expandedStates.targetState || expandedStates.currentState) {
        Popup(
            popupPositionProvider = LeftPopupPositionProvider(buttonBounds),
            onDismissRequest = onDismissRequest,
            properties = properties
        ) {
            PopupMenuContent(
                expandedStates = expandedStates,
                transformOriginState = transformOriginState,
                borderColor = expandedColor,
                surfaceColor = dismissedColor,
                modifier = modifier,
                content = content,
                buttonBounds = buttonBounds
            ){
                menuButton(Modifier)
            }
        }
    }

}

@Composable
private fun PopupMenuContent(
    expandedStates: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    modifier: Modifier,
    borderColor: Color,
    surfaceColor: Color,
    content: @Composable RowScope.() -> Unit,
    buttonBounds: IntRect,
    button: @Composable () -> Unit
) {
    val transition = updateTransition(expandedStates, "DropDownMenu")
    val expandedFloat by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = InTransitionDuration,
                easing = LinearOutSlowInEasing
            )
        }, label = "ExpandedFloat"
    ) { expanded ->
        if (expanded) 1f else 0f
    }

    Surface(
        modifier = Modifier.graphicsLayer {
            clip = true
            shape = ClippedRectangleShape(widthPercentage = expandedFloat, minWidth = buttonBounds.width, minHeight = buttonBounds.height)
            transformOrigin = transformOriginState.value
        },
        shape = CircleShape,
        border = if (expandedStates.targetState) BorderStroke(1.dp, borderColor) else null,
        color = surfaceColor,
    ) {
        Row(
            modifier = modifier
                .padding(start = 10.dp)
                .width(IntrinsicSize.Max)
                .verticalScroll(rememberScrollState()),
        ) {
            content()
            button()
        }
    }
}

// Menu open/close animation.
internal const val InTransitionDuration = 240
internal const val OutTransitionDuration = 75
internal val SlowOutFastInEasing: Easing = CubicBezierEasing(0.0f, 0.2f, 1.0f, 1.0f)
