package com.bitbyte.pdfgenerator

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

class XmlToPdfConverter(private val context: Context) {
    private fun convertBitmapToPdf(
        view: List<View>,
        fileName: String,
        showPdf: Boolean,
        sharePdf: Boolean,
        width: Int,
        height: Int
    ): File? {
        val pdfDocument = PdfDocument()
        var pageNumber: Int = 1
        view.forEach {
            val pageInfo = PdfDocument.PageInfo.Builder(width, height, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            it.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
            it.layout(0, 0, width, height)
            it.draw(page.canvas)
            pdfDocument.finishPage(page)
            pageNumber++
        }

        return try {
            val shareFilePath = File(context.cacheDir, fileName)
            val downloadFilePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (sharePdf){
//                context.contentResolver.openOutputStream(downloadFilePath.toUri())?.use { outputStream ->
//                    pdfDocument.writeTo(outputStream)
//                    outputStream.flush()
//                    Log.d("Fahad007","File saved successfully.")
//                }
                pdfDocument.writeTo(FileOutputStream(downloadFilePath))
                sharePdfFile(shareFilePath)
            }else if (showPdf){
//                context.contentResolver.openOutputStream(downloadFilePath.toUri())?.use { outputStream ->
//                    pdfDocument.writeTo(outputStream)
//                    outputStream.flush()
//                    Log.d("Fahad007","File saved successfully.")
//                }
                pdfDocument.writeTo(FileOutputStream(downloadFilePath))
                showPdf(downloadFilePath)
            }else{
//                context.contentResolver.openOutputStream(downloadFilePath.toUri())?.use { outputStream ->
//                    pdfDocument.writeTo(outputStream)
//                    outputStream.flush()
//                    Log.d("Fahad007","File saved successfully.")
//                }
                pdfDocument.writeTo(FileOutputStream(downloadFilePath))
                Toast.makeText(context, "File save into DOWNLOADS Folder.", Toast.LENGTH_SHORT).show()
            }
            pdfDocument.close()
            downloadFilePath
        } catch (e: Exception) {
            pdfDocument.close()
            e.printStackTrace()
            null
        }
    }

    fun createPdf(
        view: List<View>,
        fileName: String,
        showPdf: Boolean = false,
        sharePdf: Boolean = false,
        width: Int = context.resources.displayMetrics.widthPixels,
        height: Int = context.resources.displayMetrics.heightPixels
    ): File? {
        return convertBitmapToPdf(view,"$fileName.pdf", showPdf, sharePdf, width, height)
    }


    private fun showPdf(filePath: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "com.ft.cv_maker.files.provider",
            filePath
        )
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, "application/pdf")

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun sharePdfFile(filePath: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "com.ft.cv_maker.files.provider",
            filePath
        )
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "application/pdf"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF Using : "))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun saveFileToDownloads(fileName: String, data: ByteArray): Uri? {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above, use MediaStore to create a file in Downloads
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        } else {
            // For Android 9 and below, use the traditional method
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(downloadDir, fileName).apply {
                if (!exists()) {
                    createNewFile()
                }
            }.toUri()
        }

        uri?.let {
            writeDataToFile(it, data)
        } ?: run {
            Log.d("Fahad007","Error: Could not create file in Downloads directory")
        }
        return uri
    }

    private fun writeDataToFile(uri: Uri, data: ByteArray) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(data)
                outputStream.flush()
                Log.d("Fahad007","File saved successfully.")
            }
        } catch (e: Exception) {
            Log.d("Fahad007","Error writing data: ${e.message}")
        }
    }
}