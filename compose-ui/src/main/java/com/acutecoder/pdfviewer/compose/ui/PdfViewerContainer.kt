package com.acutecoder.pdfviewer.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.acutecoder.pdf.PdfViewer
import com.acutecoder.pdfviewer.compose.PdfState

@Composable
fun PdfViewerContainer(
    pdfState: PdfState,
    pdfViewer: @Composable PdfContainerBoxScope.() -> Unit,
    pdfToolBar: (@Composable PdfContainerScope.() -> Unit)? = null,
    pdfScrollBar: (@Composable PdfContainerBoxScope.(parentSize: IntSize) -> Unit)? = null,
    loadingIndicator: (@Composable PdfContainerBoxScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var parentSize by remember { mutableStateOf(IntSize(1, 1)) }

    Column(modifier = modifier) {
        pdfToolBar?.invoke(PdfContainerScope(pdfState))

        Box(modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { parentSize = it.size }
        ) {
            pdfViewer(PdfContainerBoxScope(pdfState, this))
            pdfScrollBar?.let { scrollBar ->
                Box(modifier = Modifier.fillMaxSize()) {
                    scrollBar(PdfContainerBoxScope(pdfState, this), parentSize)
                }
            }

            if (pdfState.isLoading || !pdfState.isInitialized) loadingIndicator?.invoke(
                PdfContainerBoxScope(pdfState, this)
            )
        }
    }
}

@Composable
fun PdfContainerBoxScope.PdfViewer(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    onReady: (PdfViewer.() -> Unit)? = null,
) {
    com.acutecoder.pdfviewer.compose.PdfViewer(
        state = pdfState,
        modifier = modifier,
        containerColor = containerColor,
        onReady = onReady,
    )
}

@Composable
fun PdfContainerScope.PdfToolBar(
    title: String,
    modifier: Modifier = Modifier,
    toolBarState: PdfToolBarState = rememberToolBarState(),
    onBack: (() -> Unit)? = null,
    fileName: (() -> String)? = null,
    contentColor: Color? = null,
    backIcon: (@Composable PdfToolBarScope.() -> Unit)?
    = defaultToolBarBackIcon(contentColor, onBack),
    dropDownMenu: @Composable (onDismiss: () -> Unit, defaultMenus: @Composable (validator: (PdfToolBarMenuItem) -> Boolean) -> Unit) -> Unit = defaultToolBarDropDownMenu(),
) {
    PdfToolBar(
        pdfState = pdfState,
        title = title,
        modifier = modifier,
        toolBarState = toolBarState,
        onBack = null,
        backIcon = backIcon,
        fileName = fileName,
        contentColor = contentColor,
        dropDownMenu = dropDownMenu,
    )
}

@Composable
fun PdfContainerBoxScope.PdfScrollBar(
    parentSize: IntSize,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.Black,
    handleColor: Color = Color(0xfff1f1f1),
    interactiveScrolling: Boolean = true,
    useVerticalScrollBarForHorizontalMode: Boolean = false,
) {
    PdfScrollBar(
        pdfState = pdfState,
        parentSize = parentSize,
        modifier = modifier,
        contentColor = contentColor,
        handleColor = handleColor,
        interactiveScrolling = interactiveScrolling,
        useVerticalScrollBarForHorizontalMode = useVerticalScrollBarForHorizontalMode,
    )
}
