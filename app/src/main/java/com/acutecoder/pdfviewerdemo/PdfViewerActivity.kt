package com.acutecoder.pdfviewerdemo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.acutecoder.pdf.PdfListener
import com.acutecoder.pdf.PdfOnLinkClick
import com.acutecoder.pdf.PdfOnPageLoadFailed
import com.acutecoder.pdf.setting.PdfSettingsManager
import com.acutecoder.pdf.setting.sharedPdfSettingsManager
import com.acutecoder.pdfviewerdemo.databinding.ActivityPdfViewerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var view: ActivityPdfViewerBinding
    private var fullscreen = false
    private lateinit var pdfSettingsManager: PdfSettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        view = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(view.root)

        ViewCompat.setOnApplyWindowInsetsListener(view.container) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val filePath: String
        val fileName: String
        pdfSettingsManager = sharedPdfSettingsManager("PdfSettings", MODE_PRIVATE)
            .also { it.includeAll() }

        // View from other apps (from intent filter)
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            filePath = intent.data.toString()
            fileName = intent.data!!.getFileName(this)
        } else {
            // Path from asset, url or android uri
            filePath = intent.extras?.getString("filePath")
                ?: intent.extras?.getString("fileUrl")
                ?: intent.extras?.getString("fileUri")
                ?: run {
                    toast("No source available!")
                    finish()
                    return
                }

            fileName = intent.extras?.getString("fileUri")
                ?.let { uri -> Uri.parse(uri).getFileName(this) }
                ?: intent.extras?.getString("fileName") ?: ""
        }

        view.pdfViewer.onReady {
//            minPageScale = PdfViewer.Zoom.PAGE_WIDTH.floatValue
//            maxPageScale = 5f
//            defaultPageScale = PdfViewer.Zoom.PAGE_WIDTH.floatValue
            pdfSettingsManager.restore(this)
            load(filePath)
            if (filePath.isNotBlank())
                view.pdfToolBar.setFileName(fileName)
        }

        view.pdfToolBar.alertDialogBuilder = { MaterialAlertDialogBuilder(this) }
        view.container.alertDialogBuilder = view.pdfToolBar.alertDialogBuilder
        view.pdfViewer.addListener(DownloadPdfListener(fileName))
        view.container.setAsLoadingIndicator(view.loader)

        onBackPressedDispatcher.addCallback(this) {
            if (view.pdfToolBar.isFindBarVisible()) view.pdfToolBar.setFindBarVisible(false)
            else finish()
        }

        view.pdfViewer.run {
            addListener(PdfOnPageLoadFailed {
                toast(it)
                finish()
            })
            addListener(PdfOnLinkClick { link ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
            })
        }

        view.pdfViewer.addListener(object : PdfListener {
            override fun onSingleClick() {
                fullscreen = !fullscreen
                setFullscreen(fullscreen)
                view.container.animateToolBar(!fullscreen)
            }

            override fun onDoubleClick() {
                view.pdfViewer.run {
                    if (!isZoomInMinScale()) zoomToMinimum()
                    else zoomToMaximum()
                }
            }
        })
    }

    override fun onPause() {
        pdfSettingsManager.save(view.pdfViewer)
        super.onPause()
    }

    override fun onDestroy() {
        pdfSettingsManager.save(view.pdfViewer)
        super.onDestroy()
    }

    inner class DownloadPdfListener(private val pdfTitle: String) : PdfListener {
        private var bytes: ByteArray? = null
        private val saveFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                bytes?.let { pdfAsBytes ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        result.data?.data?.let { uri ->
                            try {
                                contentResolver.openOutputStream(uri)?.use { it.write(pdfAsBytes) }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }

        override fun onSavePdf(pdfAsBytes: ByteArray) {
            bytes = pdfAsBytes

            saveFileLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
                putExtra(Intent.EXTRA_TITLE, pdfTitle)
            })
        }
    }

}
