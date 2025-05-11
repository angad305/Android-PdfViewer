package com.bhuvaneshw.pdf.resource

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader

internal class PdfViewerResourceLoader(context: Context) : ResourceLoader {

    companion object {
        const val PATH = "/pdfviewer/"
    }

    private val assetLoader = WebViewAssetLoader.Builder()
        .setDomain(ResourceLoader.RESOURCE_DOMAIN)
        .addPathHandler(
            PATH,
            WebViewAssetLoader.AssetsPathHandler(context)
        )
        .build()

    override fun canHandle(uri: Uri) =
        uri.host == ResourceLoader.RESOURCE_DOMAIN && uri.path?.startsWith(PATH) == true

    override fun shouldInterceptRequest(uri: Uri): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(uri)
    }

    // Should not create sharable uri with PdfViewerResourceLoader
    override fun createSharableUri(context: Context, authority: String, source: String): Uri? = null

}

