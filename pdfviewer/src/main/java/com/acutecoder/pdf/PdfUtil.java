package com.acutecoder.pdf;

import kotlin.Unit;

public class PdfUtil {

    public static void onReady(PdfViewer viewer, PdfOnReadyListener onReadyListener) {
        viewer.onReady(viewer1 -> {
            onReadyListener.onReady();
            return Unit.INSTANCE;
        });
    }

    public interface PdfOnReadyListener {
        void onReady();
    }
}

