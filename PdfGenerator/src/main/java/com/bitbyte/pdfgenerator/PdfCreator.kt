package com.bitbyte.pdfgenerator

import android.content.Context
import android.view.View
import java.io.File

// Public API class
class PdfCreator(private val context: Context) : IPdfCreator {

    private val converter = ViewsToPdfConverter(context)

    // Implement the interface method
    override fun createPdfFromViews(
        views: List<View?>,
        fileName: String,
        downloadPdf: Boolean,
        showPdf: Boolean,
        sharePdf: Boolean,
        width: Int?,
        height: Int?,
        onPdfCreated: (File?, String) -> Unit
    ) {
        converter.createPdfFromViews(
            views,
            fileName,
            downloadPdf,
            showPdf,
            sharePdf,
            width ?: context.resources.displayMetrics.widthPixels,
            height ?: context.resources.displayMetrics.heightPixels,
            onPdfCreated = onPdfCreated
        )
    }
}
