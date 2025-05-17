package com.bhuvaneshw.pdfviewerdemo

import android.app.Application
import com.bhuvaneshw.pdf.PdfViewer

class PdfViewerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Logs webview console while running in debug mode
        PdfViewer.preventWebViewConsoleLog = !BuildConfig.DEBUG
    }
}
