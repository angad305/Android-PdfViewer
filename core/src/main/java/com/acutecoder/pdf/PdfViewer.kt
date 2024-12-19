package com.acutecoder.pdf

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.acutecoder.pdf.js.Body
import com.acutecoder.pdf.js.call
import com.acutecoder.pdf.js.callDirectly
import com.acutecoder.pdf.js.invoke
import com.acutecoder.pdf.js.set
import com.acutecoder.pdf.js.setDirectly
import com.acutecoder.pdf.js.toJsString
import com.acutecoder.pdf.js.toRgba
import com.acutecoder.pdf.js.with
import com.acutecoder.pdf.setting.UiSettings
import kotlin.math.abs

class PdfViewer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var isInitialized = false; private set
    var currentSource: String? = null; private set
    var currentPage: Int = 1; private set
    var pagesCount: Int = 0; private set
    var currentPageScale: Float = 0f; private set
    var currentPageScaleValue: String = ""; private set
    var properties: PdfDocumentProperties? = null; private set

    private val listeners = mutableListOf<PdfListener>()
    private val webInterface = WebInterface()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var onReadyListeners = mutableListOf<PdfViewer.() -> Unit>()
    private var tempBackgroundColor: Int? = null

    @SuppressLint("SetJavaScriptEnabled")
    private val webView: WebView = WebView(context).apply {
        setBackgroundColor(Color.TRANSPARENT)

        if (isInEditMode) return@apply

        settings.run {
            javaScriptEnabled = true
            allowFileAccess = true
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = true
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()

                if (url.startsWith("file:///android_asset/"))
                    return super.shouldOverrideUrlLoading(view, request)

                if (URLUtil.isValidUrl(url))
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (view == null) return

                if (!isInitialized) {
                    view callDirectly "setupHelper" {
                        post {
                            isInitialized = true
                            tempBackgroundColor?.let { setContainerBackgroundColor(it) }
                            onReadyListeners.forEach { it(this@PdfViewer) }
                        }
                    }
                }
            }
        }

        setDownloadListener { url, _, _, _, _ ->
            webInterface.getBase64StringFromBlobUrl(url)?.let { evaluateJavascript(it, null) }
        }
    }

    val ui = UiSettings(webView)
        get() {
            checkViewer()
            return field
        }

    val findController = FindController(webView)
        get() {
            checkViewer()
            return field
        }

    var pageScrollMode: PageScrollMode = PageScrollMode.VERTICAL
        set(value) {
            checkViewer()
            field = value
            webView callDirectly value.function()
            adjustAlignMode(value)
            setSnapPageTo(snapPage)
        }

    var pageSpreadMode: PageSpreadMode = PageSpreadMode.NONE
        set(value) {
            checkViewer()
            field = value
            webView callDirectly value.function()
        }

    var cursorToolMode: CursorToolMode = CursorToolMode.TEXT_SELECT
        set(value) {
            checkViewer()
            field = value
            webView callDirectly value.function()
        }

    var pageRotation: PageRotation = PageRotation.R_0
        set(value) {
            checkViewer()
            field = value
            webView set "pdfViewer.pagesRotation"(value.degree)
            dispatchRotationChange(value)
        }

    var doubleClickThreshold: Long = 300
        set(value) {
            checkViewer()
            field = value
            webView setDirectly "DOUBLE_CLICK_THRESHOLD"(value)
        }

    var longClickThreshold: Long = 500
        set(value) {
            checkViewer()
            field = value
            webView setDirectly "LONG_CLICK_THRESHOLD"(value)
        }

    @FloatRange(from = -4.0, to = 10.0)
    var minPageScale = 0.1f
        set(value) {
            field = value
            if (isInitialized) {
                if (value in Zoom_SCALE_RANGE) getActualScaleFor(Zoom.entries[abs(value.toInt()) - 1]) {
                    actualMinPageScale = it ?: actualMinPageScale
                } else actualMinPageScale = value
            }
            if (field != value)
                listeners.forEach { it.onScaleLimitChange(value, maxPageScale, defaultPageScale) }
        }

    @FloatRange(from = -4.0, to = 10.0)
    var maxPageScale = 10f
        set(value) {
            if (field != value)
                listeners.forEach { it.onScaleLimitChange(minPageScale, value, defaultPageScale) }
            field = value
            if (isInitialized) {
                if (value in Zoom_SCALE_RANGE) getActualScaleFor(Zoom.entries[abs(value.toInt()) - 1]) {
                    actualMaxPageScale = it ?: actualMaxPageScale
                } else actualMaxPageScale = value
            }
        }

    @FloatRange(from = -4.0, to = 10.0)
    var defaultPageScale = Zoom.AUTOMATIC.floatValue
        set(value) {
            if (field != value)
                listeners.forEach { it.onScaleLimitChange(minPageScale, maxPageScale, value) }
            field = value
            if (isInitialized) {
                if (value in Zoom_SCALE_RANGE) getActualScaleFor(Zoom.entries[abs(value.toInt()) - 1]) {
                    actualDefaultPageScale = it ?: actualDefaultPageScale
                    scalePageTo(actualDefaultPageScale)
                } else {
                    actualDefaultPageScale = value
                    scalePageTo(value)
                }
            }
        }

    var actualMinPageScale = 0f
        private set(value) {
            if (field != value)
                listeners.forEach {
                    it.onActualScaleLimitChange(value, actualMaxPageScale, actualDefaultPageScale)
                }
            field = value
            if (value > 0) webView setDirectly "MIN_SCALE"(value)
        }
    var actualMaxPageScale = 0f
        private set(value) {
            if (field != value)
                listeners.forEach {
                    it.onActualScaleLimitChange(actualMinPageScale, value, actualDefaultPageScale)
                }
            field = value
            if (value > 0) webView setDirectly "MAX_SCALE"(value)
        }
    var actualDefaultPageScale = 0f
        private set(value) {
            if (field != value)
                listeners.forEach {
                    it.onActualScaleLimitChange(actualMinPageScale, actualMaxPageScale, value)
                }
            field = value
        }

    @PdfUnstableApi
    var pageAlignMode = PageAlignMode.DEFAULT
        set(value) {
            checkViewer()
            field = value
            webView callDirectly value.function()
        }
    var snapPage = false
        set(value) {
            checkViewer()
            field = value
            setSnapPageTo(value)
        }

    init {
        val containerBgColor = attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.PdfViewer, defStyleAttr, 0)
            val color =
                typedArray.getColor(R.styleable.PdfViewer_containerBackgroundColor, COLOR_NOT_FOUND)
            typedArray.recycle()
            color
        } ?: COLOR_NOT_FOUND

        if (!isInEditMode) {
            addView(webView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            webView.addJavascriptInterface(webInterface, "JWI")
            loadPage()

            if (containerBgColor != COLOR_NOT_FOUND)
                setContainerBackgroundColor(containerBgColor)
        } else setPreviews(context, containerBgColor)
    }

    @JvmOverloads
    fun load(source: String, originalUrl: String = source) {
        checkViewer()
        currentPage = 1
        pagesCount = 0
        currentPageScale = 0f
        currentPageScaleValue = ""
        properties = null
        currentSource = source

        listeners.forEach { it.onPageLoadStart() }
        webView callDirectly "openFile"("{url: '$source', originalUrl: '$originalUrl'}")
    }

    fun onReady(onReady: PdfViewer.() -> Unit) {
        onReadyListeners.add(onReady)
    }

    fun addListener(listener: PdfListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: PdfListener) {
        listeners.remove(listener)
    }

    fun goToPage(@IntRange(from = 1) pageNumber: Int): Boolean {
        if (pageNumber in 1..pagesCount) {
            webView set "page"(pageNumber)
            return true
        }

        return false
    }

    fun scrollToRatio(@FloatRange(from = 0.0, to = 1.0) ratio: Float) {
        webView callDirectly "scrollToRatio"(ratio)
    }

    fun scrollTo(@IntRange(from = 0) offset: Int) {
        webView callDirectly "scrollTo"(offset)
    }

    fun goToNextPage() = goToPage(currentPage + 1)

    fun goToPreviousPage() = goToPage(currentPage - 1)

    fun goToFirstPage() {
        webView callDirectly "goToFirstPage"()
    }

    fun goToLastPage() {
        webView callDirectly "goToLastPage"()
    }

    fun scalePageTo(@FloatRange(from = -4.0, to = 10.0) scale: Float) {
        if (scale in Zoom_SCALE_RANGE)
            zoomTo(Zoom.entries[abs(scale.toInt()) - 1])
        else {
            if (actualMaxPageScale < actualMinPageScale)
                throw RuntimeException("Max Page Scale($actualMaxPageScale) is less than Min Page Scale($actualMinPageScale)")
            webView set "pdfViewer.currentScale"(
                scale.coerceIn(actualMinPageScale, actualMaxPageScale)
            )
        }
    }

    fun zoomIn() {
        webView call "zoomIn"()
    }

    fun zoomOut() {
        webView call "zoomOut"()
    }

    fun zoomTo(zoom: Zoom) {
        getActualScaleFor(zoom) { scale ->
            if (scale != null && scale in actualMinPageScale..actualMaxPageScale)
                webView set "pdfViewer.currentScaleValue"(zoom.value.toJsString())
        }
    }

    fun zoomToMaximum() {
        scalePageTo(actualMaxPageScale)
    }

    fun zoomToMinimum() {
        scalePageTo(actualMinPageScale)
    }

    fun isZoomInMaxScale(): Boolean {
        return currentPageScale == actualMaxPageScale
    }

    fun isZoomInMinScale(): Boolean {
        return currentPageScale == actualMinPageScale
    }

    fun downloadFile() {
        webView callDirectly "downloadFile"()
    }

    @PdfUnstableApi
    fun printFile() {
        webView callDirectly "printFile"()
    }

    @PdfUnstableApi
    fun startPresentationMode() {
        webView callDirectly "startPresentationMode"()
    }

    fun rotateClockWise() {
        pageRotation = PageRotation.entries.let { it[(it.indexOf(pageRotation) + 1) % it.size] }
    }

    fun rotateCounterClockWise() {
        pageRotation = PageRotation.entries.let {
            it[(it.indexOf(pageRotation) - 1).let { index ->
                if (index < 0) index + it.size else index
            }]
        }
    }

    fun showDocumentProperties() {
        webView callDirectly "showDocumentProperties"()
    }

    fun reInitialize() {
        isInitialized = false
        webView.reload()
    }

    fun setContainerBackgroundColor(@ColorInt color: Int) {
        if (!isInitialized) {
            tempBackgroundColor = color
            return
        }
        if (tempBackgroundColor != null) tempBackgroundColor = null

        webView with Body set "style.backgroundColor"(color.toRgba().toJsString())
    }

    fun saveState(outState: Bundle) {
        webView.saveState(outState)
    }

    fun restoreState(inState: Bundle) {
        webView.restoreState(inState)
    }

    fun getActualScaleFor(zoom: Zoom, callback: (scale: Float?) -> Unit) {
        webView callDirectly "getActualScaleFor"(zoom.value.toJsString()) {
            callback(it?.toFloatOrNull())
        }
    }

    private fun dispatchRotationChange(pageRotation: PageRotation) {
        listeners.forEach { it.onRotationChange(pageRotation) }
    }

    private fun loadPage() {
        webView.loadUrl(PDF_VIEWER_URL)
    }

    private fun checkViewer() {
        if (!isInitialized) throw PdfViewerNotInitializedException()
    }

    @OptIn(PdfUnstableApi::class)
    private fun adjustAlignMode(scrollMode: PageScrollMode) {
        when (scrollMode) {
            PageScrollMode.VERTICAL, PageScrollMode.WRAPPED -> {
                if (pageAlignMode == PageAlignMode.CENTER_VERTICAL || pageAlignMode == PageAlignMode.CENTER_BOTH)
                    pageAlignMode = PageAlignMode.DEFAULT
            }

            PageScrollMode.HORIZONTAL -> {
                if (pageAlignMode == PageAlignMode.CENTER_HORIZONTAL || pageAlignMode == PageAlignMode.CENTER_BOTH)
                    pageAlignMode = PageAlignMode.DEFAULT
            }

            PageScrollMode.SINGLE_PAGE -> {}
        }
    }

    private fun setSnapPageTo(value: Boolean) {
        if (value) {
            when (pageScrollMode) {
                PageScrollMode.HORIZONTAL -> webView callDirectly "enableHorizontalViewPagerBehavior"()
                PageScrollMode.VERTICAL, PageScrollMode.WRAPPED -> webView callDirectly "enableVerticalViewPagerBehavior"()
                else -> {}
            }
        } else webView callDirectly "removeViewPagerBehavior"()
    }

    private fun setUpActualScaleValues(callback: () -> Unit) {
        var isMinSet = false
        var isMaxSet = false
        var isDefaultSet = false
        fun checkAndCall() {
            if (isMinSet && isMaxSet && isDefaultSet) callback()
        }

        if (minPageScale in Zoom_SCALE_RANGE)
            getActualScaleFor(Zoom.entries[abs(minPageScale.toInt()) - 1]) {
                actualMinPageScale = it ?: actualMinPageScale
                isMinSet = true
                checkAndCall()
            }
        else {
            actualMinPageScale = minPageScale
            isMinSet = true
            checkAndCall()
        }

        if (maxPageScale in Zoom_SCALE_RANGE)
            getActualScaleFor(Zoom.entries[abs(maxPageScale.toInt()) - 1]) {
                actualMaxPageScale = it ?: actualMaxPageScale
                isMaxSet = true
                checkAndCall()
            }
        else {
            actualMaxPageScale = maxPageScale
            isMaxSet = true
            checkAndCall()
        }

        if (defaultPageScale in Zoom_SCALE_RANGE)
            getActualScaleFor(Zoom.entries[abs(defaultPageScale.toInt()) - 1]) {
                actualDefaultPageScale = it ?: actualDefaultPageScale
                isDefaultSet = true
                checkAndCall()
            }
        else {
            actualDefaultPageScale = defaultPageScale
            isDefaultSet = true
            checkAndCall()
        }
    }

    enum class Zoom(internal val value: String, val floatValue: Float) {
        AUTOMATIC("auto", -1f),
        PAGE_FIT("page-fit", -2f),
        PAGE_WIDTH("page-width", -3f),
        ACTUAL_SIZE("page-actual", -4f)
    }

    enum class CursorToolMode(internal val function: String) {
        TEXT_SELECT("selectCursorSelectTool"),
        HAND("selectCursorHandTool")
    }

    enum class PageScrollMode(internal val function: String) {
        VERTICAL("selectScrollVertical"),
        HORIZONTAL("selectScrollHorizontal"),
        WRAPPED("selectScrollWrapped"),
        SINGLE_PAGE("selectScrollPage")
    }

    enum class PageSpreadMode(internal val function: String) {
        NONE("selectSpreadNone"),
        ODD("selectSpreadOdd"),
        EVEN("selectSpreadEven")
    }

    enum class PageRotation(internal val degree: Int) {
        R_0(0),
        R_90(90),
        R_180(180),
        R_270(270),
    }

    enum class PageAlignMode(internal val function: () -> Triple<String, String, ((String?) -> Unit)?>) {
        DEFAULT({ "centerPage"() }),
        CENTER_VERTICAL({ "centerPage"(true) }),
        CENTER_HORIZONTAL({ "centerPage"(false, true) }),
        CENTER_BOTH({ "centerPage"(true, true) }),
    }

    companion object {
        private const val PDF_VIEWER_URL =
            "file:///android_asset/com/acutecoder/mozilla/pdfjs/pdf_viewer.html"
        private const val COLOR_NOT_FOUND = 11
        private val Zoom_SCALE_RANGE = -4f..-1f
    }

    @Suppress("Unused")
    private inner class WebInterface {
        @JavascriptInterface
        fun onLoadSuccess(count: Int) = post {
            pagesCount = count
            setUpActualScaleValues {
                scalePageTo(actualDefaultPageScale)
            }
            pageRotation = pageRotation
            @OptIn(PdfUnstableApi::class)
            pageAlignMode = pageAlignMode
            snapPage = snapPage
            listeners.forEach { it.onPageLoadSuccess(count) }
        }

        @JavascriptInterface
        fun onLoadFailed(error: String) = post {
            listeners.forEach { it.onPageLoadFailed(error) }
        }

        @JavascriptInterface
        fun onPageChange(pageNumber: Int) = post({ currentPage != pageNumber }) {
            currentPage = pageNumber
            listeners.forEach { it.onPageChange(pageNumber) }
        }

        @JavascriptInterface
        fun onScaleChange(scale: Float, scaleValue: String) =
            post({ currentPageScale != scale || currentPageScaleValue != scaleValue }) {
                currentPageScale = scale
                currentPageScaleValue = scaleValue
                listeners.forEach { it.onScaleChange(scale) }
            }

        @JavascriptInterface
        fun onFindMatchStart() = post {
            listeners.forEach { it.onFindMatchStart() }
        }

        @JavascriptInterface
        fun onFindMatchChange(current: Int, total: Int) = post {
            listeners.forEach { it.onFindMatchChange(current, total) }
        }

        @JavascriptInterface
        fun onFindMatchComplete(found: Boolean) = post {
            listeners.forEach { it.onFindMatchComplete(found) }
        }

        @JavascriptInterface
        fun onScroll(currentOffset: Int, totalOffset: Int) = post {
            listeners.forEach { it.onScrollChange(currentOffset, totalOffset) }
        }

        @JavascriptInterface
        fun onPasswordDialogChange(isOpen: Boolean) = post {
            listeners.forEach { it.onPasswordDialogChange(isOpen) }
        }

        @JavascriptInterface
        fun onSpreadModeChange(ordinal: Int) = post {
            listeners.forEach { it.onSpreadModeChange(PageSpreadMode.entries[ordinal]) }
        }

        @JavascriptInterface
        fun onScrollModeChange(ordinal: Int) = post {
            listeners.forEach { it.onScrollModeChange(PageScrollMode.entries[ordinal]) }
        }

        @JavascriptInterface
        fun onSingleClick() = post {
            listeners.forEach { it.onSingleClick() }
        }

        @JavascriptInterface
        fun onDoubleClick() = post {
            listeners.forEach { it.onDoubleClick() }
        }

        @JavascriptInterface
        fun onLongClick() = post {
            listeners.forEach { it.onLongClick() }
        }

        @JavascriptInterface
        fun onLinkClick(link: String) = post {
            if (!link.startsWith(PDF_VIEWER_URL))
                listeners.forEach { it.onLinkClick(link) }
        }

        @JavascriptInterface
        fun onLoadProperties(
            title: String,
            subject: String,
            author: String,
            creator: String,
            producer: String,
            creationDate: String,
            modifiedDate: String,
            keywords: String,
            language: String,
            pdfFormatVersion: String,
            fileSize: Long,
            isLinearized: Boolean,
            encryptFilterName: String,
            isAcroFormPresent: Boolean,
            isCollectionPresent: Boolean,
            isSignaturesPresent: Boolean,
            isXFAPresent: Boolean,
            customJson: String
        ) = post {
            properties = PdfDocumentProperties(
                title = title,
                subject = subject,
                author = author,
                creator = creator,
                producer = producer,
                creationDate = creationDate,
                modifiedDate = modifiedDate,
                keywords = keywords,
                language = language,
                pdfFormatVersion = pdfFormatVersion,
                fileSize = fileSize,
                isLinearized = isLinearized,
                encryptFilterName = encryptFilterName,
                isAcroFormPresent = isAcroFormPresent,
                isCollectionPresent = isCollectionPresent,
                isSignaturesPresent = isSignaturesPresent,
                isXFAPresent = isXFAPresent,
                customJson = customJson,
            ).apply { listeners.forEach { it.onLoadProperties(this) } }
        }

        @JavascriptInterface
        fun getBase64FromBlobData(base64Data: String) {
            val pdfAsBytes: ByteArray = Base64.decode(
                base64Data.replaceFirst("^data:application/pdf;base64,".toRegex(), ""),
                0
            )

            post { listeners.forEach { it.onSavePdf(pdfAsBytes) } }
        }

        fun getBase64StringFromBlobUrl(blobUrl: String): String? {
            if (blobUrl.startsWith("blob")) {
                return "var xhr = new XMLHttpRequest();" +
                        "xhr.open('GET', '" + blobUrl + "', true);" +
                        "xhr.setRequestHeader('Content-type','application/pdf');" +
                        "xhr.responseType = 'blob';" +
                        "xhr.onload = function(e) {" +
                        "    if (this.status == 200) {" +
                        "        var blobPdf = this.response;" +
                        "        var reader = new FileReader();" +
                        "        reader.readAsDataURL(blobPdf);" +
                        "        reader.onloadend = function() {" +
                        "            base64data = reader.result;" +
                        "            JWI.getBase64FromBlobData(base64data);" +
                        "        }" +
                        "    }" +
                        "};" +
                        "xhr.send();"
            }
            return null
        }

        private inline fun post(
            crossinline condition: () -> Boolean = { true },
            runnable: Runnable
        ) {
            mainHandler.post {
                if (condition()) runnable.run()
            }
        }
    }

    private fun setPreviews(context: Context, containerBgColor: Int) {
        addView(
            LinearLayout(context).apply {
                orientation = VERTICAL
                if (containerBgColor == COLOR_NOT_FOUND) {
                    if (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES)
                        setBackgroundColor(Color.parseColor("#2A2A2E"))
                    else setBackgroundColor(Color.parseColor("#d4d4d7"))
                } else setBackgroundColor(containerBgColor)
                addView(createPageView(context, 1))
                addView(createPageView(context, 2))
                addView(createPageView(context, 3))
            },
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
    }

    @SuppressLint("SetTextI18n")
    private fun createPageView(context: Context, pageNo: Int): View {
        return TextView(context).apply {
            setBackgroundColor(Color.WHITE)
            layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f).apply {
                    setMargins(24, 24, 24, 0)
                }
            gravity = Gravity.CENTER
            text = "Page $pageNo"
            setTextColor(Color.BLACK)
        }
    }

}
