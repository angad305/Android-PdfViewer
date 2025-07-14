package com.bhuvaneshw.pdf.print

import android.print.PrintDocumentAdapter
import android.webkit.ValueCallback
import android.webkit.WebView

abstract class PdfPrintBridge : PrintDocumentAdapter() {
    internal lateinit var webView: WebView
    internal lateinit var onProgress: (progress: Float) -> Unit

    internal fun evaluateJavascript(script: String, resultCallback: ValueCallback<String>?) =
        webView.evaluateJavascript(script, resultCallback)

    internal abstract fun onMessage(message: String?, type: String?, pageNum: Int?)
}
