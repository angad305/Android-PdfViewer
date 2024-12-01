package com.acutecoder.pdf.ui

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.acutecoder.pdf.PdfListener
import com.acutecoder.pdf.PdfViewer

class PdfContainer : RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    var pdfViewer: PdfViewer? = null; private set
    var pdfToolBar: PdfToolBar? = null; private set
    var pdfScrollBar: PdfScrollBar? = null; private set

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        when (child) {
            is PdfViewer -> {
                this.pdfViewer = child
                setup()

                pdfToolBar?.let { toolBar ->
                    super.addView(child, index, params.apply {
                        if (this is LayoutParams) {
                            addRule(BELOW, toolBar.id)
                        }
                    })
                } ?: super.addView(child, index, params)
            }

            is PdfScrollBar -> {
                this.pdfScrollBar = child
                setup()

                super.addView(child, index, params.apply {
                    if (this is LayoutParams) {
                        addRule(ALIGN_PARENT_END)
                    }
                })
            }

            is PdfToolBar -> {
                this.pdfToolBar = child
                super.addView(child, index, params)
                setup()

                context?.let {
                    if (it is Activity)
                        child.onBack = it::finish
                }
            }

            else -> super.addView(child, index, params)
        }
    }

    fun setAsLoader(loader: View) {
        pdfViewer?.addListener(object : PdfListener {
            override fun onPageLoadStart() {
                loader.visibility = View.VISIBLE
            }

            override fun onPageLoadSuccess(pagesCount: Int) {
                loader.visibility = View.GONE
            }
        })
    }

    private fun setup() {
        pdfViewer?.let { viewer ->
            pdfToolBar?.setupWith(viewer)
            pdfScrollBar?.setupWith(viewer, pdfToolBar)
        }
    }

}
