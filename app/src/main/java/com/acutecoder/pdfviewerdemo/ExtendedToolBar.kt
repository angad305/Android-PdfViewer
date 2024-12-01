package com.acutecoder.pdfviewerdemo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.acutecoder.pdf.ui.PdfToolBar

class ExtendedToolBar : PdfToolBar {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    override fun getPopupMenu(anchorView: View): PopupMenu {
        return PopupMenu(context, anchorView).apply {
            if (pdfViewer.currentUrl?.startsWith("file:///android_asset") == false)
                menu.add(Menu.NONE, 8, Menu.NONE, "Open in other app") // Item ids 0-7 are already used
            addDefaultMenus(this)
        }
    }

    override fun handlePopupMenuItemClick(item: MenuItem): Boolean {
        if (super.handlePopupMenuItemClick(item)) return true

        if (item.itemId == 8) {
            val uri = Uri.parse(pdfViewer.currentUrl)
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri).apply {
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            )
            return true
        }

        return false
    }
}
