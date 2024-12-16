package com.acutecoder.pdf.setting

import com.acutecoder.pdf.PdfOnPageLoadSuccess
import com.acutecoder.pdf.PdfViewer

class PdfSettingsManager(private val saver: PdfSettingsSaver) {

    fun save(pdfViewer: PdfViewer, savePageNumber: Boolean = false) {
        saver.run {
            pdfViewer.run {
                if (savePageNumber)
                    save(KEY_CURRENT_PAGE, currentPage)
                else remove(KEY_CURRENT_PAGE)

                save(KEY_MIN_SCALE, minPageScale)
                save(KEY_MAX_SCALE, maxPageScale)
                save(KEY_DEFAULT_SCALE, currentPageScale)

                save(KEY_SCROLL_MODE, pageScrollMode)
                save(KEY_SPREAD_MODE, pageSpreadMode)
                save(KEY_ROTATION, pageRotation)

                apply()
            }
        }
    }

    fun restore(pdfViewer: PdfViewer) {
        saver.run {
            pdfViewer.run {
                val currentPage = getInt(KEY_CURRENT_PAGE, -1)
                if (currentPage != -1) {
                    addListener(PdfOnPageLoadSuccess { pagesCount ->
                        if (currentPage <= pagesCount)
                            goToPage(currentPage)
                    })
                }

                minPageScale = getFloat(KEY_MIN_SCALE, minPageScale)
                maxPageScale = getFloat(KEY_MAX_SCALE, maxPageScale)
                defaultPageScale = getFloat(KEY_DEFAULT_SCALE, defaultPageScale)

                pageScrollMode = getEnum(KEY_SCROLL_MODE, pageScrollMode)
                pageSpreadMode = getEnum(KEY_SPREAD_MODE, pageSpreadMode)
                pageRotation = getEnum(KEY_ROTATION, pageRotation)
            }
        }
    }

    fun reset() {
        saver.clearAll()
        saver.apply()
    }

    companion object {
        private const val KEY_CURRENT_PAGE = "current_page"
        private const val KEY_MIN_SCALE = "min_scale"
        private const val KEY_MAX_SCALE = "max_scale"
        private const val KEY_DEFAULT_SCALE = "default_scale"
        private const val KEY_SCROLL_MODE = "scroll_mode"
        private const val KEY_SPREAD_MODE = "spread_mode"
        private const val KEY_ROTATION = "rotation"
    }
}
