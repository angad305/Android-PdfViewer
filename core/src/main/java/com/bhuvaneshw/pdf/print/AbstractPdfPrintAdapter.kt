package com.bhuvaneshw.pdf.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import android.util.Base64
import android.util.Log
import java.io.FileOutputStream

abstract class AbstractPdfPrintAdapter(private var context: Context) : PdfPrintBridge() {

    private var writer: FileOutputStream? = null
    private var cancellationSignal: android.os.CancellationSignal? = null
    private var callback: WriteResultCallback? = null
    private var pdfDocument: PrintedPdfDocument? = null
    private var pageCount = 0

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: android.os.CancellationSignal?,
        callback: LayoutResultCallback?,
        metadata: android.os.Bundle?
    ) {
        onProgress(0f)

        evaluateJavascript("PDFViewerApplication.pdfViewer.pagesCount;") { result ->
            if (cancellationSignal?.isCanceled == true) {
                callback?.onLayoutCancelled()
                return@evaluateJavascript
            }

            pdfDocument =
                PrintedPdfDocument(context, newAttributes ?: PrintAttributes.Builder().build())

            try {
                pageCount = result.toIntOrNull() ?: 0

                val builder = PrintDocumentInfo.Builder("PDFDocument.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_PHOTO)
                    .setPageCount(pageCount)

                callback?.onLayoutFinished(builder.build(), true)
            } catch (e: Exception) {
                Log.e("DefaultPdfPrintAdapter", "onLayout $e")
                callback?.onLayoutFailed("Failed to retrieve layout information")
            }
        }
    }

    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: android.os.CancellationSignal,
        callback: WriteResultCallback
    ) {
        try {
            onProgress(0f)
            this.writer = FileOutputStream(destination.fileDescriptor)
            this.cancellationSignal = cancellationSignal
            this.callback = callback

            evaluateJavascript("extractPrintImages()", null)
        } catch (e: Exception) {
            Log.e("DefaultPdfPrintAdapter", "onWrite $e")
            cancellationSignal.cancel()
        }
    }

    override fun onMessage(message: String?, type: String?, pageNum: Int?) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onWriteCancelled()
            return
        }

        try {
            val pdfDocument = pdfDocument
            when (type) {
                "PRINT_START" -> {
                    onRenderStart()
                }

                "PAGE_DATA" -> {
                    message ?: throw RuntimeException("message null!")
                    pageNum ?: throw RuntimeException("pageNum null!")
                    pdfDocument ?: throw RuntimeException("No document created!")

                    val imageBytes = decodeBase64(message)
                        ?: throw RuntimeException("Unable to decode page $pageNum")
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    val page = pdfDocument.startPage(pageNum - 1)

                    onRenderPage(page.canvas, page.info, bitmap)

                    pdfDocument.finishPage(page)
                    bitmap.recycle()

                    onProgress(pageNum / pageCount.toFloat())
                }

                "PRINT_END" -> {
                    pdfDocument?.writeTo(writer)
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))

                    writer?.run {
                        close()
                        flush()
                    }
                    onRenderEnd()
                }
            }
        } catch (e: Exception) {
            Log.e("DefaultPdfPrintAdapter", "onPage$pageNum $e")
            callback?.onWriteFailed(e.message ?: "Failed to write PDF data")
            cancellationSignal?.cancel()
        }
    }

    abstract fun onRenderStart()
    abstract fun onRenderEnd()
    abstract fun onRenderPage(
        canvas: Canvas,
        info: PdfDocument.PageInfo,
        bitmap: Bitmap
    )

    private fun decodeBase64(result: String): ByteArray? {
        return try {
            val base64 = result.trim('"')
            if (base64.contains(",")) {
                Base64.decode(base64.substringAfter(","), Base64.DEFAULT)
            } else {
                Base64.decode(base64, Base64.DEFAULT)
            }
        } catch (_: Exception) {
            null
        }
    }

}
