package com.acutecoder.pdfviewerdemo

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Uri.getFileName(context: Context): String {
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

fun Activity.setFullscreen(fullscreen: Boolean) {
    val window = window ?: return
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)

    insetsController.apply {
        systemBarsBehavior = if (fullscreen) {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            show(WindowInsetsCompat.Type.statusBars())
            show(WindowInsetsCompat.Type.navigationBars())
            WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }

}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
