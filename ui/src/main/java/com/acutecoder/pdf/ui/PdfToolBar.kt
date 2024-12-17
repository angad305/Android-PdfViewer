package com.acutecoder.pdf.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import com.acutecoder.pdf.PdfDocumentProperties
import com.acutecoder.pdf.PdfListener
import com.acutecoder.pdf.PdfUnstableApi
import com.acutecoder.pdf.PdfViewer

open class PdfToolBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val layoutInflater = LayoutInflater.from(context)
    protected lateinit var pdfViewer: PdfViewer; private set

    var onBack: (() -> Unit)? = null
    var alertDialogBuilder: () -> AlertDialog.Builder = { AlertDialog.Builder(context) }
    var showDialog: (Dialog) -> Unit = { dialog -> dialog.show() }
    private var fileName: String? = null

    @SuppressLint("InflateParams")
    private val root = layoutInflater.inflate(R.layout.pdf_toolbar, null)
    val back: ImageView = root.findViewById(R.id.back)
    val title: TextView = root.findViewById(R.id.title)
    val find: ImageView = root.findViewById(R.id.find)
    val more: ImageView = root.findViewById(R.id.more)
    val findBar: LinearLayout = root.findViewById(R.id.findBar)
    val findEditText: EditText = root.findViewById(R.id.findEditText)
    val findProgressBar: ProgressBar = root.findViewById(R.id.findProgressBar)
    val findInfo: TextView = root.findViewById(R.id.findInfo)
    val findPrevious: ImageView = root.findViewById(R.id.findPrevious)
    val findNext: ImageView = root.findViewById(R.id.findNext)

    init {
        initListeners()

        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.PdfToolBar, defStyleAttr, 0)
            val contentColor = typedArray.getColor(
                R.styleable.PdfToolBar_contentColor,
                Color.BLACK
            )
            setContentColor(contentColor)
            typedArray.recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addView(root)
    }

    @SuppressLint("SetTextI18n")
    fun setupWith(pdfViewer: PdfViewer) {
        if (this::pdfViewer.isInitialized && this.pdfViewer == pdfViewer) return
        this.pdfViewer = pdfViewer
        initSecondaryMenu()

        pdfViewer.addListener(object : PdfListener {
            private var total = 0

            override fun onPageLoadStart() {
                find.isEnabled = false
                more.isEnabled = false
                setFindBarVisible(false)
            }

            override fun onPageLoadSuccess(pagesCount: Int) {
                find.isEnabled = true
                more.isEnabled = true
            }

            override fun onLoadProperties(properties: PdfDocumentProperties) {
                if (title.text.isBlank())
                    setTitle(properties.title)
            }

            override fun onFindMatchChange(current: Int, total: Int) {
                this.total = total
                findInfo.text = "$current of $total"
            }

            override fun onFindMatchStart() {
                findProgressBar.visibility = VISIBLE
            }

            override fun onFindMatchComplete(found: Boolean) {
                if (!found)
                    Toast.makeText(context, "No match found!", Toast.LENGTH_SHORT).show()
                findProgressBar.visibility = GONE
            }
        })

        find.isEnabled = false
        more.isEnabled = false
    }

    fun setFileName(name: String, setAsTitle: Boolean = true) {
        this.fileName = name
        if (setAsTitle)
            setTitle(name)
    }

    fun setTitle(title: String) {
        this.title.text = title
    }

    fun isFindBarVisible() = findBar.visibility == VISIBLE

    fun setFindBarVisible(visible: Boolean) {
        findBar.visibility = if (visible) VISIBLE else GONE
        findEditText.setText("")
        findInfo.text = ""

        if (visible) findEditText.requestKeyboard()
        else findEditText.hideKeyboard()
    }

    fun setContentColor(@ColorInt contentColor: Int) {
        find.setTintModes(contentColor)
        more.setTintModes(contentColor)
        back.setTintModes(contentColor)
        title.setTextColor(contentColor)
        findEditText.setTextColor(contentColor)
        findNext.setTintModes(contentColor)
        findPrevious.setTintModes(contentColor)
    }

    private fun initListeners() {
        back.setOnClickListener {
            if (isFindBarVisible()) setFindBarVisible(false)
            else onBack?.invoke()
        }

        find.setOnClickListener {
            setFindBarVisible(true)
        }
        findNext.setOnClickListener {
            pdfViewer.findController.findNext()
        }
        findPrevious.setOnClickListener {
            pdfViewer.findController.findPrevious()
        }

        findEditText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString()
                pdfViewer.findController.startFind(query)
                findEditText.hideKeyboard()
                true
            } else false
        }
    }

    private fun initSecondaryMenu() {
        more.setOnClickListener {
            val popupMenu = getPopupMenu(more)
            popupMenu.setOnMenuItemClickListener(this::handlePopupMenuItemClick)
            popupMenu.show()
        }
    }

    protected open fun handlePopupMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            PdfMenuItem.DOWNLOAD.id -> pdfViewer.downloadFile()
            PdfMenuItem.ZOOM.id -> showZoomDialog()
            PdfMenuItem.GO_TO_PAGE.id -> showGoToPageDialog()
            PdfMenuItem.ROTATE_CLOCK_WISE.id -> pdfViewer.rotateClockWise()
            PdfMenuItem.ROTATE_ANTI_CLOCK_WISE.id -> pdfViewer.rotateCounterClockWise()
            PdfMenuItem.SCROLL_MODE.id -> showScrollModeDialog()
            PdfMenuItem.SPREAD_MODE.id -> showSpreadModeDialog()
            PdfMenuItem.ALIGN_MODE.id -> showAlignModeDialog()
            PdfMenuItem.SNAP_PAGE.id -> showSnapPageDialog()
            PdfMenuItem.PROPERTIES.id -> showPropertiesDialog()
        }

        return item.itemId < PdfMenuItem.entries.size
    }

    protected open fun getPopupMenu(anchorView: View): PopupMenu {
        return addDefaultMenus(PopupMenu(context, anchorView))
    }

    protected fun addDefaultMenus(popupMenu: PopupMenu): PopupMenu {
        return popupMenu.apply {
            addMenu("Download", PdfMenuItem.DOWNLOAD)
            addMenu(
                pdfViewer.currentPageScaleValue.formatZoom(pdfViewer.currentPageScale),
                PdfMenuItem.ZOOM
            )
            addMenu("Go to page", PdfMenuItem.GO_TO_PAGE)
            addMenu("Rotate Clockwise", PdfMenuItem.ROTATE_CLOCK_WISE)
            addMenu("Rotate Anti Clockwise", PdfMenuItem.ROTATE_ANTI_CLOCK_WISE)
            addMenu("Scroll Mode", PdfMenuItem.SCROLL_MODE)
            addMenu("Split Mode", PdfMenuItem.SPREAD_MODE)
            addMenu("Align Mode", PdfMenuItem.ALIGN_MODE)
            addMenu("Snap Page", PdfMenuItem.SNAP_PAGE)
            addMenu("Properties", PdfMenuItem.PROPERTIES)
        }
    }

    private fun showZoomDialog() {
        val displayOptions = arrayOf(
            "Automatic", "Page Fit", "Page Width", "Actual Size",
            "50%", "75%", "100%", "125%", "150%", "200%", "300%", "400%"
        )
        val options = arrayOf(
            Zoom.AUTOMATIC.value, Zoom.PAGE_FIT.value,
            Zoom.PAGE_WIDTH.value, Zoom.ACTUAL_SIZE.value,
            "0.5", "0.75", "1", "1.25", "1.5", "2", "3", "4"
        )

        val dialog = alertDialogBuilder()
            .setTitle("Select Zoom Level")
            .setSingleChoiceItems(
                displayOptions,
                findSelectedOption(options, pdfViewer.currentPageScaleValue)
            ) { dialog, which ->
                when (which) {
                    0 -> pdfViewer.zoomTo(PdfViewer.Zoom.AUTOMATIC)
                    1 -> pdfViewer.zoomTo(PdfViewer.Zoom.PAGE_FIT)
                    2 -> pdfViewer.zoomTo(PdfViewer.Zoom.PAGE_WIDTH)
                    3 -> pdfViewer.zoomTo(PdfViewer.Zoom.ACTUAL_SIZE)
                    else -> pdfViewer.scalePageTo(scale = options[which].toFloatOrNull() ?: 1f)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        showDialog(dialog)
    }

    private fun showGoToPageDialog() {
        @SuppressLint("InflateParams")
        val root = layoutInflater.inflate(R.layout.pdf_go_to_page_dialog, null)
        val field: EditText = root.findViewById(R.id.goToPageField)

        val gotTo: (String, DialogInterface) -> Unit = { pageNumber, dialog ->
            pdfViewer.goToPage(pageNumber.toIntOrNull() ?: pdfViewer.currentPage)
            dialog.dismiss()
        }

        val dialog = alertDialogBuilder()
            .setTitle("Go to page")
            .setView(root)
            .setPositiveButton("Go") { dialog, _ ->
                gotTo(field.text.toString(), dialog)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        field.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                gotTo(field.text.toString(), dialog)
                true
            } else {
                false
            }
        }
        dialog.setOnShowListener {
            field.postDelayed({
                field.requestKeyboard()
            }, 500)
        }
        showDialog(dialog)
    }

    private fun showScrollModeDialog() {
        val displayOptions = arrayOf(
            "Vertical", "Horizontal", "Wrapped", "Single Page"
        )
        val options = arrayOf(
            PdfViewer.PageScrollMode.VERTICAL.name,
            PdfViewer.PageScrollMode.HORIZONTAL.name,
            PdfViewer.PageScrollMode.WRAPPED.name,
            PdfViewer.PageScrollMode.SINGLE_PAGE.name
        )

        val dialog = alertDialogBuilder()
            .setTitle("Select Page Scroll Mode")
            .setSingleChoiceItems(
                displayOptions,
                findSelectedOption(options, pdfViewer.pageScrollMode.name)
            ) { dialog, which ->
                pdfViewer.pageScrollMode = PdfViewer.PageScrollMode.valueOf(options[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        showDialog(dialog)
    }

    private fun showSpreadModeDialog() {
        val displayOptions = arrayOf(
            "None", "Odd", "Even"
        )
        val options = arrayOf(
            PdfViewer.PageSpreadMode.NONE.name,
            PdfViewer.PageSpreadMode.ODD.name,
            PdfViewer.PageSpreadMode.EVEN.name
        )

        val dialog = alertDialogBuilder()
            .setTitle("Select Page Split Mode")
            .setSingleChoiceItems(
                displayOptions,
                findSelectedOption(options, pdfViewer.pageSpreadMode.name)
            ) { dialog, which ->
                pdfViewer.pageSpreadMode = PdfViewer.PageSpreadMode.valueOf(options[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        showDialog(dialog)
    }

    @OptIn(PdfUnstableApi::class)
    private fun showAlignModeDialog() {
        val displayOptions = buildList {
            add("Default")
            if (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.VERTICAL && pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.WRAPPED)
                add("Center Vertically")
            if (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.HORIZONTAL)
                add("Center Horizontally")
            if (pdfViewer.pageScrollMode == PdfViewer.PageScrollMode.SINGLE_PAGE)
                add("Center Both")
        }.toTypedArray()
        val options = buildList {
            add(PdfViewer.PageAlignMode.DEFAULT.name)
            if (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.VERTICAL && pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.WRAPPED)
                add(PdfViewer.PageAlignMode.CENTER_VERTICAL.name)
            if (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.HORIZONTAL)
                add(PdfViewer.PageAlignMode.CENTER_HORIZONTAL.name)
            if (pdfViewer.pageScrollMode == PdfViewer.PageScrollMode.SINGLE_PAGE)
                add(PdfViewer.PageAlignMode.CENTER_BOTH.name)
        }.toTypedArray()

        val dialog = alertDialogBuilder()
            .setTitle("Select Page Align Mode")
            .setSingleChoiceItems(
                displayOptions,
                findSelectedOption(options, pdfViewer.pageAlignMode.name)
            ) { dialog, which ->
                pdfViewer.pageAlignMode = PdfViewer.PageAlignMode.valueOf(options[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        showDialog(dialog)
    }

    private fun showSnapPageDialog() {
        val root = layoutInflater.inflate(R.layout.pdf_snap_page_dialog, null)
        val switch = root.findViewById<SwitchCompat>(R.id.snap_page)
        switch.isChecked = pdfViewer.snapPage

        alertDialogBuilder()
            .setTitle("Snap Page")
            .setView(root)
            .setPositiveButton("Done") { dialog, _ ->
                pdfViewer.snapPage = switch.isChecked
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showPropertiesDialog() {
        alertDialogBuilder()
            .setTitle("Document Properties")
            .let {
                pdfViewer.properties?.let { properties ->
                    it.setPropertiesView(properties)
                } ?: it.setMessage("Properties not loaded yet!")
            }
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun AlertDialog.Builder.setPropertiesView(properties: PdfDocumentProperties): AlertDialog.Builder {
        @SuppressLint("InflateParams")
        val root = layoutInflater.inflate(R.layout.pdf_properties_dialog, null)

        root.find(R.id.fileName).text = this@PdfToolBar.fileName?.ifBlank { "-" } ?: "-"
        root.find(R.id.fileSize).text = properties.fileSize.formatToSize()
        root.find(R.id.title).text = properties.title
        root.find(R.id.subject).text = properties.subject
        root.find(R.id.author).text = properties.author
        root.find(R.id.creator).text = properties.creator
        root.find(R.id.producer).text = properties.producer
        root.find(R.id.creationDate).text = properties.creationDate.formatToDate()
        root.find(R.id.modifiedDate).text = properties.modifiedDate.formatToDate()
        root.find(R.id.keywords).text = properties.keywords
        root.find(R.id.language).text = properties.language
        root.find(R.id.pdfFormatVersion).text = properties.pdfFormatVersion
        root.find(R.id.isLinearized).text = properties.isLinearized.toString()

        return setView(root)
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun View.find(id: Int): TextView {
    return findViewById(id)
}

