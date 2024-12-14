package com.acutecoder.pdfviewerdemo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.acutecoder.pdf.ui.PdfToolBar
import com.acutecoder.pdfviewerdemo.databinding.ZoomLimitDialogBinding
import kotlin.math.roundToInt

class ExtendedToolBar : PdfToolBar {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    override fun getPopupMenu(anchorView: View): PopupMenu {
        return PopupMenu(context, anchorView).apply {
            // Item ids 0-7 are already taken
            if (pdfViewer.currentUrl?.startsWith("file:///android_asset") == false)
                menu.add(Menu.NONE, 8, Menu.NONE, "Open in other app")
            menu.add(Menu.NONE, 9, Menu.NONE, "Zoom Limit")
            addDefaultMenus(this)
        }
    }

    override fun handlePopupMenuItemClick(item: MenuItem): Boolean {
        if (super.handlePopupMenuItemClick(item)) return true

        return when (item.itemId) {
            8 -> {
                val uri = Uri.parse(pdfViewer.currentUrl)
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, uri).apply {
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                )
                true
            }

            9 -> {
                showZoomLimitDialog()
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
