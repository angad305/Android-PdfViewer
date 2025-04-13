package com.bhuvaneshw.pdf.resource

import android.net.Uri
import android.webkit.WebResourceResponse

internal interface ResourceLoader {

    fun canHandle(uri: Uri): Boolean
    fun shouldInterceptRequest(uri: Uri): WebResourceResponse?

    companion object {
        internal const val RESOURCE_DOMAIN = "pdfviewer-assets.bhuvaneshw.app"
    }
}
