package com.bhuvaneshw.pdf

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.webkit.JavascriptInterface
import com.bhuvaneshw.pdf.PdfViewer.Companion.PDF_VIEWER_URL
import com.bhuvaneshw.pdf.PdfViewer.PageScrollMode
import com.bhuvaneshw.pdf.PdfViewer.PageSpreadMode
import com.bhuvaneshw.pdf.js.toJsHex
import com.bhuvaneshw.pdf.print.PdfPrintBridge

@Suppress("Unused")
internal class WebInterface(private val pdfViewer: PdfViewer) {
    private var findMatchStarted = false

    @JavascriptInterface
    fun onLoadSuccess(count: Int) = post {
        pdfViewer.pagesCount = count
        pdfViewer.isInitialized = true  // Make sure this is set
        
        pdfViewer.setUpActualScaleValues {
            pdfViewer.scalePageTo(pdfViewer.actualDefaultPageScale)
        }
        pdfViewer.dispatchRotationChange(pdfViewer.pageRotation, false)
        pdfViewer.dispatchSnapChange(pdfViewer.snapPage, false)
    
        pdfViewer.dispatchSinglePageArrangement(pdfViewer.singlePageArrangement, false)
        pdfViewer.dispatchPageAlignMode(pdfViewer.pageAlignMode, false)
        @OptIn(PdfUnstableApi::class)
        pdfViewer.dispatchScrollSpeedLimit(pdfViewer.scrollSpeedLimit, false)
    
        // Apply stored preferences when PDF is loaded and ready
        pdfViewer.tempBackgroundColor?.let { 
            pdfViewer.setContainerBackgroundColor(it)
            pdfViewer.tempBackgroundColor = null
        }
        
        pdfViewer.tempDarkMode?.let { 
            // Apply dark mode after a short delay to ensure DOM is ready
            pdfViewer.mainHandler.postDelayed({
                pdfViewer.applyDarkModeInternal(it)
            }, 500)
            pdfViewer.tempDarkMode = null
        }
    
        // Call ready listeners
        pdfViewer.onReadyListeners.forEach { it(pdfViewer) }
        pdfViewer.listeners.forEach { it.onPageLoadSuccess(count) }
    }

    @JavascriptInterface
    fun onLoadFailed(error: String) = post {
        pdfViewer.listeners.forEach { it.onPageLoadFailed(error) }
    }

    @JavascriptInterface
    fun onProgressChange(progress: Int) = post {
        pdfViewer.listeners.forEach { it.onProgressChange(progress / 100f) }
    }

    @JavascriptInterface
    fun onPageChange(pageNumber: Int) = post({ pdfViewer.currentPage != pageNumber }) {
        pdfViewer.currentPage = pageNumber
        pdfViewer.listeners.forEach { it.onPageChange(pageNumber) }
    }

    @JavascriptInterface
    fun onScaleChange(scale: Float, scaleValue: String) =
        post({ pdfViewer.currentPageScale != scale || pdfViewer.currentPageScaleValue != scaleValue }) {
            pdfViewer.currentPageScale = scale
            pdfViewer.currentPageScaleValue = scaleValue
            pdfViewer.listeners.forEach { it.onScaleChange(scale) }
        }

    @JavascriptInterface
    fun onFindMatchStart() = post {
        findMatchStarted = true
        pdfViewer.listeners.forEach { it.onFindMatchStart() }
    }

    @JavascriptInterface
    fun onFindMatchChange(current: Int, total: Int) = post {
        if (findMatchStarted) pdfViewer.listeners.forEach { it.onFindMatchChange(current, total) }
    }

    @JavascriptInterface
    fun onFindMatchComplete(found: Boolean) = post {
        if (findMatchStarted) pdfViewer.listeners.forEach { it.onFindMatchComplete(found) }
        findMatchStarted = false
    }

    @JavascriptInterface
    fun onScroll(currentOffset: Int, totalOffset: Int, isHorizontal: Boolean) = post {
        if (pdfViewer.pageScrollMode != PageScrollMode.SINGLE_PAGE)
            pdfViewer.listeners.forEach {
                it.onScrollChange(
                    currentOffset,
                    totalOffset,
                    isHorizontal
                )
            }
    }

    @JavascriptInterface
    fun onPasswordDialogChange(isOpen: Boolean) = post {
        pdfViewer.listeners.forEach { it.onPasswordDialogChange(isOpen) }
    }

    @JavascriptInterface
    fun onSpreadModeChange(ordinal: Int) = post {
        pdfViewer.listeners.forEach { it.onSpreadModeChange(PageSpreadMode.entries[ordinal]) }
    }

    @JavascriptInterface
    fun onScrollModeChange(ordinal: Int) = post {
        pdfViewer.listeners.forEach { it.onScrollModeChange(PageScrollMode.entries[ordinal]) }
    }

