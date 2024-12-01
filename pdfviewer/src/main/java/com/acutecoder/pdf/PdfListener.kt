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

}

@Suppress("FunctionName")
inline fun PdfOnPageLoadStart(crossinline callback: () -> Unit) =
    object : PdfListener {
        override fun onPageLoadStart() {
            callback()
        }
    }

@Suppress("FunctionName")
inline fun PdfOnPageLoadSuccess(crossinline callback: (pageCount: Int) -> Unit) =
    object : PdfListener {
        override fun onPageLoadSuccess(pagesCount: Int) {
            callback(pagesCount)
        }
    }

@Suppress("FunctionName")
inline fun PdfOnPageLoadFailed(crossinline callback: (errorMessage: String) -> Unit) =
    object : PdfListener {
        override fun onPageLoadFailed(errorMessage: String) {
            callback(errorMessage)
        }
    }

@Suppress("FunctionName")
inline fun PdfOnPageChange(crossinline callback: (pageNumber: Int) -> Unit) =
    object : PdfListener {
        override fun onPageChange(pageNumber: Int) {
            callback(pageNumber)
        }
    }

@Suppress("FunctionName")
inline fun PdfOnScaleChange(crossinline callback: (scale: Float) -> Unit) =
    object : PdfListener {
        override fun onScaleChange(scale: Float) {
            callback(scale)
        }
    }

@Suppress("FunctionName")
inline fun PdfOnDownload(crossinline callback: (pdfAsBytes: ByteArray) -> Unit) =
    object : PdfListener {
        override fun onSavePdf(pdfAsBytes: ByteArray) {
            callback(pdfAsBytes)
        }
    }

@Suppress("FunctionName")
inline fun PdfOnFindMatchChange(crossinline callback: (current: Int, total: Int) -> Unit) =
    object : PdfListener {
        override fun onFindMatchChange(current: Int, total: Int) {
            callback(current, total)
        }
    }

@Suppress("FunctionName")
inline fun PdfOnFindMatchStart(crossinline callback: () -> Unit) =
    object : PdfListener {
        override fun onFindMatchStart() {
            callback()
        }
    }

@Suppress("FunctionName")
inline fun PdfOnFindMatchComplete(crossinline callback: () -> Unit) =
    object : PdfListener {
        override fun onFindMatchComplete(found: Boolean) {
            callback()
        }
    }

@Suppress("FunctionName")
inline fun PdfOnScrollChange(crossinline callback: (currentOffset: Int, totalOffset: Int) -> Unit) =
    object : PdfListener {
        override fun onScrollChange(currentOffset: Int, totalOffset: Int) {
            callback(currentOffset, totalOffset)
        }
    }

@Suppress("FunctionName")
inline fun PdfOnLoadProperties(crossinline callback: (properties: PdfDocumentProperties) -> Unit) =
    object : PdfListener {
        override fun onLoadProperties(properties: PdfDocumentProperties) {
            callback(properties)
        }
    }
