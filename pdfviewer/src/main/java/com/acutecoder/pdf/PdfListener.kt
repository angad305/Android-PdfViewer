package com.acutecoder.pdf

interface PdfListener {

    fun onPageLoadStart() {}
    fun onPageLoadSuccess(pagesCount: Int) {}
    fun onPageLoadFailed(errorMessage: String) {}
    fun onPageChange(pageNumber: Int) {}
    fun onScaleChange(scale: Float) {}
    fun onSavePdf(pdfAsBytes: ByteArray) {}
    fun onFindMatchStart() {}
    fun onFindMatchChange(current: Int, total: Int) {}
    fun onFindMatchComplete(found: Boolean) {}
    fun onScrollChange(currentOffset: Int, totalOffset: Int) {}
    fun onLoadProperties(properties: PdfDocumentProperties) {}
    fun onPasswordDialogChange(isOpen: Boolean) {}
    fun onScrollModeChange(scrollMode: PdfViewer.PageScrollMode) {}
    fun onSpreadModeChange(spreadMode: PdfViewer.PageSpreadMode) {}
    fun onRotationChange(rotation: PdfViewer.PageRotation) {}

}

@Suppress("FunctionName")
fun PdfOnPageLoadStart(callback: () -> Unit) =
    object : PdfListener {
        override fun onPageLoadStart() {
            callback()
        }
    }

@Suppress("FunctionName")
fun PdfOnPageLoadSuccess(callback: (pageCount: Int) -> Unit) =
    object : PdfListener {
        override fun onPageLoadSuccess(pagesCount: Int) {
            callback(pagesCount)
        }
    }

@Suppress("FunctionName")
fun PdfOnPageLoadFailed(callback: (errorMessage: String) -> Unit) =
    object : PdfListener {
        override fun onPageLoadFailed(errorMessage: String) {
            callback(errorMessage)
        }
    }

@Suppress("FunctionName")
fun PdfOnPageChange(callback: (pageNumber: Int) -> Unit) =
    object : PdfListener {
        override fun onPageChange(pageNumber: Int) {
            callback(pageNumber)
        }
    }

@Suppress("FunctionName")
fun PdfOnScaleChange(callback: (scale: Float) -> Unit) =
    object : PdfListener {
        override fun onScaleChange(scale: Float) {
            callback(scale)
        }
    }

@Suppress("FunctionName")
fun PdfOnDownload(callback: (pdfAsBytes: ByteArray) -> Unit) =
    object : PdfListener {
        override fun onSavePdf(pdfAsBytes: ByteArray) {
            callback(pdfAsBytes)
        }
    }

@Suppress("FunctionName")
fun PdfOnFindMatchChange(callback: (current: Int, total: Int) -> Unit) =
    object : PdfListener {
        override fun onFindMatchChange(current: Int, total: Int) {
            callback(current, total)
        }
    }

@Suppress("FunctionName")
fun PdfOnFindMatchStart(callback: () -> Unit) =
    object : PdfListener {
        override fun onFindMatchStart() {
            callback()
        }
    }

@Suppress("FunctionName")
fun PdfOnFindMatchComplete(callback: (found: Boolean) -> Unit) =
    object : PdfListener {
        override fun onFindMatchComplete(found: Boolean) {
            callback(found)
        }
    }

@Suppress("FunctionName")
fun PdfOnScrollChange(callback: (currentOffset: Int, totalOffset: Int) -> Unit) =
    object : PdfListener {
        override fun onScrollChange(currentOffset: Int, totalOffset: Int) {
            callback(currentOffset, totalOffset)
        }
    }

@Suppress("FunctionName")
fun PdfOnLoadProperties(callback: (properties: PdfDocumentProperties) -> Unit) =
    object : PdfListener {
        override fun onLoadProperties(properties: PdfDocumentProperties) {
            callback(properties)
        }
    }

@Suppress("FunctionName")
fun PdfOnPasswordDialogChange(callback: (isOpen: Boolean) -> Unit) =
    object : PdfListener {
        override fun onPasswordDialogChange(isOpen: Boolean) {
            callback(isOpen)
        }
    }

@Suppress("FunctionName")
fun PdfOnScrollModeChange(callback: (scrollMode: PdfViewer.PageScrollMode) -> Unit) =
    object : PdfListener {
        override fun onScrollModeChange(scrollMode: PdfViewer.PageScrollMode) {
            callback(scrollMode)
        }
    }

@Suppress("FunctionName")
fun PdfOnSpreadModeChange(callback: (spreadMode: PdfViewer.PageSpreadMode) -> Unit) =
    object : PdfListener {
        override fun onSpreadModeChange(spreadMode: PdfViewer.PageSpreadMode) {
            callback(spreadMode)
        }
    }

@Suppress("FunctionName")
fun PdfOnRotationChange(callback: (rotation: PdfViewer.PageRotation) -> Unit) =
    object : PdfListener {
        override fun onRotationChange(rotation: PdfViewer.PageRotation) {
            callback(rotation)
        }
    }
