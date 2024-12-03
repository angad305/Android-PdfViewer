package com.acutecoder.pdf.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import com.acutecoder.pdf.PdfListener
import com.acutecoder.pdf.PdfViewer
import kotlin.random.Random

class PdfContainer : RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    var pdfViewer: PdfViewer? = null; private set
    var pdfToolBar: PdfToolBar? = null; private set
    var pdfScrollBar: PdfScrollBar? = null; private set
    var alertDialogBuilder: () -> AlertDialog.Builder = { AlertDialog.Builder(context) }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        when (child) {
            is PdfViewer -> {
                this.pdfViewer = child
                child.addListener(PasswordDialogListener())
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
                        if (isInEditMode)
                            pdfToolBar?.id?.let { addRule(BELOW, it) }
                    }
                })
            }

            is PdfToolBar -> {
                this.pdfToolBar = child.apply {
                    if (id == NO_ID) id = Random.nextInt()
                }
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
                loader.visibility = VISIBLE
            }

            override fun onPageLoadSuccess(pagesCount: Int) {
                loader.visibility = GONE
            }
        })
    }

    private fun setup() {
        pdfViewer?.let { viewer ->
            pdfToolBar?.setupWith(viewer)
            pdfScrollBar?.setupWith(viewer, pdfToolBar)
        }
    }

    @Suppress("NOTHING_TO_INLINE", "FunctionName")
    private inline fun PasswordDialogListener() = object : PdfListener {
        var dialog: AlertDialog? = null

        override fun onPasswordDialogChange(isOpen: Boolean) {
            if (!isOpen) {
                dialog?.dismiss()
                dialog = null
                return
            }

            pdfViewer?.let { pdfViewer ->
                pdfViewer.ui.passwordDialog.getLabelText { title ->
                    @SuppressLint("InflateParams")
                    val root =
                        LayoutInflater.from(context).inflate(R.layout.pdf_go_to_page_dialog, null)
                    val field: EditText = root.findViewById<EditText?>(R.id.goToPageField).apply {
                        inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        imeOptions = EditorInfo.IME_ACTION_DONE
                        hint = "Password"
                    }

                    val submitPassword: (String, DialogInterface) -> Unit = { password, dialog ->
                        if (password.isEmpty()) onPasswordDialogChange(true)
                        else pdfViewer.ui.passwordDialog.submitPassword(password)
                        dialog.dismiss()
                    }

                    dialog = alertDialogBuilder()
                        .setTitle(title.replace("\"", ""))
                        .setView(root)
                        .setPositiveButton("Done") { dialog, _ ->
                            submitPassword(field.text.toString(), dialog)
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            pdfViewer.ui.passwordDialog.cancel()
                            dialog.dismiss()
                        }
                        .create()

                    field.setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            dialog?.let { submitPassword(field.text.toString(), it) }
                            true
                        } else {
                            false
                        }
                    }
                    dialog?.show()
                }
            }
        }
    }
}
