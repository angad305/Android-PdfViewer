package com.bhuvaneshw.pdf.resource

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader

internal class AssetResourceLoader(context: Context) : ResourceLoader {

    private val path = "/assets/"
    private val assetLoader = WebViewAssetLoader.Builder()
        .setDomain(ResourceLoader.RESOURCE_DOMAIN)
        .addPathHandler(
            path,
            WebViewAssetLoader.AssetsPathHandler(context)
        )
        .build()

    override fun canHandle(uri: Uri) =
        uri.host == ResourceLoader.RESOURCE_DOMAIN && uri.path?.startsWith(path) == true

    override fun shouldInterceptRequest(uri: Uri): WebResourceResponse? {
        if (uri.host == ResourceLoader.RESOURCE_DOMAIN && uri.path?.startsWith("${path}com/bhuvaneshw/mozilla") == true) {
            throw IllegalAccessException("Not allowed to load PdfViewer's internal files.")
        }

        return assetLoader.shouldInterceptRequest(uri)
    }

    override fun createSharableUri(context: Context, authority: String, source: String): Uri? = null

}
