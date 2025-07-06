@file:Suppress("unused")

package com.bhuvaneshw.pdf.icc

import android.content.Context

internal class PdfColorProfile {
    private fun isPresent(context: Context): Boolean {
        return try {
            context.assets.open("com/bhuvaneshw/mozilla/web/wasm/qcms_bg.wasm").use { }
            true
        } catch (_: Exception) {
            false
        }
    }
}