    @JavascriptInterface
    fun onSingleClick() = post {
        pdfViewer.listeners.forEach { it.onSingleClick() }
    }

    @JavascriptInterface
    fun onDoubleClick() = post {
        pdfViewer.listeners.forEach { it.onDoubleClick() }
    }

    @JavascriptInterface
    fun onLongClick() = post {
        pdfViewer.listeners.forEach { it.onLongClick() }
    }

    @JavascriptInterface
    fun onLinkClick(link: String) = post {
        if (!link.startsWith(PDF_VIEWER_URL))
            pdfViewer.listeners.forEach { it.onLinkClick(link) }
    }

    @JavascriptInterface
    fun getHighlightEditorColorsString() = pdfViewer.highlightEditorColors
        .joinToString(separator = ",") { "${it.first}=${it.second.toJsHex()}" }

    @JavascriptInterface
    fun onPrintProcessStart() = post {
        pdfViewer.listeners.forEach { it.onPrintProcessStart() }
    }

    @JavascriptInterface
    fun onPrintProcessEnd() = post {
        pdfViewer.listeners.forEach { it.onPrintProcessEnd() }
    }

    @JavascriptInterface
    fun onPrintProcessProgress(progress: Float) = post {
        pdfViewer.listeners.forEach { it.onPrintProcessProgress(progress) }
    }

    @JavascriptInterface
    fun onLoadProperties(
        title: String,
        subject: String,
        author: String,
        creator: String,
        producer: String,
        creationDate: String,
        modifiedDate: String,
        keywords: String,
        language: String,
        pdfFormatVersion: String,
        fileSize: Long,
        isLinearized: Boolean,
        encryptFilterName: String,
        isAcroFormPresent: Boolean,
        isCollectionPresent: Boolean,
        isSignaturesPresent: Boolean,
        isXFAPresent: Boolean,
        customJson: String
    ) = post {
        pdfViewer.properties = PdfDocumentProperties(
            title = title,
            subject = subject,
            author = author,
            creator = creator,
            producer = producer,
            creationDate = creationDate,
            modifiedDate = modifiedDate,
            keywords = keywords,
            language = language,
            pdfFormatVersion = pdfFormatVersion,
            fileSize = fileSize,
            isLinearized = isLinearized,
            encryptFilterName = encryptFilterName,
            isAcroFormPresent = isAcroFormPresent,
            isCollectionPresent = isCollectionPresent,
            isSignaturesPresent = isSignaturesPresent,
            isXFAPresent = isXFAPresent,
            customJson = customJson,
        ).apply { pdfViewer.listeners.forEach { it.onLoadProperties(this) } }
    }

    @JavascriptInterface
    fun createPrintJob() = post {
        pdfViewer.pdfPrintAdapter?.let { pdfPrintAdapter ->
            val printManager =
                pdfViewer.context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "${pdfViewer.context.packageName} Document"

            if (pdfPrintAdapter is PdfPrintBridge) {
                pdfPrintAdapter.webView = pdfViewer.webView
            }

            printManager.print(
                jobName,
                pdfPrintAdapter,
                PrintAttributes.Builder().build()
            )
        }
    }

    @JavascriptInterface
    fun conveyMessage(message: String?, type: String?, pageNum: String?) = post {
        pdfViewer.pdfPrintAdapter?.let { pdfPrintAdapter ->
            if (pdfPrintAdapter is PdfPrintBridge)
                pdfPrintAdapter.onMessage(message, type, pageNum?.toIntOrNull())
        }
    }

    @JavascriptInterface
    fun getBase64FromBlobData(base64Data: String) {
        val pdfAsBytes: ByteArray = Base64.decode(
            base64Data.replaceFirst("^data:application/pdf;base64,".toRegex(), ""),
            0
        )

        post { pdfViewer.listeners.forEach { it.onSavePdf(pdfAsBytes) } }
    }

    fun getBase64StringFromBlobUrl(blobUrl: String): String? {
        if (blobUrl.startsWith("blob")) {
            return "var xhr = new XMLHttpRequest();" +
                    "xhr.open('GET', '" + blobUrl + "', true);" +
                    "xhr.setRequestHeader('Content-type','application/pdf');" +
                    "xhr.responseType = 'blob';" +
                    "xhr.onload = function(e) {" +
                    "    if (this.status == 200) {" +
                    "        var blobPdf = this.response;" +
                    "        var reader = new FileReader();" +
                    "        reader.readAsDataURL(blobPdf);" +
                    "        reader.onloadend = function() {" +
                    "            base64data = reader.result;" +
                    "            JWI.getBase64FromBlobData(base64data);" +
                    "        }" +
                    "    }" +
                    "};" +
                    "xhr.send();"
        }
        return null
    }

    private inline fun post(
        crossinline condition: () -> Boolean = { true },
        runnable: Runnable
    ) {
        pdfViewer.mainHandler.post {
            if (condition()) runnable.run()
        }
    }
}
