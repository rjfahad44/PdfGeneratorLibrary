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
            val pdfFileName = "$fileName.pdf"
            val sanitizedViews = views.filterNotNull() // Filter out any null views
            if (sanitizedViews.isEmpty()) {
                Log.e("XmlToPdfConverter", "No valid views to generate PDF.")
                onPdfCreated.invoke(null, "No valid views to generate PDF.")
                return
            }
            val pdfFile = generatePdfFile(sanitizedViews, pdfFileName, width, height)
            val finalFile = if (downloadPdf) saveToDownloadsFolder(pdfFile, pdfFileName) else pdfFile
            finalFile?.let { handlePdfOutput(it, showPdf, sharePdf) }
            onPdfCreated.invoke(finalFile, "Successfully generate PDF.")
            return
        } catch (e: Exception) {
            Log.e("XmlToPdfConverter", "Error creating PDF: ${e.message}", e)
            onPdfCreated.invoke(null, "Error creating PDF: ${e.message}")
            return
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

        val pdfFile = File(context.cacheDir, fileName)
        FileOutputStream(pdfFile).use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        return pdfFile
    }

    private fun saveToDownloadsFolder(pdfFile: File, fileName: String): File? {
        val folderName = "My-Resume"
        val resolver = context.contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Create a subfolder in Downloads using MediaStore
            val downloadsCollection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val newFileDetails = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.IS_PENDING, 1) // Set as pending before writing
            }

            try {
                // Insert the new file into MediaStore
                val fileUri = resolver.insert(downloadsCollection, newFileDetails)
                fileUri?.let { uri ->
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        pdfFile.inputStream().copyTo(outputStream)
                    }

                    // Mark as complete
                    newFileDetails.clear()
                    newFileDetails.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, newFileDetails, null, null)

                    //Toast.makeText(context, "File saved in Downloads folder.", Toast.LENGTH_SHORT).show()

                    uri.path?.let { File(it) } // Return the saved file
                }
            } catch (e: Exception) {
                Log.e("PdfSaveError", "Error saving file: ${e.message}", e)
                null
            }
        } else {
            // Legacy approach for Android 10 and below
            val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val downloadFile = File(downloadsDir, fileName)
            pdfFile.copyTo(downloadFile, overwrite = true)
            Toast.makeText(context, "File saved in Downloads folder.", Toast.LENGTH_SHORT).show()
            downloadFile
        }
    }



    private fun handlePdfOutput(file: File, showPdf: Boolean, sharePdf: Boolean) {
        when {
            sharePdf -> sharePdfFile(file)
            showPdf -> showPdf(file)
            else -> {
            }
        }
    }

    private fun showPdf(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No PDF viewer found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sharePdfFile(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF Using"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No apps found to share the PDF.", Toast.LENGTH_SHORT).show()
        }
    }
}
