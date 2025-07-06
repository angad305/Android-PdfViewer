package com.bhuvaneshw.pdf.resource

import android.content.Context

object PdfResources {

    fun isColorProfileAvailable(context: Context): Boolean {
        return context.call(
            className = "com.bhuvaneshw.pdf.icc.PdfColorProfile",
            methodName = "isPresent"
        )
    }

    fun isJPEG2000Available(context: Context): Boolean {
        return context.call(
            className = "com.bhuvaneshw.pdf.jp2.JPEG2000",
            methodName = "isPresent"
        )
    }

    private fun Context.call(className: String, methodName: String): Boolean {
        return try {
            val clazz = Class.forName(className)
            val instance = clazz.getDeclaredConstructor().newInstance()
            val method = clazz.getDeclaredMethod(methodName, Context::class.java)

            method.isAccessible = true
            method.invoke(instance, this) as Boolean
        } catch (_: Exception) {
            false
        }
    }

}
