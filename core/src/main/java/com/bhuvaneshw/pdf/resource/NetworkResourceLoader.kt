package com.bhuvaneshw.pdf.resource

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import java.net.HttpURLConnection
import java.net.URL

internal class NetworkResourceLoader : ResourceLoader {

    private val path = "/network/"
    private val assetLoader = WebViewAssetLoader.Builder()
        .setDomain(ResourceLoader.RESOURCE_DOMAIN)
        .addPathHandler(
            path,
            NetworkUriPathHandler()
        )
        .build()

    override fun canHandle(uri: Uri) =
        uri.host == ResourceLoader.RESOURCE_DOMAIN && uri.path?.startsWith(path) == true

    override fun shouldInterceptRequest(uri: Uri): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(uri)
    }

}

private class NetworkUriPathHandler : WebViewAssetLoader.PathHandler {

    @SuppressLint("UseKtx")
    override fun handle(path: String): WebResourceResponse? {
        return try {
            val uri = Uri.decode(path)
            val url = URL(uri)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            val mimeType = connection.contentType ?: "application/octet-stream"
            val inputStream = connection.inputStream

            WebResourceResponse(mimeType, "UTF-8", inputStream)
        } catch (e: Exception) {
            throw e
            null
        }
    }

}
