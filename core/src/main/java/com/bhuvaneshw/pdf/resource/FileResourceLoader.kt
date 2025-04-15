package com.bhuvaneshw.pdf.resource

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.MimeTypeMap
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import java.io.File

internal class FileResourceLoader(
    onError: (String) -> Unit,
) : ResourceLoader {

    private val path = "/file/"
    private val assetLoader = WebViewAssetLoader.Builder()
        .setDomain(ResourceLoader.RESOURCE_DOMAIN)
        .addPathHandler(
            path,
            FileUriPathHandler(onError)
        )
        .build()

    override fun canHandle(uri: Uri) =
        uri.host == ResourceLoader.RESOURCE_DOMAIN && uri.path?.startsWith(path) == true

    override fun shouldInterceptRequest(uri: Uri): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(uri)
    }

}

private class FileUriPathHandler(
    private val onError: (String) -> Unit,
) : WebViewAssetLoader.PathHandler {

    @SuppressLint("UseKtx")
    override fun handle(path: String): WebResourceResponse? {
        return try {
            val filePath = Uri.decode(path)
            val file = File(filePath)
            val mimeType = file.mimeType ?: "application/octet-stream"
            val inputStream = file.inputStream()
            WebResourceResponse(mimeType, "UTF-8", inputStream)
        } catch (e: Exception) {
            onError(e.message ?: "$e")
            null
        }
    }

}

private val File.mimeType: String?
    get() {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(name)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension?.lowercase())
    }
