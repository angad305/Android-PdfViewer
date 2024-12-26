package com.acutecoder.pdfviewer.compose

import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.acutecoder.pdf.PdfViewer

@Composable
fun PdfViewer(
    state: PdfState,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    onReady: (PdfViewer.() -> Unit)? = null,
) {
    LaunchedEffect(state.source) {
        state.pdfViewer?.run {
            if (isInitialized)
                load(source = state.source)
        }
    }

    AndroidView(
        factory = { context ->
            PdfViewer(context).also {
                state.setPdfViewerTo(it)
                it.onReady {
                    load(state.source)
                    onReady?.invoke(it)
                }

                it.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                containerColor?.toArgb()?.let { color ->
                    it.setContainerBackgroundColor(color)
                }
            }
        },
        onRelease = {
            it.clearAllListeners()
            state.clearPdfViewer()
        },
        onReset = {
            it.clearAllListeners()
        },
        update = {
            containerColor?.toArgb()?.let { color ->
                it.setContainerBackgroundColor(color)
            }
        },
        modifier = modifier
    )
}
