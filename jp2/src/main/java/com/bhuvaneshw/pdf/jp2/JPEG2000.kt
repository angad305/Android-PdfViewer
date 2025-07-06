@file:Suppress("unused")

package com.bhuvaneshw.pdf.jp2

import android.content.Context

internal class JPEG2000 {
    private fun isPresent(context: Context): Boolean {
        return try {
            context.assets.open("com/bhuvaneshw/mozilla/web/wasm/openjpeg.wasm").use { }
            true
        } catch (_: Exception) {
            false
        }
    }
}
