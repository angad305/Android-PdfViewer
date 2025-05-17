package com.bhuvaneshw.pdfviewerdemo

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.bhuvaneshw.pdf.PdfUnstableApi
import com.bhuvaneshw.pdf.PdfViewer
import com.bhuvaneshw.pdf.ui.PdfToolBar
import com.bhuvaneshw.pdfviewerdemo.databinding.ZoomLimitDialogBinding
import kotlin.math.roundToInt

class ExtendedToolBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PdfToolBar(context, attrs, defStyleAttr) {

    private val authority = "${BuildConfig.APPLICATION_ID}.file.provider"

    @OptIn(PdfUnstableApi::class)
    override fun getPopupMenu(anchorView: View): PopupMenu {
        return PopupMenu(context, anchorView).apply {
            // Item ids 0-10 are already taken
            if (pdfViewer.createSharableUri(authority) != null) {
                menu.add(Menu.NONE, 11, Menu.NONE, "Share")
                menu.add(Menu.NONE, 12, Menu.NONE, "Open With")
            }
            menu.add(Menu.NONE, 13, Menu.NONE, "Zoom Limit")
            menu.add(
                Menu.NONE,
                14,
                Menu.NONE,
                (if (pdfViewer.scrollSpeedLimit == PdfViewer.ScrollSpeedLimit.None) "Enable" else "Disable")
                        + " scroll speed limit"
            )
            addDefaultMenus(this)
        }
    }

    @OptIn(PdfUnstableApi::class)
    override fun handlePopupMenuItemClick(item: MenuItem): Boolean {
        if (super.handlePopupMenuItemClick(item)) return true

        return when (item.itemId) {
            11 -> {
                pdfViewer
                    .createSharableUri(authority)
                    ?.let {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, it)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share PDF using"))
                    } ?: context.toast("Unable to share pdf!")
                return true
            }

            12 -> {
                pdfViewer
                    .createSharableUri(authority)
                    ?.let {
                        context.startActivity(Intent(Intent.ACTION_VIEW, it).apply {
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        })
                    } ?: context.toast("Unable to open pdf with other apps!")
                return true
            }

            13 -> {
                showZoomLimitDialog()
                true
            }

            14 -> {
                if (pdfViewer.scrollSpeedLimit == PdfViewer.ScrollSpeedLimit.None)
                    pdfViewer.scrollSpeedLimit = PdfViewer.ScrollSpeedLimit.AdaptiveFling()
                else pdfViewer.scrollSpeedLimit = PdfViewer.ScrollSpeedLimit.None
                true
            }

            else -> false
        }
    }

    private fun showZoomLimitDialog() {
        val view = ZoomLimitDialogBinding.inflate(LayoutInflater.from(context))
        view.zoomSeekbar.apply {
            currentMinValue = (pdfViewer.minPageScale * 100).roundToInt()
            currentMaxValue = (pdfViewer.maxPageScale * 100).roundToInt()
        }

        alertDialogBuilder()
            .setTitle("Zoom Limit")
            .setView(view.root)
            .setPositiveButton("Done") { dialog, _ ->
                dialog.dismiss()
                view.zoomSeekbar.let {
                    pdfViewer.minPageScale = it.currentMinValue / 100f
                    pdfViewer.maxPageScale = it.currentMaxValue / 100f
                    pdfViewer.scalePageTo(pdfViewer.currentPageScale)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
