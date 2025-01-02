package com.bitbyte.pdfgenerator

import android.view.View
import java.io.File

// Interface for generating PDFs
interface IPdfCreator {
    fun createPdfFromViews(
        views: List<View?>,
        fileName: String,
        downloadPdf: Boolean = false,
        showPdf: Boolean = false,
        sharePdf: Boolean = false,
        width: Int? = null,
        height: Int? = null,
        onPdfCreated: (File?, String) -> Unit
    )
}
