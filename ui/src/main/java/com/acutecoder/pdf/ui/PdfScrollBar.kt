package com.acutecoder.pdf.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import com.acutecoder.pdf.PdfListener
import com.acutecoder.pdf.PdfViewer
import java.util.Timer
import java.util.TimerTask
import kotlin.math.roundToInt

class PdfScrollBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @SuppressLint("InflateParams")
    private val root = LayoutInflater.from(context).inflate(R.layout.pdf_scrollbar, null)
    private val timer = Timer()
    private var timerTask: TimerTask? = null
    private var isSetupDone = false
    var hideDelayMillis = 2000L
    var animationDuration = 250L
    var interactiveScrolling = true

    val pageNumberInfo: TextView = root.findViewById(R.id.pageNumberInfo)
    val dragHandle: ImageView = root.findViewById(R.id.drag_handle)

    init {
        addView(root)

        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.PdfScrollBar, defStyleAttr, 0)
            val contentColor = typedArray.getColor(
                R.styleable.PdfScrollBar_contentColor,
                Color.BLACK
            )
            val handleColor = typedArray.getColor(
                R.styleable.PdfScrollBar_handleColor,
                0xfff1f1f1.toInt()
            )
            setContentColor(contentColor, handleColor)
            typedArray.recycle()
        }

        @SuppressLint("SetTextI18n")
        if (isInEditMode) pageNumberInfo.text = "1/3"
        else visibility = GONE
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    fun setupWith(pdfViewer: PdfViewer, toolBar: PdfToolBar? = null, force: Boolean = false) {
        if (isSetupDone && !force) return
        isSetupDone = true

        pdfViewer.post {
            val dragListener = DragListener(
                targetView = this,
                parentHeight = pdfViewer.height,
                topHeight = toolBar?.height ?: 0,
                onScrollChange = { y ->
                    val ratio = (y - (toolBar?.height ?: 0)) / (pdfViewer.height - height)
                    pdfViewer.scrollToRatio(ratio)
                },
                onUpdatePageInfoForNonInteractiveMode = { y ->
                    timerTask?.cancel()
                    if (visibility != VISIBLE) animateShow()
                    startTimer()

                    val ratio = (y - (toolBar?.height ?: 0)) / (pdfViewer.height - height)
                    val pageNumber =
                        (ratio * (pdfViewer.pagesCount - 1)).checkNaN(1f).roundToInt() + 1
                    pageNumberInfo.text = "$pageNumber/${pdfViewer.pagesCount}"
                }
            )

            pdfViewer.onReady { ui.viewerScrollbar = false }
            dragHandle.setOnTouchListener(dragListener)

            pdfViewer.addListener(object : PdfListener {
                override fun onPageChange(pageNumber: Int) {
                    pageNumberInfo.text = "$pageNumber/${pdfViewer.pagesCount}"
                }

                override fun onPageLoadStart() {
                    visibility = GONE
                }

                override fun onPageLoadSuccess(pagesCount: Int) {
                    pageNumberInfo.text = "${pdfViewer.currentPage}/${pdfViewer.pagesCount}"
                    pdfViewer.scrollTo(0)
                }

                override fun onScrollChange(currentOffset: Int, totalOffset: Int) {
                    timerTask?.cancel()
                    if (visibility != VISIBLE) animateShow()
                    startTimer()

                    if (!dragListener.isDragging) {
                        val ratio = currentOffset.toFloat() / totalOffset.toFloat()
                        val top = (pdfViewer.height - height) * ratio
                        translationY = (toolBar?.height ?: 0) + top
                    }
                }
            })
        }
    }

    fun setContentColor(@ColorInt contentColor: Int, @ColorInt handleColor: Int) {
        pageNumberInfo.setTextColor(contentColor)
        pageNumberInfo.setBgTintModes(handleColor)
        dragHandle.setTintModes(contentColor)
        dragHandle.setBgTintModes(handleColor)
    }

    private fun startTimer() {
        timerTask = object : TimerTask() {
            override fun run() {
                animateHide()
            }
        }
        timer.schedule(timerTask, hideDelayMillis)
    }

    private fun animateShow() {
        post {
            visibility = VISIBLE
            animate()
                .alpha(1f)
                .setDuration(animationDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        alpha = 1f
                    }
                })
                .start()
        }
    }

    private fun animateHide() {
        post {
            animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        visibility = GONE
                        alpha = 0f
                    }
                })
                .start()
        }
    }

    inner class DragListener(
        private var targetView: View,
        private val parentHeight: Int,
        private val topHeight: Int,
        private val onScrollChange: (y: Float) -> Unit,
        private val onUpdatePageInfoForNonInteractiveMode: (y: Float) -> Unit,
    ) : OnTouchListener {
        var isDragging: Boolean = false
        private var dY: Float = 0f

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dY = targetView.y - event.rawY
                    isDragging = true
                }

                MotionEvent.ACTION_MOVE -> {
                    val y =
                        (event.rawY + dY - height / 2).coerceIn(
                            topHeight.toFloat(),
                            topHeight.toFloat() + parentHeight.toFloat() - height
                        )

                    targetView.translationY = y
                    if (interactiveScrolling)
                        onScrollChange(y)
                    else onUpdatePageInfoForNonInteractiveMode(y)
                }

                else -> {
                    if (!interactiveScrolling)
                        onScrollChange(targetView.translationY)

                    isDragging = false
                    startTimer()

                    return false
                }
            }
            return true
        }
    }

}
