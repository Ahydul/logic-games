package com.example.tfg.ui.components.common

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.drawToBitmap
/**
 * https://mobileappcircular.com/jetpack-composable-to-bitmap-image-4ddea2fe3238
 * **/
@Composable
fun CaptureBitmap(
    content: @Composable () -> Unit,
): () -> Bitmap? {

    val context = LocalContext.current

    /**
     * ComposeView that would take composable as its content
     * Kept in remember so recomposition doesn't re-initialize it
     **/
    val composeView = remember { ComposeView(context) }

    /**
     * Callback function which could get latest image bitmap
     **/
    fun captureBitmap(): Bitmap? {
        return try {
            composeView.drawToBitmap()
        } catch (error: IllegalStateException) {
            null
        }
    }

    /** Use Native View inside Composable **/
    AndroidView(
        factory = {
            composeView.apply {
                setContent {
                    content.invoke()
                }
            }
        }
    )

    /** returning callback to bitmap **/
    return ::captureBitmap
}