package com.acutecoder.pdfviewer.compose

import androidx.compose.foundation.layout.BoxScope
import com.acutecoder.pdf.setting.PdfSettingsManager

open class PdfContainerScope internal constructor(
    internal val state: PdfState
)

class PdfContainerBoxScope internal constructor(
    state: PdfState,
    private val boxScope: BoxScope
) : PdfContainerScope(state), BoxScope by boxScope

class PdfToolBarScope internal constructor(
    val state: PdfState,
    val isFindBarOpen: () -> Boolean,
    val closeFindBar: () -> Unit
)
