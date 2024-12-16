package com.acutecoder.pdfviewer.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import com.acutecoder.pdf.PdfViewer

@Composable
fun PdfContainer(
    state: PdfState,
    pdfViewer: @Composable PdfContainerBoxScope.() -> Unit,
    pdfToolBar: (@Composable PdfContainerScope.() -> Unit)? = null,
    pdfScrollBar: (@Composable PdfContainerBoxScope.(parentHeight: Int) -> Unit)? = null,
    loader: (@Composable PdfContainerBoxScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var parentHeight by remember { mutableIntStateOf(0) }

    Column(modifier = modifier) {
        pdfToolBar?.invoke(PdfContainerScope(state))

        Box(modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                parentHeight = it.size.height
            }
        ) {
            pdfViewer(PdfContainerBoxScope(state, this))
            pdfScrollBar?.let { scrollBar ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    scrollBar(PdfContainerBoxScope(state, this), parentHeight)
                }
            }

            if (state.isLoading || !state.isInitialized) loader?.invoke(
                PdfContainerBoxScope(state, this)
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
    PdfViewer(
        state = state,
        modifier = modifier,
        containerColor = containerColor,
        onReady = onReady,
    )
}

@Composable
fun PdfContainerScope.PdfToolBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    fileName: (() -> String)? = null,
    contentColor: Color? = null,
    backIcon: (@Composable PdfToolBarScope.() -> Unit)?
    = defaultToolBarBackIcon(contentColor, onBack),
    dropDownMenu: @Composable (onDismiss: () -> Unit, defaultMenus: @Composable () -> Unit) -> Unit = defaultToolBarDropDownMenu(),
) {
    PdfToolBar(
        state = state,
        title = title,
        modifier = modifier,
        onBack = null,
        backIcon = backIcon,
        fileName = fileName,
        contentColor = contentColor,
        dropDownMenu = dropDownMenu,
    )
}

@Composable
fun PdfContainerBoxScope.PdfScrollBar(
    parentHeight: Int,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.Black,
    handleColor: Color = Color(0xfff1f1f1),
    interactiveScrolling: Boolean = true
) {
    PdfScrollBar(
        state = state,
        parentHeight = parentHeight,
        modifier = modifier,
        contentColor = contentColor,
        handleColor = handleColor,
        interactiveScrolling = interactiveScrolling
    )
}
