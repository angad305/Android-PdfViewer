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
    pdfState: PdfState,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    onCreateViewer: (PdfViewer.() -> Unit)? = null,
    onReady: (PdfViewer.(loadSource: () -> Unit) -> Unit) = { loadSource -> loadSource() },
) {
    LaunchedEffect(pdfState.source) {
        pdfState.pdfViewer?.run {
            if (isInitialized)
                load(source = pdfState.source)
        }
    }

    AndroidView(
        factory = { context ->
            PdfViewer(context).also {
                pdfState.setPdfViewerTo(it)
                onCreateViewer?.invoke(it)
                it.onReady {
                    onReady(this) { load(pdfState.source) }
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
            pdfState.clearPdfViewer()
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
