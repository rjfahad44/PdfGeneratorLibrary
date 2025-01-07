package com.bitbyte.pdfgenerator

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class ViewsToPdfConverter(private val context: Context) {

    fun createPdfFromViews(
        views: List<View?>,
        fileName: String,
        downloadPdf: Boolean = false,
        showPdf: Boolean = false,
        sharePdf: Boolean = false,
        width: Int = context.resources.displayMetrics.widthPixels,
        height: Int = context.resources.displayMetrics.heightPixels,
        onPdfCreated: (File?, String) -> Unit
    ) {
        try {
            val sanitizedViews = views.filterNotNull()
            if (sanitizedViews.isEmpty()) {
                onPdfCreated(null, "No valid views to generate PDF.")
                return
            }

            val pdfFile = generatePdfFile(sanitizedViews, fileName, width, height)
            val savedFile = if (downloadPdf) saveData(pdfFile, fileName) else null

            if (downloadPdf && savedFile == null) {
                onPdfCreated(null, "Error saving PDF.")
                return
            }

            onPdfCreated(savedFile ?: pdfFile, "PDF created successfully.")
            handlePdfOutput(savedFile ?: pdfFile, showPdf, sharePdf)
        } catch (e: Exception) {
            Log.e("PdfError", "Error creating PDF: ${e.message}", e)
            onPdfCreated(null, "Error creating PDF: ${e.message}")
        }
    }

    private fun generatePdfFile(views: List<View>, fileName: String, width: Int, height: Int): File {
        val pdfDocument = PdfDocument()

        views.forEachIndexed { index, view ->
            val pageInfo = PdfDocument.PageInfo.Builder(width, height, index + 1).create()
            val page = pdfDocument.startPage(pageInfo)

            view.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, width, height)
            view.draw(page.canvas)

            pdfDocument.finishPage(page)
        }

        val pdfFile = File(context.cacheDir, "$fileName.pdf")
        FileOutputStream(pdfFile).use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        return pdfFile
    }

    private fun saveData(pdfFile: File, fileName: String): File? {
        val folderName = "My-Resume"
        val resolver = context.contentResolver

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val downloadsCollection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val newFileDetails = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$folderName")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            try {
                val fileUri = resolver.insert(downloadsCollection, newFileDetails) ?: return null
                resolver.openOutputStream(fileUri)?.use { pdfFile.inputStream().copyTo(it) }

                newFileDetails.clear()
                newFileDetails.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(fileUri, newFileDetails, null, null)

                File(fileUri.path ?: return null)
            } catch (e: Exception) {
                Log.e("PdfSaveError", "Error saving file: ${e.message}", e)
                null
            }
        } else {
            val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName)
            if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                Log.e("PdfSaveError", "Failed to create directory: ${downloadsDir.absolutePath}")
                return null
            }

            val downloadFile = File(downloadsDir, "$fileName.pdf")
            return try {
                pdfFile.copyTo(downloadFile, overwrite = true)
                downloadFile
            } catch (e: Exception) {
                Log.e("PdfSaveError", "Error saving file: ${e.message}", e)
                null
            }
        }
    }

    private fun handlePdfOutput(file: File, showPdf: Boolean, sharePdf: Boolean) {
        when {
            sharePdf -> sharePdfFile(file)
            showPdf -> showPdf(file)
        }
    }

    private fun showPdf(file: File) {
        if (!file.exists()) {
            Toast.makeText(context, "File not found.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No PDF viewer found.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sharePdfFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share PDF Using"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No apps found to share the PDF.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}