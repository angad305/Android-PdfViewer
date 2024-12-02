package com.acutecoder.pdfviewerdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.acutecoder.pdf.PdfListener
import com.acutecoder.pdf.PdfOnPageLoadFailed
import com.acutecoder.pdfviewerdemo.databinding.ActivityPdfViewerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var view: ActivityPdfViewerBinding

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
            load(filePath)
            if (filePath.isNotBlank())
                view.pdfToolBar.setFileName(fileName)
        }

        view.pdfToolBar.alertDialogBuilder = { MaterialAlertDialogBuilder(this) }
        view.pdfViewer.addListener(DownloadPdfListener(fileName))
        view.container.setAsLoader(view.loader)

        onBackPressedDispatcher.addCallback(this) {
            if (view.pdfToolBar.isFindBarVisible()) view.pdfToolBar.setFindBarVisible(false)
            else finish()
        }

        view.pdfViewer.addListener(PdfOnPageLoadFailed {
            toast(it)
            finish()
        })
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

private fun Uri.getFileName(context: Context): String {
    var name = "UNKNOWN"
    val cursor: Cursor? = context.contentResolver.query(
        this,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }
    }

    return name
}