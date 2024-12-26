package com.acutecoder.pdf

import android.content.Context
import com.acutecoder.pdf.setting.PdfSettingsManager
import com.acutecoder.pdf.setting.SharedPreferencePdfSettingsSaver

fun Context.sharedPdfSettingsManager(name: String, mode: Int = Context.MODE_PRIVATE) =
    PdfSettingsManager(SharedPreferencePdfSettingsSaver(this, name, mode))

@PdfUnstableApi
fun PdfViewer.callWithScrollSpeedLimitDisabled(block: DisableScrollSpeedLimitScope.() -> Unit) {
    val scope = DisableScrollSpeedLimitScope()

    if (scrollSpeedLimit != PdfViewer.ScrollSpeedLimit.None) {
        val originalScrollSpeedLimit = scrollSpeedLimit
        scrollSpeedLimit = PdfViewer.ScrollSpeedLimit.None
        block.invoke(scope)
        scope.onEnabled?.invoke()
        scrollSpeedLimit = originalScrollSpeedLimit
    } else block.invoke(scope)
}

fun DisableScrollSpeedLimitScope.callIfScrollSpeedLimitIsEnabled(onEnabled: () -> Unit) {
    this.onEnabled = onEnabled
}

class DisableScrollSpeedLimitScope internal constructor(var onEnabled: (() -> Unit)? = null)
